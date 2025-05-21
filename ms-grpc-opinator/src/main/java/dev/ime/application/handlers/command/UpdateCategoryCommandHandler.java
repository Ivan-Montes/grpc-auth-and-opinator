package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.UpdateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.CategorySpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class UpdateCategoryCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Category> categoryRepository;
	private final CategorySpecificReadRepositoryPort categorySpecificReadRepository;
	private final EventMapper eventMapper;	

	public UpdateCategoryCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Category> categoryRepository,
			CategorySpecificReadRepositoryPort categorySpecificReadRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.categoryRepository = categoryRepository;
		this.categorySpecificReadRepository = categorySpecificReadRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {
		
		return Mono.justOrEmpty(command)
				.ofType(UpdateCategoryCommand.class)
				.flatMap(this::validateUpdate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(
						Map.of(GlobalConstants.UPDATE_CAT, ""))));	
	}

	private Mono<UpdateCategoryCommand> validateUpdate(UpdateCategoryCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::isAvailableByIdAndName)
				.thenReturn(command);
	}

	private Mono<UpdateCategoryCommand> validateIdExists(UpdateCategoryCommand command) {

		return categoryRepository.findById(command.categoryId())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(
						Map.of(GlobalConstants.CAT_ID, command.categoryId().toString()))))
	    		.thenReturn(command);
	}

	private Mono<UpdateCategoryCommand> isAvailableByIdAndName(UpdateCategoryCommand command) {
	    
		return categorySpecificReadRepository.isAvailableByIdAndName(command.categoryId(), command.categoryName())
	    		.filter(b -> b)
	    		.switchIfEmpty(Mono.error(new UniqueValueException(
	    				Map.of(GlobalConstants.CAT_NAME, command.categoryName()))))
	    		.thenReturn(command);
	}

	private Event createEvent(Command command) {		
		
		return eventMapper.createEvent(GlobalConstants.CAT_CAT, GlobalConstants.CAT_UPDATED, command);
	}
	
}
