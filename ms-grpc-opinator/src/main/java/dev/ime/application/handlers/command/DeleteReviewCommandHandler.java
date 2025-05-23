package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.EntityAssociatedException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.DeleteReviewCommand;
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
public class DeleteReviewCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Review> readRepository;
	private final JwtUtil jwtUtil;
	private final EventMapper eventMapper;
	
	public DeleteReviewCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Review> readRepository, JwtUtil jwtUtil, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.readRepository = readRepository;
		this.jwtUtil = jwtUtil;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(DeleteReviewCommand.class)
				.flatMap(this::validateDelete)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.DELETE_REV, ""))));	
	}

	private Mono<DeleteReviewCommand> validateDelete(DeleteReviewCommand command) {
	    
		return Mono.just(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::checkJwtTokenOwner)
				.flatMap(this::checkEntityAssociation)				
				.thenReturn(command);		
	}

	private Mono<Review> validateIdExists(DeleteReviewCommand command) {

		return readRepository.findById(command.reviewId()).switchIfEmpty(Mono
				.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_ID, command.reviewId().toString()))));
	}

	private Mono<Review> checkJwtTokenOwner(Review review) {

		return jwtUtil.getJwtTokenFromContext().filter(m -> m.equals(review.getEmail()))
				.switchIfEmpty(Mono.error(new JwtTokenEmailRestriction(Map.of(GlobalConstants.REV_ID,
						review.getReviewId().toString(), GlobalConstants.USERAPP_EMAIL, review.getEmail()))))
				.thenReturn(review);
	}

	private Mono<Review> checkEntityAssociation(Review entity){
		
		return Mono.justOrEmpty(entity.getVotes())
				.filter( i -> i.isEmpty())
				.switchIfEmpty(Mono.error(new EntityAssociatedException(Map.of(GlobalConstants.REV_ID, entity.getReviewId().toString()))))
				.thenReturn(entity);
	}

	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_DELETED, command);
	}	
		
}
