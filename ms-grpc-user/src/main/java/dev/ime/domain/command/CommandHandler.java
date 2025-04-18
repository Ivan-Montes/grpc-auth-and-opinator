package dev.ime.domain.command;

import dev.ime.domain.model.Event;
import reactor.core.publisher.Mono;

public interface CommandHandler {

	Mono<Event> handle(Command command);
}
