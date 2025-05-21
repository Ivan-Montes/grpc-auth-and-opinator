package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.UpdateVoteCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class UpdateVoteCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Vote> voteRepository;
	private final JwtUtil jwtUtil;
	private final EventMapper eventMapper;
	
	public UpdateVoteCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Vote> voteRepository, JwtUtil jwtUtil, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.voteRepository = voteRepository;
		this.jwtUtil = jwtUtil;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(UpdateVoteCommand.class)
				.flatMap(this::validateUpdate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.UPDATE_VOT, ""))));	
	}

	private Mono<UpdateVoteCommand> validateUpdate(UpdateVoteCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::checkJwtTokenOwner)
				.thenReturn(command);
	}

	private Mono<Vote> validateIdExists(UpdateVoteCommand command) {

		return voteRepository.findById(command.voteId()).switchIfEmpty(Mono
				.error(new ResourceNotFoundException(Map.of(GlobalConstants.VOT_ID, command.voteId().toString()))));
	}

	private Mono<Vote> checkJwtTokenOwner(Vote vote) {

		return jwtUtil.getJwtTokenFromContext().filter(m -> m.equals(vote.getEmail()))
				.switchIfEmpty(Mono.error(new JwtTokenEmailRestriction(Map.of(GlobalConstants.VOT_ID,
						vote.getVoteId().toString(), GlobalConstants.USERAPP_EMAIL, vote.getEmail()))))
				.thenReturn(vote);
	}
	
	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.VOT_CAT, GlobalConstants.VOT_UPDATED, command);
	}	
	
}
