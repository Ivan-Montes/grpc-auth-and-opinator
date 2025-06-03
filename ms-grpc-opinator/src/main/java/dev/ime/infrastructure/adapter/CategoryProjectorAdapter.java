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
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.utils.MapExtractorHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.BaseProjectorPort;
import dev.ime.domain.port.outbound.ExtendedProjectorPort;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("categoryProjectorAdapter")
public class CategoryProjectorAdapter implements BaseProjectorPort, ExtendedProjectorPort {

	private final SessionFactory sessionFactory;
	private final MapExtractorHelper mapExtractorHelper;
	private static final Logger logger = LoggerFactory.getLogger(CategoryProjectorAdapter.class);

	public CategoryProjectorAdapter(SessionFactory sessionFactory, MapExtractorHelper mapExtractorHelper) {
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
				.flatMap(this::validateNameAvailability)
				.flatMap(this::insertAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<CategoryJpaEntity> createJpaEntity(Map<String, Object> eventData) {
		
		return Mono.fromCallable( () -> {
			
			UUID catId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.CAT_ID);
	        String name = mapExtractorHelper.extractString(eventData, GlobalConstants.CAT_NAME, GlobalConstants.PATTERN_NAME_FULL);

			return CategoryJpaEntity
		    		.builder()
		    		.categoryId(catId)
		    		.categoryName(name)
		    		.build();
			
		}).onErrorMap(e -> new CreateJpaEntityException(Map.of( GlobalConstants.CAT_CAT, e.getMessage() )));
		
	}

	private Mono<CategoryJpaEntity> validateNameAvailability(CategoryJpaEntity entity) {

		String query = """
				SELECT
				 COUNT(c) 
				FROM
				 CategoryJpaEntity c 
				WHERE
				 c.categoryName = :name
				""";
		
		return sessionFactory
				.withSession(session -> session
						.createQuery(query, Long.class)
						.setParameter("name", entity.getCategoryName()).getSingleResult().map(count -> count == 0))
				.convert().with(UniReactorConverters.toMono()).filter(b -> b)
				.switchIfEmpty(Mono
						.error(new UniqueValueException(Map.of(GlobalConstants.CAT_NAME, entity.getCategoryName()))))
				.thenReturn(entity);
	}

	private Mono<CategoryJpaEntity> insertAction(CategoryJpaEntity entity) {

		return sessionFactory
		.withTransaction((session, tx) -> session.persist(entity)
				.replaceWith(entity))
		.convert().with(UniReactorConverters.toMono());
	}
	
	@Override
	public Mono<Void> update(Event event) {

		return  Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntity)
				.doOnNext(this::logFlowStep)
				.flatMap(this::validateNameAvailabilityInUpdateAction)
				.flatMap(this::updateAction)	
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();	
	}

	private Mono<CategoryJpaEntity> validateNameAvailabilityInUpdateAction(CategoryJpaEntity entity) {

		String query = """
				SELECT
				 COUNT(c) 
				FROM
				 CategoryJpaEntity c 
				WHERE
				 c.categoryName = :name AND
				 c.categoryId != :categoryId
				""";
		
		return sessionFactory
				.withSession(session -> session
						.createQuery(query, Long.class)
						.setParameter("name", entity.getCategoryName())
						.setParameter(GlobalConstants.CAT_ID, entity.getCategoryId())
						.getSingleResult().map(count -> count == 0))
				.convert().with(UniReactorConverters.toMono()).filter(b -> b)
				.switchIfEmpty(Mono
						.error(new UniqueValueException(Map.of(GlobalConstants.CAT_NAME, entity.getCategoryName()))))
				.thenReturn(entity);
	}

	private Mono<CategoryJpaEntity> updateAction(CategoryJpaEntity entity) {
	
		String query = """
				SELECT
				 c 
				FROM
				 CategoryJpaEntity c 
				WHERE
				 c.categoryId = :categoryId
				""";
		
		return sessionFactory
		.withTransaction((session, tx) -> session.createQuery(query, CategoryJpaEntity.class)
				.setParameter(GlobalConstants.CAT_ID, entity.getCategoryId())
				.getSingleResult()
	            .flatMap(item -> {
	            	item.setCategoryName(entity.getCategoryName());
	                return session.merge(item);
	            }))
				.convert().with(UniReactorConverters.toMono());	
	}
	
	@Override
	public Mono<Void> deleteById(Event event) {
		
		return Mono.justOrEmpty(event.getEventData())
				.doOnNext(this::logFlowStep)
				.map(evenData -> evenData.get(GlobalConstants.CAT_ID))
				.cast(String.class)
				.map(UUID::fromString)
				.doOnNext(this::logFlowStep)
				.flatMap(this::checkEntityAssociation)
				.flatMap(this::deleteByIdAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.CAT_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<UUID> checkEntityAssociation(UUID categoryId){
		
		String query = """
				SELECT
				 COUNT(*)
				FROM
				 products p 
				WHERE
				 p.category_id = :categoryId
				""";		
		
		return sessionFactory
				.withSession(session -> session.createNativeQuery(query, Long.class)
						.setParameter(GlobalConstants.CAT_ID, categoryId)
						.getSingleResult())
						.convert().with(UniReactorConverters.toMono())
						.filter( i -> i == 0)
						.switchIfEmpty(Mono.error(new EntityAssociatedException(Map.of(GlobalConstants.CAT_ID, categoryId.toString()))))
						.thenReturn(categoryId);
	}

	private Mono<CategoryJpaEntity> deleteByIdAction(UUID entityId) {
		
	    return sessionFactory.withTransaction((session, tx) -> session.find(CategoryJpaEntity.class, entityId)
	            .onItem().ifNull().failWith(() -> new ResourceNotFoundException(
	                Map.of(GlobalConstants.CAT_ID, entityId.toString())))
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
