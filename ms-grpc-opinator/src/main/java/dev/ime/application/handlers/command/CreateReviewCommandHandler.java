package dev.ime.application.handlers.command;

import java.util.Map;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.CreateEventException;
import dev.ime.application.exception.OnlyOneReviewPerUserInProductException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.CreateReviewCommand;
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
public class CreateReviewCommandHandler implements CommandHandler {

	private final EventWriteRepositoryPort eventWriteRepository;
	private final ReadRepositoryPort<Product> productRepository;
	private final EventMapper eventMapper;
	
	public CreateReviewCommandHandler(EventWriteRepositoryPort eventWriteRepository,
			ReadRepositoryPort<Product> productRepository, EventMapper eventMapper) {
		super();
		this.eventWriteRepository = eventWriteRepository;
		this.productRepository = productRepository;
		this.eventMapper = eventMapper;
	}
	
	@Override
	public Mono<Event> handle(Command command) {

		return Mono.justOrEmpty(command)
				.ofType(CreateReviewCommand.class)
				.flatMap(this::validateCreate)
				.map(this::createEvent)
				.flatMap(eventWriteRepository::save)
				.switchIfEmpty(Mono.error(new CreateEventException(Map.of(GlobalConstants.CREATE_REV, ""))));	
	}
	
	private Mono<CreateReviewCommand> validateCreate(CreateReviewCommand command) {
		
		return Mono.justOrEmpty(command)
				.flatMap(this::checkExistsProduct)
				.flatMap( product -> this.validateOnlyOneReviewRestricction(command, product))
				.thenReturn(command);
	}
	
	private Mono<Product> checkExistsProduct(CreateReviewCommand command) {

		return productRepository.findById(command.productId()).switchIfEmpty(Mono
				.error(new ResourceNotFoundException(Map.of(GlobalConstants.PROD_ID, command.productId().toString()))));
	}

	private Mono<Product> validateOnlyOneReviewRestricction(CreateReviewCommand command, Product product) {

		return Mono.justOrEmpty(command.email())
				.filter(m -> product.getReviews().stream().noneMatch(r -> m.equals(r.getEmail())))
				.switchIfEmpty(Mono.error(new OnlyOneReviewPerUserInProductException(
						Map.of(GlobalConstants.PROD_ID, product.getProductId().toString()))))
				.thenReturn(product);
	}
	
	private Event createEvent(Command command) {		
		return eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_CREATED, command);
	}	
	
}
