package dev.ime.application.handlers;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.EmailUsedException;
import dev.ime.application.usecases.CreateUserAppCommand;
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
public class CreateUserAppCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<UserApp> readRepository;
	private final UserAppMapper userAppMapper;

	public CreateUserAppCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<UserApp> readRepository, UserAppMapper userAppMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.readRepository = readRepository;
		this.userAppMapper = userAppMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {
		
		return Mono.justOrEmpty(command)
				.ofType(CreateUserAppCommand.class)
				.flatMap(this::validateEntityAlreadyInDb)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.CREATE_USERAPP, ""))));	
	}

	private Mono<CreateUserAppCommand> validateEntityAlreadyInDb(CreateUserAppCommand command) {
	    return readRepository.countByEmail(command.email())
	        .flatMap(count -> {
	            if (count > 0) {
	                return Mono.error(new EmailUsedException(Map.of(GlobalConstants.USERAPP_EMAIL, command.email())));
	            }
	            return Mono.just(command);
	        });
	}

	private Event createEvent(Command command) {		
		
		return userAppMapper.createEvent(command, GlobalConstants.USERAPP_CREATED);
	}

}
