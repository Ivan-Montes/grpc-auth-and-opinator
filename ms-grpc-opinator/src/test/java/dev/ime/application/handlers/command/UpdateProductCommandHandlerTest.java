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
import dev.ime.application.usecases.command.UpdateProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ProductSpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UpdateProductCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ProductSpecificReadRepositoryPort productSpecificReadRepository;
	@Mock
	private ReadRepositoryPort<Object> readRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private UpdateProductCommandHandler updateProductCommandHandler;

	private Event event;
	private UpdateProductCommand updateCommand;

	private final UUID productId = UUID.randomUUID();
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final UUID categoryId = UUID.randomUUID();

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.PROD_CAT;
	private final String eventType = GlobalConstants.PROD_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.PROD_ID, productId.toString());
		eventData.put(GlobalConstants.PROD_NAME, productName);
		eventData.put(GlobalConstants.PROD_DESC, productDescription);
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		
		updateCommand = new UpdateProductCommand(productId, productName, productDescription, categoryId);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(new Product())).thenReturn(Mono.just(new Category()));
		Mockito.when(productSpecificReadRepository.isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString())).thenReturn(Mono.just(true));
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(updateProductCommandHandler.handle(updateCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(readRepository, Mockito.times(2)).findById(Mockito.any(UUID.class));
		Mockito.verify(productSpecificReadRepository).isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString());
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_UnknownResource_shouldReturnError() {
		
		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());

		StepVerifier
		.create(updateProductCommandHandler.handle(updateCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));
	}

	@Test
	void handle_WithUnavailableName_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(new Product()));
		Mockito.when(productSpecificReadRepository.isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString())).thenReturn(Mono.just(false));

		StepVerifier
		.create(updateProductCommandHandler.handle(updateCommand))
		.expectError(UniqueValueException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));
		Mockito.verify(productSpecificReadRepository).isAvailableByIdAndName(Mockito.any(UUID.class),Mockito.anyString());
	}

}
