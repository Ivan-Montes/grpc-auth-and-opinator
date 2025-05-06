package dev.ime.application.handlers;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.EmailNotChageException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.UpdateUserAppCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class UpdateUserAppCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<UserApp> readRepository;
	private final UserAppMapper userAppMapper;
	private final JwtUtil jwtUtil;

	public UpdateUserAppCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<UserApp> readRepository, UserAppMapper userAppMapper, JwtUtil jwtUtil) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.readRepository = readRepository;
		this.userAppMapper = userAppMapper;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Mono<Event> handle(Command command) {
		
		return Mono.justOrEmpty(command)
				.ofType(UpdateUserAppCommand.class)
				.flatMap(this::validateUpdate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.UPDATE_USERAPP, ""))));	
	}

	private Mono<UpdateUserAppCommand> validateUpdate(UpdateUserAppCommand command) {
		
		return Mono.just(command)
				.flatMap(this::validateIdExists)
				.flatMap( u -> checkEmailNotChange(u, command))
				.flatMap(this::checkJwtTokenOwner)
				.thenReturn(command);		
	}

	private Mono<UserApp> validateIdExists(UpdateUserAppCommand command) {

		return readRepository.findById(command.userAppId())
				.switchIfEmpty(Mono.error(
				new ResourceNotFoundException(Map.of(GlobalConstants.USERAPP_ID, command.userAppId().toString()))));
	}

	private Mono<UserApp> checkEmailNotChange(UserApp userApp, UpdateUserAppCommand command) {
		
		return Mono.justOrEmpty(userApp)
		.filter( u -> u.getEmail().equals(command.email()))
		.switchIfEmpty(Mono.error( new EmailNotChageException(Map.of(GlobalConstants.USERAPP_CAT, String.valueOf(userApp)))));
	}

	private Mono<UserApp> checkJwtTokenOwner(UserApp userApp) {

		return jwtUtil.getJwtTokenFromContext()
				.filter( m -> m.equals(userApp.getEmail()))
				.switchIfEmpty(Mono.error( new JwtTokenEmailRestriction(Map.of(GlobalConstants.USERAPP_EMAIL, userApp.getEmail()))))
				.thenReturn(userApp);
	}
	
	private Event createEvent(Command command) {		
		
		return userAppMapper.createEvent(command, GlobalConstants.USERAPP_UPDATED);
	}

}
