package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.CreateProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ProductSpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class CreateProductCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ProductSpecificReadRepositoryPort productSpecificReadRepository;
	private final ReadRepositoryPort<Category> categoryRepository;
	private final EventMapper eventMapper;
	
	public CreateProductCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ProductSpecificReadRepositoryPort productSpecificReadRepository,
			ReadRepositoryPort<Category> categoryRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.productSpecificReadRepository = productSpecificReadRepository;
		this.categoryRepository = categoryRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(CreateProductCommand.class)
				.flatMap(this::checkExistsByName)
				.flatMap(this::checkExistsCategory)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.CREATE_PROD, ""))));	
	}

	private Mono<CreateProductCommand> checkExistsByName(CreateProductCommand command) {
	    return productSpecificReadRepository.existsByName(command.productName())
	    		.filter(b -> !b)
	    		.switchIfEmpty(Mono.error(new UniqueValueException(Map.of(GlobalConstants.PROD_NAME, command.productName()))))
	    		.thenReturn(command);
	}

	private Mono<CreateProductCommand> checkExistsCategory(CreateProductCommand command) {
	    return categoryRepository.findById(command.categoryId())
	    		.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_ID, command.categoryId().toString()))))
	    		.thenReturn(command);
	}

	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_CREATED, command);
	}
	
}
