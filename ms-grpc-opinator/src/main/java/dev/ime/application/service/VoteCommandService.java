package dev.ime.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.VoteDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.CreateVoteCommand;
import dev.ime.application.usecases.command.DeleteVoteCommand;
import dev.ime.application.usecases.command.UpdateVoteCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandDispatcher;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;

@Service
public class VoteCommandService implements CommandServicePort<VoteDto> {

	private final CommandDispatcher commandDispatcher;
	private final PublisherPort publisherPort;
	private final JwtUtil jwtUtil;
	private static final Logger logger = LoggerFactory.getLogger(VoteCommandService.class);

	public VoteCommandService(@Qualifier("voteCommandDispatcher")CommandDispatcher commandDispatcher, PublisherPort publisherPort, JwtUtil jwtUtil) {
		super();
		this.commandDispatcher = commandDispatcher;
		this.publisherPort = publisherPort;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Mono<Event> create(VoteDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.flatMap(this::createCommand)
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	private Mono<CreateVoteCommand> createCommand(VoteDto dto) {

		return jwtUtil.getJwtTokenFromContext()
				.map(email -> new CreateVoteCommand(UUID.randomUUID(), email,
				dto.reviewId(), dto.useful()));
	}

	@Override
	public Mono<Event> update(VoteDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new UpdateVoteCommand(dto.voteId(), dto.useful()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> deleteById(UUID id) {

		return Mono.justOrEmpty(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new DeleteVoteCommand(id))
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
