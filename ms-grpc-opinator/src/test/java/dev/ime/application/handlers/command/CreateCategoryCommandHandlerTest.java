package dev.ime.application.handlers.command;

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

import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.CreateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.CategorySpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateCategoryCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private CategorySpecificReadRepositoryPort categorySpecificReadRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private CreateCategoryCommandHandler createCategoryCommandHandler;

	private Event event;
	private CreateCategoryCommand createCommand;

	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.CAT_CAT;
	private final String eventType = GlobalConstants.CAT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		eventData.put(GlobalConstants.CAT_NAME, categoryName);
		
		createCommand = new CreateCategoryCommand(categoryId, categoryName);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(categorySpecificReadRepository.existsByName(Mockito.anyString())).thenReturn(Mono.just(false));
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(createCategoryCommandHandler.handle(createCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(categorySpecificReadRepository).existsByName(Mockito.anyString());
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithExistedName_shouldReturnError() {

		Mockito.when(categorySpecificReadRepository.existsByName(Mockito.anyString())).thenReturn(Mono.just(true));

		StepVerifier
		.create(createCategoryCommandHandler.handle(createCommand))
		.expectError(UniqueValueException.class)
		.verify();

		Mockito.verify(categorySpecificReadRepository).existsByName(Mockito.anyString());
	}

}
