package dev.ime.infrastructure.adapter;

import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.application.exception.PublishEventException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Service
public class KafkaPublisherAdapter implements PublisherPort {
	
	private final ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaTemplate;
	private static final Logger logger = LoggerFactory.getLogger(KafkaPublisherAdapter.class);

	public KafkaPublisherAdapter(ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaTemplate) {
		super();
		this.reactiveKafkaTemplate = reactiveKafkaTemplate;
	}	

	@Transactional("kafkaTransactionManager")
    public Mono<Void> publishEvent(Event event) {
    	
		return Mono.justOrEmpty(event)
				.switchIfEmpty(Mono.error( new PublishEventException(Map.of(GlobalConstants.MSG_PUBLISH_FAIL, GlobalConstants.MSG_NODATA ))))						
		        .doOnNext(item -> logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_EVENT, item.toString()))
		        .map( eventItem -> SenderRecord.create(new ProducerRecord<String, Object>(event.getEventType(), event), null))				
				.flatMap(reactiveKafkaTemplate::send)
				.doOnSuccess(this::handleSuccess)
	            .onErrorMap( ex -> new PublishEventException(Map.of(GlobalConstants.MSG_PUBLISH_FAIL, ex.getMessage())))
		    	.doOnError(this::handleFailure)
				.doFinally(signalType -> logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_END, signalType.toString()))
	            .then();		
    }
	
    private void handleSuccess(SenderResult<Object> result) {
    	
    	logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_OK, prepareSuccessMsg(result));

    }

	private String prepareSuccessMsg(SenderResult<Object> result) {
		return Optional.ofNullable(result)
    	.map(SenderResult::recordMetadata)
    	.map(RecordMetadata::topic)
    	.orElse(GlobalConstants.MSG_NODATA);
	}

	private void handleFailure(Throwable ex) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_FAIL, ex.getMessage());
   
	}
}
