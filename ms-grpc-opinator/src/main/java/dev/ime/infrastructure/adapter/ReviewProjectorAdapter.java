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
import dev.ime.application.exception.EntityAssociatedException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.utils.MapExtractorHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.BaseProjectorPort;
import dev.ime.domain.port.outbound.ExtendedProjectorPort;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("reviewProjectorAdapter")
public class ReviewProjectorAdapter implements BaseProjectorPort, ExtendedProjectorPort {

	private final SessionFactory sessionFactory;
	private final MapExtractorHelper mapExtractorHelper;
	private static final Logger logger = LoggerFactory.getLogger(ReviewProjectorAdapter.class);
	
	public ReviewProjectorAdapter(SessionFactory sessionFactory, MapExtractorHelper mapExtractorHelper) {
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

	private Mono<ReviewJpaEntity> createJpaEntity(Map<String, Object> eventData) {
		
	    return Mono.defer(() -> {
	        UUID reviewId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.REV_ID);
	        String email = mapExtractorHelper.extractString(eventData, GlobalConstants.USERAPP_EMAIL, GlobalConstants.PATTERN_EMAIL);
	        UUID productId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.PROD_ID);
	        String reviewText = mapExtractorHelper.extractString(eventData, GlobalConstants.REV_TXT, GlobalConstants.PATTERN_DESC_FULL);
	        int rating = Integer.parseInt(mapExtractorHelper.extractString(eventData, GlobalConstants.REV_RAT, "\\d"));
	        
	        return findProductById(productId)
	                .map(product -> ReviewJpaEntity.builder()
	                        .reviewId(reviewId)
	                        .email(email)
	                        .product(product)
	                        .reviewText(reviewText)
	                        .rating(rating)
	                        .build())
	                .onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.REV_CAT, e.getMessage())));
	    });
	}
	
	private Mono<ProductJpaEntity> findProductById(UUID id) {

		String queryString = """
				SELECT
				 p
				FROM
				 ProductJpaEntity p
				WHERE productId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, ProductJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.PROD_ID,id.toString()))));
	}

	private Mono<ReviewJpaEntity> insertAction(ReviewJpaEntity entity) {

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
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<ReviewJpaEntity> createJpaEntityForUpdateAction(Map<String, Object> eventData) {
		
	    return Mono.fromCallable(() -> {
	    	
	        UUID reviewId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.REV_ID);
	        String reviewText = mapExtractorHelper.extractString(eventData, GlobalConstants.REV_TXT, GlobalConstants.PATTERN_DESC_FULL);
	        int rating = Integer.parseInt(mapExtractorHelper.extractString(eventData, GlobalConstants.REV_RAT, "\\d"));
	        
	        return ReviewJpaEntity.builder()
	                        .reviewId(reviewId)
	                        .reviewText(reviewText)
	                        .rating(rating)
	                        .build();
	        
	    }).onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.REV_CAT, e.getMessage())));
	
	}
	
	private Mono<ReviewJpaEntity> updateAction(ReviewJpaEntity entity) {

		String query = """
				SELECT
				 r 
				FROM
				 ReviewJpaEntity r 
				WHERE
				 r.reviewId = :reviewId
				""";
		
		return sessionFactory
		.withTransaction((session, tx) -> session.createQuery(query, ReviewJpaEntity.class)
				.setParameter(GlobalConstants.REV_ID, entity.getReviewId())
				.getSingleResult()
                .flatMap(item -> {
                	item.setReviewText(entity.getReviewText());
                	item.setRating(entity.getRating());
                    return session.merge(item);
                }))
				.convert().with(UniReactorConverters.toMono());	
	}
	
	@Override
	public Mono<Void> deleteById(Event event) {

		return Mono.justOrEmpty(event.getEventData())
				.doOnNext(this::logFlowStep)
				.map(evenData -> evenData.get(GlobalConstants.REV_ID))
				.cast(String.class)
				.map(UUID::fromString)
				.doOnNext(this::logFlowStep)
				.flatMap(this::checkEntityAssociation)
				.flatMap(this::deleteByIdAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.REV_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<UUID> checkEntityAssociation(UUID reviewId){
		
		String query = """
				SELECT
				 COUNT(*)
				FROM
				 votes v 
				WHERE
				 v.review_id = :reviewId
				""";		
		
		return sessionFactory
				.withSession(session -> session.createNativeQuery(query, Long.class)
						.setParameter(GlobalConstants.REV_ID, reviewId)
						.getSingleResult())
						.convert().with(UniReactorConverters.toMono())
						.filter( i -> i == 0)
						.switchIfEmpty(Mono.error(new EntityAssociatedException(Map.of(GlobalConstants.REV_ID, reviewId.toString()))))
						.thenReturn(reviewId);
	}

	private Mono<ReviewJpaEntity> deleteByIdAction(UUID entityId) {
		
	    return sessionFactory.withTransaction((session, tx) -> session.find(ReviewJpaEntity.class, entityId)
	            .onItem().ifNull().failWith(() -> new ResourceNotFoundException(
	                Map.of(GlobalConstants.REV_ID, entityId.toString())))
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
