package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.EntityAssociatedException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.DeleteProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;

@Component
public class DeleteProductCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Product> readRepository;
	private final EventMapper eventMapper;
	
	public DeleteProductCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Product> readRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.readRepository = readRepository;
		this.eventMapper = eventMapper;
	}

	@Override
	public Mono<Event> handle(Command command) {
		
		return Mono.justOrEmpty(command)
				.ofType(DeleteProductCommand.class)
				.flatMap(this::validateDelete)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.DELETE_PROD, ""))));	
	}

	private Mono<DeleteProductCommand> validateDelete(DeleteProductCommand command) {
	    
		return Mono.just(command)
				.flatMap(this::validateIdExists)
				.flatMap(this::checkEntityAssociation)				
				.thenReturn(command);		
	}

	private Mono<Product> validateIdExists(DeleteProductCommand command) {

		return readRepository.findById(command.productId())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.PROD_ID, command.productId().toString()))));
	}

	private Mono<Product> checkEntityAssociation(Product entity){
		
		return Mono.justOrEmpty(entity.getReviews())
				.filter( i -> i.isEmpty())
				.switchIfEmpty(Mono.error(new EntityAssociatedException(Map.of(GlobalConstants.PROD_ID, entity.getProductId().toString()))))
				.thenReturn(entity);
	}
	
	private Event createEvent(Command command) {		
		
		return eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_DELETED, command);
	}
	
}
