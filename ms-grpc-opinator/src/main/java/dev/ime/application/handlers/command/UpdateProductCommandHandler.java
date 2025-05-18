package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.UpdateProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ProductSpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class UpdateProductCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ProductSpecificReadRepositoryPort productSpecificReadRepository;
	private final ReadRepositoryPort<Product> productRepository;
	private final ReadRepositoryPort<Category> categoryRepository;
	private final EventMapper eventMapper;

	public UpdateProductCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ProductSpecificReadRepositoryPort productSpecificReadRepository,
			ReadRepositoryPort<Product> productRepository, ReadRepositoryPort<Category> categoryRepository,
			EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.productSpecificReadRepository = productSpecificReadRepository;
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {
		
		return Mono.justOrEmpty(command)
				.ofType(UpdateProductCommand.class)
				.flatMap(this::validateUpdate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.UPDATE_CAT, ""))));	
	}

	private Mono<UpdateProductCommand> validateUpdate(UpdateProductCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::isAvailableByIdAndName)
				.flatMap(this::checkExistsCategory)
				.thenReturn(command);
	}

	private Mono<UpdateProductCommand> validateIdExists(UpdateProductCommand command) {

		return productRepository.findById(command.productId())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(
						Map.of(GlobalConstants.PROD_ID, command.productId().toString()))))
	    		.thenReturn(command);
	}

	private Mono<UpdateProductCommand> isAvailableByIdAndName(UpdateProductCommand command) {
		return productSpecificReadRepository.isAvailableByIdAndName(command.productId(), command.productName())
	    		.filter(b -> b)
	    		.switchIfEmpty(Mono.error(new UniqueValueException(
	    				Map.of(GlobalConstants.PROD_NAME, command.productName()))))
	    		.thenReturn(command);
	}

	private Mono<UpdateProductCommand> checkExistsCategory(UpdateProductCommand command) {
	    return categoryRepository.findById(command.categoryId())
	    		.switchIfEmpty(Mono.error(new ResourceNotFoundException(
	    				Map.of(GlobalConstants.CAT_ID, command.categoryId().toString()))))
	    		.thenReturn(command);
	}

	private Event createEvent(Command command) {			
		return eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_UPDATED, command);
	}
	
}
