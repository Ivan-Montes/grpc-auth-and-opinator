package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.OnlyOneVotePerUserInReviewException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.CreateVoteCommand;
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
public class CreateVoteCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Review> reviewRepository;
	private final EventMapper eventMapper;
	
	public CreateVoteCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Review> reviewRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.reviewRepository = reviewRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(CreateVoteCommand.class)
				.flatMap(this::validateCreate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.CREATE_VOT, ""))));	
	}

	private Mono<CreateVoteCommand> validateCreate(CreateVoteCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::checkExistsReview)
				.flatMap(review -> this.validateOnlyOneVoteRestricction(command, review))
				.thenReturn(command);
	}

	private Mono<Review> checkExistsReview(CreateVoteCommand command) {

		return reviewRepository.findById(command.reviewId()).switchIfEmpty(Mono
				.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_ID, command.reviewId().toString()))));
	}

	private Mono<Review> validateOnlyOneVoteRestricction(CreateVoteCommand command, Review review) {

		return Mono.justOrEmpty(command.email())
				.filter(m -> review.getVotes().stream().noneMatch(v -> m.equals(v.getEmail())))
				.switchIfEmpty(Mono.error(new OnlyOneVotePerUserInReviewException(
						Map.of(GlobalConstants.REV_ID, review.getReviewId().toString()))))
				.thenReturn(review);
	}

	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.VOT_CAT, GlobalConstants.VOT_CREATED, command);
	}	
	
}
