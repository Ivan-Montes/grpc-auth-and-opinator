package dev.ime.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.CategoryDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.command.CreateCategoryCommand;
import dev.ime.application.usecases.command.DeleteCategoryCommand;
import dev.ime.application.usecases.command.UpdateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandDispatcher;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;

@Service
public class CategoryCommandService implements CommandServicePort<CategoryDto> {

	private final CommandDispatcher commandDispatcher;
	private final PublisherPort publisherPort;
	private static final Logger logger = LoggerFactory.getLogger(CategoryCommandService.class);

	public CategoryCommandService(@Qualifier("categoryCommandDispatcher")CommandDispatcher commandDispatcher, PublisherPort publisherPort) {
		super();
		this.commandDispatcher = commandDispatcher;
		this.publisherPort = publisherPort;
	}

	@Override
	public Mono<Event> create(CategoryDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new CreateCategoryCommand(UUID.randomUUID(), dto.categoryName()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> update(CategoryDto dto) {

		return Mono.justOrEmpty(dto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new UpdateCategoryCommand(dto.categoryId(), dto.categoryName()))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	@Override
	public Mono<Event> deleteById(UUID id) {

		return Mono.justOrEmpty(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_REQUIRED))))						
				.map(item -> new DeleteCategoryCommand(id))
				.flatMap(this::runHandler)
				.flatMap(this::processEvents);	
	}

	private Mono<Event> runHandler(Command command){
		
		return Mono.just(command)
				.map(commandDispatcher::getCommandHandler)
				.flatMap( handler -> handler.handle(command));		
	}
	
	private Mono<Event> processEvents(Event event) {
		
	    return Mono.just(event)	
	        .doOnNext(eventItem -> logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.EVENT_CAT, eventItem))
	    .flatMap(eventItem -> publisherPort.publishEvent(event).thenReturn(event))
        .thenReturn(event);
	}
	
}
