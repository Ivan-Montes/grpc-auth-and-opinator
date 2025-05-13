package dev.ime.infrastructure.adapter;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import reactor.core.publisher.Mono;

@Repository
public class EventMongoWriteRepositoryAdapter implements EventWriteRepositoryPort {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
	private final UserAppMapper userAppMapper;
	
	public EventMongoWriteRepositoryAdapter(ReactiveMongoTemplate reactiveMongoTemplate, UserAppMapper userAppMapper) {
		super();
		this.reactiveMongoTemplate = reactiveMongoTemplate;
		this.userAppMapper = userAppMapper;
	}

	@Override
	public Mono<Event> save(Event event) {

		return Mono.fromSupplier( () -> userAppMapper.fromEventDomainToEventMongo(event))
				.flatMap(reactiveMongoTemplate::save)
				.map(userAppMapper::fromEventMongoToEventDomain);			
	}

}
