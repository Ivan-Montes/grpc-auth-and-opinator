package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.CreateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.CategorySpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class CreateCategoryCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final CategorySpecificReadRepositoryPort categorySpecificReadRepository;
	private final EventMapper eventMapper;
	
	public CreateCategoryCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			CategorySpecificReadRepositoryPort categorySpecificReadRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.categorySpecificReadRepository = categorySpecificReadRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(CreateCategoryCommand.class)
				.flatMap(this::checkExistsByName)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.CREATE_CAT, ""))));	
	}

	private Mono<CreateCategoryCommand> checkExistsByName(CreateCategoryCommand command) {
	    return categorySpecificReadRepository.existsByName(command.categoryName())
	    		.filter(b -> !b)
	    		.switchIfEmpty(Mono.error(new UniqueValueException(Map.of(GlobalConstants.CAT_NAME, command.categoryName()))))
	    		.thenReturn(command);
	}

	private Event createEvent(Command command) {		
		
		return eventMapper.createEvent(GlobalConstants.CAT_CAT, GlobalConstants.CAT_CREATED, command);
	}

}
