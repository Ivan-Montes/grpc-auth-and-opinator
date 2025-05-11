package dev.ime.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.ime.application.dispatcher.CommandDispatcher;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.CreateUserAppCommand;
import dev.ime.application.usecases.DeleteUserAppCommand;
import dev.ime.application.usecases.UpdateUserAppCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;

@Service
public class CommandService implements CommandServicePort<UserAppDto> {

	private final CommandDispatcher commandDispatcher;
	private final PublisherPort publisherPort;
	private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

	public CommandService(CommandDispatcher commandDispatcher, PublisherPort publisherPort) {
		super();
		this.commandDispatcher = commandDispatcher;
		this.publisherPort = publisherPort;
	}

	@Override
	public Mono<Event> create(UserAppDto dto) {
		
		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new CreateUserAppCommand(UUID.randomUUID(), dto.email(), dto.name(), dto.lastname()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> update(UserAppDto dto) {
		
		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new UpdateUserAppCommand(dto.userAppId(), dto.email(), dto.name(), dto.lastname()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> deleteById(UUID id) {
		
		return Mono.justOrEmpty(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.USERAPP_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new DeleteUserAppCommand(id))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	private Mono<Event> runHandler(Command command){
		
		return Mono.just(command)
				.map(commandDispatcher::getCommandHandler)
				.flatMap( handler -> handler.handle(command));		
	}
	
	private Mono<Event> processEvents(Event event) {
		
	    return Mono.just(event)	
	        .doOnNext(eventItem -> logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.EVENT_CAT, eventItem))
	    .flatMap(eventItem -> publisherPort.publishEvent(event).thenReturn(event))
        .thenReturn(event);
	}
	
}
