package dev.ime.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.ReviewDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.CreateReviewCommand;
import dev.ime.application.usecases.command.DeleteReviewCommand;
import dev.ime.application.usecases.command.UpdateReviewCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandDispatcher;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;

@Service
public class ReviewCommandService implements CommandServicePort<ReviewDto> {

	private final CommandDispatcher commandDispatcher;
	private final PublisherPort publisherPort;
	private final JwtUtil jwtUtil;
	private static final Logger logger = LoggerFactory.getLogger(ReviewCommandService.class);

	public ReviewCommandService(@Qualifier("reviewCommandDispatcher")CommandDispatcher commandDispatcher, PublisherPort publisherPort,
			JwtUtil jwtUtil) {
		super();
		this.commandDispatcher = commandDispatcher;
		this.publisherPort = publisherPort;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Mono<Event> create(ReviewDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_REQUIRED))))						
				.flatMap(this::createCommand)
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	private Mono<CreateReviewCommand> createCommand(ReviewDto dto) {

		return jwtUtil.getJwtTokenFromContext()
				.map(email -> new CreateReviewCommand(UUID.randomUUID(), email,
				dto.productId(), dto.reviewText(), dto.rating()));
	}

	@Override
	public Mono<Event> update(ReviewDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new UpdateReviewCommand(dto.reviewId(), dto.reviewText(), dto.rating()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> deleteById(UUID id) {

		return Mono.justOrEmpty(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new DeleteReviewCommand(id))
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
