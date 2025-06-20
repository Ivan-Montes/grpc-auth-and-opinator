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
import dev.ime.infrastructure.entity.ProductJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("productProjectorAdapter")
public class ProductProjectorAdapter implements BaseProjectorPort, ExtendedProjectorPort {

	private final SessionFactory sessionFactory;
	private final MapExtractorHelper mapExtractorHelper;
	private static final Logger logger = LoggerFactory.getLogger(ProductProjectorAdapter.class);
	
	public ProductProjectorAdapter(SessionFactory sessionFactory, MapExtractorHelper mapExtractorHelper) {
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
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.PROD_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}
	
	private Mono<ProductJpaEntity> createJpaEntity(Map<String, Object> eventData) {
		
	    return Mono.defer(() -> {
	        UUID productId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.PROD_ID);
	        String productName = mapExtractorHelper.extractString(eventData, GlobalConstants.PROD_NAME, GlobalConstants.PATTERN_NAME_FULL);
	        String productDescription = mapExtractorHelper.extractString(eventData, GlobalConstants.PROD_DESC, GlobalConstants.PATTERN_NAME_FULL);
	        UUID categoryId = mapExtractorHelper.extractUuid(eventData, GlobalConstants.CAT_ID);

	        return findCategoryById(categoryId)
	                .map(category -> ProductJpaEntity.builder()
	                        .productId(productId)
	                        .productName(productName)
	                        .productDescription(productDescription)
	                        .category(category)
	                        .build())
	                .onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.PROD_CAT, e.getMessage())));
	    });
	}
	
	private Mono<CategoryJpaEntity> findCategoryById(UUID id) {

		String queryString = """
				SELECT
				 c
				FROM
				 CategoryJpaEntity c
				WHERE categoryId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, CategoryJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_ID,id.toString()))));
	}

	private Mono<ProductJpaEntity> validateNameAvailability(ProductJpaEntity entity) {

		String query = """
				SELECT
				 COUNT(p) 
				FROM
				 ProductJpaEntity p 
				WHERE
				 p.productName = :name
				""";
		
		return sessionFactory
				.withSession(session -> session
						.createQuery(query, Long.class)
						.setParameter("name", entity.getProductName()).getSingleResult().map(count -> count == 0))
				.convert().with(UniReactorConverters.toMono()).filter(b -> b)
				.switchIfEmpty(Mono.error(new UniqueValueException(
						Map.of(GlobalConstants.PROD_NAME, entity.getProductName()))))
				.thenReturn(entity);
	}

	private Mono<ProductJpaEntity> insertAction(ProductJpaEntity entity) {

		return sessionFactory
		.withTransaction((session, tx) -> session.persist(entity)
				.replaceWith(entity))
		.convert().with(UniReactorConverters.toMono());
	}
	
	@Override
	public Mono<Void> update(Event event) {

		return Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntity)
				.doOnNext(this::logFlowStep)
				.flatMap(this::validateNameAvailabilityInUpdateAction)
				.flatMap(this::updateAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.PROD_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<ProductJpaEntity> validateNameAvailabilityInUpdateAction(ProductJpaEntity entity) {

		String query = """
				SELECT
				 COUNT(p)
				FROM
				 ProductJpaEntity p
				WHERE
				 p.productName = :productName AND
				 p.productId != :productId
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(query, Long.class)
						.setParameter("productName", entity.getProductName())
						.setParameter(GlobalConstants.PROD_ID, entity.getProductId()).getSingleResult()
						.map(count -> count == 0))
				.convert().with(UniReactorConverters.toMono()).filter(b -> b)
				.switchIfEmpty(Mono.error(new UniqueValueException(
						Map.of(GlobalConstants.PROD_NAME, entity.getProductName()))))
				.thenReturn(entity);
	}

	private Mono<ProductJpaEntity> updateAction(ProductJpaEntity entity) {

		String query = """
				SELECT
				 p 
				FROM
				 ProductJpaEntity p 
				WHERE
				 p.productId = :productId
				""";
		
		return sessionFactory
		.withTransaction((session, tx) -> session.createQuery(query, ProductJpaEntity.class)
				.setParameter(GlobalConstants.PROD_ID, entity.getProductId())
				.getSingleResult()
                .flatMap(item -> {
                	item.setProductName(entity.getProductName());
                	item.setProductDescription(entity.getProductDescription());
                	item.setCategory(entity.getCategory());
                    return session.merge(item);
                }))
				.convert().with(UniReactorConverters.toMono());	
	}
	
	@Override
	public Mono<Void> deleteById(Event event) {

		return Mono.justOrEmpty(event.getEventData())
				.doOnNext(this::logFlowStep)
				.map(evenData -> evenData.get(GlobalConstants.PROD_ID))
				.cast(String.class)
				.map(UUID::fromString)
				.doOnNext(this::logFlowStep)
				.flatMap(this::checkEntityAssociation)
				.flatMap(this::deleteByIdAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.PROD_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<UUID> checkEntityAssociation(UUID productId){
		
		String query = """
				SELECT
				 COUNT(*)
				FROM
				 reviews r 
				WHERE
				 r.product_id = :productId
				""";		
		
		return sessionFactory
				.withSession(session -> session.createNativeQuery(query, Long.class)
						.setParameter(GlobalConstants.PROD_ID, productId)
						.getSingleResult())
						.convert().with(UniReactorConverters.toMono())
						.filter( i -> i == 0)
						.switchIfEmpty(Mono.error(new EntityAssociatedException(Map.of(GlobalConstants.PROD_ID, productId.toString()))))
						.thenReturn(productId);
	}
	
	private Mono<ProductJpaEntity> deleteByIdAction(UUID entityId) {
		
	    return sessionFactory.withTransaction((session, tx) -> session.find(ProductJpaEntity.class, entityId)
	            .onItem().ifNull().failWith(() -> new ResourceNotFoundException(
	                Map.of(GlobalConstants.PROD_ID, entityId.toString())))
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
