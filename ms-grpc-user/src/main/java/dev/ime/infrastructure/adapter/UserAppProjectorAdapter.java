package dev.ime.infrastructure.adapter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;

import dev.ime.application.exception.CreateJpaEntityException;
import dev.ime.application.exception.EmailUsedException;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.BaseProjectorPort;
import dev.ime.domain.port.outbound.ExtendedProjectorPort;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("userAppProjectorAdapter")
public class UserAppProjectorAdapter implements BaseProjectorPort, ExtendedProjectorPort {

	private final R2dbcEntityTemplate r2dbcEntityTemplate;
	private static final Logger logger = LoggerFactory.getLogger(UserAppProjectorAdapter.class);
	
	public UserAppProjectorAdapter(R2dbcEntityTemplate r2dbcEntityTemplate) {
		super();
		this.r2dbcEntityTemplate = r2dbcEntityTemplate;
	}

	@Override
	public Mono<Void> create(Event event) {
		
		return Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntity)
				.doOnNext(this::logFlowStep)
				.flatMap(this::validateEmailAvailability)
				.flatMap(this::insertAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();
	}

	private Mono<UserAppJpaEntity> createJpaEntity(Map<String, Object> eventData) {
		
		return Mono.fromCallable( () -> {
			
			UUID userAppId = extractUuid(eventData, GlobalConstants.USERAPP_ID);
	        String email = extractString(eventData, GlobalConstants.USERAPP_EMAIL, GlobalConstants.PATTERN_EMAIL);
	        String name = extractString(eventData, GlobalConstants.USERAPP_NAME, GlobalConstants.PATTERN_NAME_FULL);
	        String lastname = extractString(eventData, GlobalConstants.USERAPP_LASTNAME, GlobalConstants.PATTERN_NAME_FULL);

			return UserAppJpaEntity
		    		.builder()
		    		.userAppId(userAppId)
		    		.email(email)
		    		.name(name)
		    		.lastname(lastname)
		    		.build();
			
		}).onErrorMap(e -> new CreateJpaEntityException(Map.of( GlobalConstants.USERAPP_CAT, e.getMessage() )));
		
	}

	private UUID extractUuid(Map<String, Object> eventData, String key) {
		
	    return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .map(UUID::fromString)
	                   .orElseThrow(() -> new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key)));	    
	}

	private String extractString(Map<String, Object> eventData, String key, String patternConstraint) {
		
		String value = Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse("");
	    
	    Pattern compiledPattern = Pattern.compile(patternConstraint);
	    Matcher matcher = compiledPattern.matcher(value);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key, GlobalConstants.OBJ_VALUE, value));
	    }

	    return value;	    
	}
	
	private Mono<UserAppJpaEntity> validateEmailAvailability(UserAppJpaEntity entity) {
	    
		return r2dbcEntityTemplate.selectOne(
				Query.query(Criteria.where(GlobalConstants.USERAPP_EMAIL).is(entity.getEmail())
						.and(GlobalConstants.USERAPP_ID_DB).not(entity.getUserAppId())),
				UserAppJpaEntity.class)				
				.flatMap( entityFound -> Mono.error(new EmailUsedException(Map.of(GlobalConstants.USERAPP_EMAIL, entityFound.getEmail()))))
				.then(Mono.just(entity));		
	}

    private Mono<UserAppJpaEntity> insertAction(UserAppJpaEntity entity) {
    	
        return r2dbcEntityTemplate.insert(entity);
        
    }

	@Override
	public Mono<Void> update(Event event) {
		
		return  Mono.justOrEmpty(event.getEventData())	
				.doOnNext(this::logFlowStep)
				.flatMap(this::createJpaEntityForUpdateAction)
				.flatMap(this::updateAction)	
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog)
				.then();				
	}

	private Mono<UserAppJpaEntity> createJpaEntityForUpdateAction(Map<String, Object> eventData) {
		
		return Mono.fromCallable( () -> {
			
			UUID userAppId = extractUuid(eventData, GlobalConstants.USERAPP_ID);
	        String name = extractString(eventData, GlobalConstants.USERAPP_NAME, GlobalConstants.PATTERN_NAME_FULL);
	        String lastname = extractString(eventData, GlobalConstants.USERAPP_LASTNAME, GlobalConstants.PATTERN_NAME_FULL);

			return UserAppJpaEntity
		    		.builder()
		    		.userAppId(userAppId)
		    		.name(name)
		    		.lastname(lastname)
		    		.build();
			
		}).onErrorMap(e -> new CreateJpaEntityException(Map.of(GlobalConstants.USERAPP_CAT, e.getMessage() )));
		
	}

	private Mono<Long> updateAction(UserAppJpaEntity entity) {
		 
		return r2dbcEntityTemplate.update(
				Query.query(Criteria.where(GlobalConstants.USERAPP_ID_DB).is(entity.getUserAppId())),
				Update.update(GlobalConstants.USERAPP_NAME, entity.getName())
					  .set(GlobalConstants.USERAPP_LASTNAME, entity.getLastname()),
					  UserAppJpaEntity.class);		
	}
	
	@Override
	public Mono<Void> deleteById(Event event) {

		return Mono.justOrEmpty(event.getEventData())
				.doOnNext(this::logFlowStep)
				.map(evenData -> evenData.get(GlobalConstants.USERAPP_ID))
				.cast(String.class)
				.map(UUID::fromString)
				.doOnNext(this::logFlowStep)
				.flatMap(this::deleteByIdAction)
				.switchIfEmpty(Mono.error(new EmptyResponseException(
						Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.EX_EMPTYRESPONSE_DESC))))
				.transform(this::addFinalLog).then();
	}

	private Mono<Long> deleteByIdAction(UUID entityId) {
		
	    return r2dbcEntityTemplate.delete(
	    		Query.query(Criteria.where(GlobalConstants.USERAPP_ID_DB).is(entityId)),
	    		UserAppJpaEntity.class
	    		);
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
