package dev.ime.infrastructure.adapter;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import reactor.core.publisher.Mono;

@Repository
public class EventMongoWriteRepositoryAdapter implements EventWriteRepositoryPort {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final EventMapper eventMapper;
	
	public EventMongoWriteRepositoryAdapter(ReactiveMongoTemplate reactiveMongoTemplate, EventMapper eventMapper) {
		super();
		this.reactiveMongoTemplate = reactiveMongoTemplate;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> save(Event event) {

		return Mono.fromSupplier( () -> eventMapper.fromEventDomainToEventMongo(event))
				.flatMap(reactiveMongoTemplate::save)
				.map(eventMapper::fromEventMongoToEventDomain);			
	}

}
