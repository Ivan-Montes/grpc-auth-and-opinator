package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.UpdateReviewCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class UpdateReviewCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Review> reviewRepository;
	private final JwtUtil jwtUtil;
	private final EventMapper eventMapper;
	
	public UpdateReviewCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Review> reviewRepository, JwtUtil jwtUtil, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.reviewRepository = reviewRepository;
		this.jwtUtil = jwtUtil;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(UpdateReviewCommand.class)
				.flatMap(this::validateUpdate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.UPDATE_REV, ""))));	
	}

	private Mono<UpdateReviewCommand> validateUpdate(UpdateReviewCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::checkJwtTokenOwner)
				.thenReturn(command);
	}
	
	private Mono<Review> validateIdExists(UpdateReviewCommand command) {

		return reviewRepository.findById(command.reviewId())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(
						Map.of(GlobalConstants.REV_ID, command.reviewId().toString()))));
	}

	private Mono<Review> checkJwtTokenOwner(Review review) {

		return jwtUtil.getJwtTokenFromContext().filter(m -> m.equals(review.getEmail()))
				.switchIfEmpty(Mono.error(new JwtTokenEmailRestriction(Map.of(GlobalConstants.REV_ID,
						review.getReviewId().toString(), GlobalConstants.USERAPP_EMAIL, review.getEmail()))))
				.thenReturn(review);
	}
	
	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_UPDATED, command);
	}	
	
}
