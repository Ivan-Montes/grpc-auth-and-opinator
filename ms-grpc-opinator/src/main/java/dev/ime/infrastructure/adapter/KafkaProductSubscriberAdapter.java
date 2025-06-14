package dev.ime.infrastructure.adapter;

import java.util.Map;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.SubscriberPort;
import dev.ime.domain.port.outbound.BaseProjectorPort;
import dev.ime.domain.port.outbound.ExtendedProjectorPort;
import reactor.core.publisher.Mono;

@Component
public class KafkaProductSubscriberAdapter implements SubscriberPort {

	private final BaseProjectorPort baseProjector;
	private final ExtendedProjectorPort extendedProjector;
	private final Map<String, Function<Event, Mono<?>>> actionsMap;	
	private static final Logger logger = LoggerFactory.getLogger(KafkaProductSubscriberAdapter.class);
	
	public KafkaProductSubscriberAdapter(@Qualifier("productProjectorAdapter")BaseProjectorPort baseProjector, @Qualifier("productProjectorAdapter")ExtendedProjectorPort extendedProjector
			) {
		super();
		this.baseProjector = baseProjector;
		this.extendedProjector = extendedProjector;
		this.actionsMap = initializeActionsMap();
	}

	private Map<String, Function<Event, Mono<?>>> initializeActionsMap() {

		return Map.of(
				GlobalConstants.PROD_CREATED, baseProjector::create, 
				GlobalConstants.PROD_UPDATED, extendedProjector::update, 
				GlobalConstants.PROD_DELETED, baseProjector::deleteById
				);
	}

	@KafkaListener(topics = {GlobalConstants.PROD_CREATED, GlobalConstants.PROD_UPDATED, GlobalConstants.PROD_DELETED}, groupId = "msgrpcopinator-consumer-product")
	@Override
	public Mono<Void> onMessage(ConsumerRecord<String, Event> consumerRecord) {

		return Mono.just(consumerRecord)
				.flatMap(this::validateTopic)
				.flatMap(this::validateValue)
				.doOnNext( c -> logger.info(GlobalConstants.MSG_PATTERN_INFO, c.topic(), c))				
				.flatMap(this::processEvent)
				.onErrorResume( error ->  Mono.fromRunnable(()-> logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_PLAIN, error.getMessage() != null? error.getMessage():GlobalConstants.EX_PLAIN)))
				.then();		
	}

	private Mono<ConsumerRecord<String, Event>> validateTopic(ConsumerRecord<String, Event> consumer ){
		
		return Mono.justOrEmpty(consumer.topic())
				.filter( topic -> !topic.trim().isEmpty() )
				.switchIfEmpty(Mono.error(new ValidationException(Map.of(this.getClass().getSimpleName(), GlobalConstants.EX_VALIDATION_DESC))))
				.thenReturn(consumer);
	}
	
	private Mono<ConsumerRecord<String, Event>> validateValue(ConsumerRecord<String, Event> consumer ){
		
		return Mono.justOrEmpty(consumer.value())
				.switchIfEmpty(Mono.error(new ValidationException(Map.of(this.getClass().getSimpleName(), GlobalConstants.EX_VALIDATION_DESC))))
				.thenReturn(consumer);
	}
	
    private Mono<Void> processEvent(ConsumerRecord<String, Event> consumer ){
    	
    	return Mono.justOrEmpty(consumer.topic())
    	.map(actionsMap::get)
		.switchIfEmpty(Mono.error(new IllegalHandlerException(Map.of(this.getClass().getSimpleName(), consumer.topic()))))
		.flatMap( function -> function.apply(consumer.value()))
    	.then();
    }
	
}
