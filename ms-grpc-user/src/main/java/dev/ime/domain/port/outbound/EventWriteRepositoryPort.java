package dev.ime.domain.port.outbound;

import dev.ime.domain.model.Event;
import reactor.core.publisher.Mono;

public interface EventWriteRepositoryPort {
	
	Mono<Event> save(Event event);

}
