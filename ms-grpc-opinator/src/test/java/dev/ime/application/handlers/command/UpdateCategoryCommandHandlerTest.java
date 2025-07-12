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

import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.application.usecases.command.UpdateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.CategorySpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<Category> categoryRepository;
	@Mock
	private CategorySpecificReadRepositoryPort categorySpecificReadRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private UpdateCategoryCommandHandler updateCategoryCommandHandler;

	private Event event;
	private UpdateCategoryCommand updateCommand;

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
		
		updateCommand = new UpdateCategoryCommand(categoryId, categoryName);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	@Test
	void handle_shouldReturnEvent() {
		
		Mockito.when(categoryRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(new Category()));
		Mockito.when(categorySpecificReadRepository.isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString())).thenReturn(Mono.just(true));
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(updateCategoryCommandHandler.handle(updateCommand))
		.expectNext(event)
		.verifyComplete();
		
		Mockito.verify(categoryRepository).findById(Mockito.any(UUID.class));
		Mockito.verify(categorySpecificReadRepository).isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString());
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_UnknownResource_shouldReturnError() {
		
		Mockito.when(categoryRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());

		StepVerifier
		.create(updateCategoryCommandHandler.handle(updateCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(categoryRepository).findById(Mockito.any(UUID.class));
	}

	@Test
	void handle_WithExistedName_shouldReturnError() {
		
		Mockito.when(categoryRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(new Category()));
		Mockito.when(categorySpecificReadRepository.isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString())).thenReturn(Mono.just(false));

		StepVerifier
		.create(updateCategoryCommandHandler.handle(updateCommand))
		.expectError(UniqueValueException.class)
		.verify();

		Mockito.verify(categoryRepository).findById(Mockito.any(UUID.class));
		Mockito.verify(categorySpecificReadRepository).isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString());
	}

}
