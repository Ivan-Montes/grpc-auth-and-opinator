package dev.ime.domain.port.inbound;

import java.util.UUID;

import dev.ime.domain.model.Event;
import reactor.core.publisher.Mono;

public interface CommandServicePort<T> {

	Mono<Event> create(T dto);
	Mono<Event> update(T dto);
	Mono<Event> deleteById(UUID id);	
}
