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
import dev.ime.application.usecases.command.DeleteCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<Category> readRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private DeleteCategoryCommandHandler deleteCategoryCommandHandler;

	private Event event;
	private DeleteCategoryCommand deleteCommand;
	private Category category;
	
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
		
		deleteCommand = new DeleteCategoryCommand(categoryId);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
		
		category = new Category();
		category.setCategoryId(categoryId);
		
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(category));	
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(deleteCategoryCommandHandler.handle(deleteCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));	
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithUnknown_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());	

		StepVerifier
		.create(deleteCategoryCommandHandler.handle(deleteCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));	
	}

}
