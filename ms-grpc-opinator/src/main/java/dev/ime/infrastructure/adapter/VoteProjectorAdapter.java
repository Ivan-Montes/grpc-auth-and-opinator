package dev.ime.infrastructure.adapter;

import java.util.Map;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import dev.ime.application.exception.CreateJpaEntityException;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.utils.MapExtractorHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.BaseProjectorPort;
import dev.ime.domain.port.outbound.ExtendedProjectorPort;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("voteProjectorAdapter")
public class VoteProjectorAdapter implements BaseProjectorPort, ExtendedProjectorPort {

	private final SessionFactory sessionFactory;
	private final MapExtractorHelper mapExtractorHelper;
	private static final Logger logger = LoggerFactory.getLogger(VoteProjectorAdapter.class);
	
	public VoteProjectorAdapter(SessionFactory sessionFactory, MapExtractorHelper mapExtractorHelper) {
		super();
		this.sessionFactory = sessionFactory;
		this.mapExtractorHelper = mapExtractorHelper;
	}

	@Override
	public Mono<Void> create(Event event) {

		return Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntity)
				.doOnNext(this::logFlowStep)
				.flatMap(this::insertAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<VoteJpaEntity> createJpaEntity(Map<String, Object> eventData) {
		
	    return Mono.defer(() -> {
	        UUID voteId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.VOT_ID);
	        String email = mapExtractorHelper.extractString(eventData, GlobalConstants.USERAPP_EMAIL, GlobalConstants.PATTERN_EMAIL);
	        UUID reviewId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.REV_ID);
	        Boolean useful = Boolean.parseBoolean(mapExtractorHelper.extractString(eventData, GlobalConstants.VOT_US, GlobalConstants.PATTERN_NAME_FULL));
	        
	        return findReviewById(reviewId)
	                .map(review -> VoteJpaEntity.builder()
	                        .voteId(voteId)
	                        .email(email)
	                        .review(review)
	                        .useful(useful)
	                        .build())
	                .onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.VOT_CAT, e.getMessage())));
	    });
	}
	
	private Mono<ReviewJpaEntity> findReviewById(UUID id) {

		String queryString = """
				SELECT
				 r
				FROM
				 ReviewJpaEntity r
				WHERE reviewId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, ReviewJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_ID,id.toString()))));
	}

	private Mono<VoteJpaEntity> insertAction(VoteJpaEntity entity) {

		return sessionFactory
		.withTransaction((session, tx) -> session.persist(entity)
				.replaceWith(entity))
		.convert().with(UniReactorConverters.toMono());
	}
	
	@Override
	public Mono<Void> update(Event event) {

		return Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntityForUpdateAction)
				.doOnNext(this::logFlowStep)
				.flatMap(this::updateAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<VoteJpaEntity> createJpaEntityForUpdateAction(Map<String, Object> eventData) {
		
	    return Mono.fromCallable(() -> {
	    	
	        UUID voteId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.VOT_ID);
	        Boolean useful = Boolean.parseBoolean(mapExtractorHelper.extractString(eventData, GlobalConstants.VOT_US, GlobalConstants.PATTERN_NAME_FULL));
	        
	        return VoteJpaEntity.builder()
	                        .voteId(voteId)
	                        .useful(useful)
	                        .build();
	        
	    }).onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.VOT_CAT, e.getMessage())));
	
	}
	
	private Mono<VoteJpaEntity> updateAction(VoteJpaEntity entity) {

		String query = """
				SELECT
				 v 
				FROM
				 VoteJpaEntity v 
				WHERE
				 v.voteId = :voteId
				""";
		
		return sessionFactory
		.withTransaction((session, tx) -> session.createQuery(query, VoteJpaEntity.class)
				.setParameter(GlobalConstants.VOT_ID, entity.getVoteId())
				.getSingleResult()
                .flatMap(item -> {
                	item.setUseful(entity.isUseful());
                    return session.merge(item);
                }))
				.convert().with(UniReactorConverters.toMono());	
	}
	
	@Override
	public Mono<Void> deleteById(Event event) {

		return Mono.justOrEmpty(event.getEventData())
				.doOnNext(this::logFlowStep)
				.map(evenData -> evenData.get(GlobalConstants.VOT_ID))
				.cast(String.class)
				.map(UUID::fromString)
				.doOnNext(this::logFlowStep)
				.flatMap(this::deleteByIdAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.VOT_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<VoteJpaEntity> deleteByIdAction(UUID entityId) {
		
	    return sessionFactory.withTransaction((session, tx) -> session.find(VoteJpaEntity.class, entityId)
	            .onItem().ifNull().failWith(() -> new ResourceNotFoundException(
	                Map.of(GlobalConstants.VOT_ID, entityId.toString())))
	            .call(session::remove))
	            .convert()
	    		.with(UniReactorConverters.toMono());	
	}

	private <T> Mono<T> addFinalLog(Mono<T> reactiveFlow) {
		
		return reactiveFlow
				.doOnSubscribe( subscribed -> this.logInfo( GlobalConstants.MSG_FLOW_SUBS, subscribed.toString()) )
				.doOnSuccess( success -> this.logInfo( GlobalConstants.MSG_FLOW_OK, createExtraInfo(success) ))
	            .doOnCancel( () -> this.logInfo( GlobalConstants.MSG_FLOW_CANCEL, GlobalConstants.MSG_NODATA) )
	            .doOnError( error -> this.logInfo( GlobalConstants.MSG_FLOW_ERROR, error.toString()) )
		        .doFinally( signal -> this.logInfo( GlobalConstants.MSG_FLOW_RESULT, signal.toString()) );					
	}
	
	private void logInfo(String action, String extraInfo) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, action, extraInfo);

	}
	
	private <T> String createExtraInfo(T response) {
		
		return response instanceof Number? GlobalConstants.MSG_MODLINES + response.toString():response.toString();
				
	}	

	private <T> void logFlowStep(T data){
		
		 logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_FLOW_PROCESS, data!=null?data.toString():"");			
	}
	
}
