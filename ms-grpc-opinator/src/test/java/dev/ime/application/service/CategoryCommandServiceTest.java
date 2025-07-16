package dev.ime.application.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandDispatcher;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {

	@Mock
	private CommandDispatcher commandDispatcher;
	@Mock
	private PublisherPort publisherPort;
	
	@InjectMocks
	private CategoryCommandService categoryCommandService;	

	@Mock
	private CommandHandler handler;
	private CategoryDto categoryDto;
	private Event event;

	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.CAT_CAT;
	private final String eventType = GlobalConstants.CAT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {
		
		categoryDto = new CategoryDto(categoryId,categoryName);
		
		event = new Event(
				eventId, 
				eventCategory, 
				eventType, 
				eventTimestamp, 
				eventData);
	}

	@Test
	void create_shouldReturnEvent() {

		Mockito.when(commandDispatcher.getCommandHandler(Mockito.any(Command.class))).thenReturn(handler);
		Mockito.when(handler.handle(Mockito.any(Command.class))).thenReturn(Mono.just(event));
		Mockito.when(publisherPort.publishEvent(Mockito.any(Event.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(categoryCommandService.create(categoryDto))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(commandDispatcher).getCommandHandler(Mockito.any(Command.class));
		Mockito.verify(handler).handle(Mockito.any(Command.class));
		Mockito.verify(publisherPort).publishEvent(Mockito.any(Event.class));
	}

	@Test
	void update_shouldReturnEvent() {

		Mockito.when(commandDispatcher.getCommandHandler(Mockito.any(Command.class))).thenReturn(handler);
		Mockito.when(handler.handle(Mockito.any(Command.class))).thenReturn(Mono.just(event));
		Mockito.when(publisherPort.publishEvent(Mockito.any(Event.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(categoryCommandService.update(categoryDto))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(commandDispatcher).getCommandHandler(Mockito.any(Command.class));
		Mockito.verify(handler).handle(Mockito.any(Command.class));
		Mockito.verify(publisherPort).publishEvent(Mockito.any(Event.class));
	}

	@Test
	void deleteById_shouldReturnEvent() {

		Mockito.when(commandDispatcher.getCommandHandler(Mockito.any(Command.class))).thenReturn(handler);
		Mockito.when(handler.handle(Mockito.any(Command.class))).thenReturn(Mono.just(event));
		Mockito.when(publisherPort.publishEvent(Mockito.any(Event.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(categoryCommandService.deleteById(categoryId))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(commandDispatcher).getCommandHandler(Mockito.any(Command.class));
		Mockito.verify(handler).handle(Mockito.any(Command.class));
		Mockito.verify(publisherPort).publishEvent(Mockito.any(Event.class));
	}

}
