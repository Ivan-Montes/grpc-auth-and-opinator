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
import dev.ime.application.usecases.command.DeleteProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DeleteProductCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<Product> readRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private DeleteProductCommandHandler deleteProductCommandHandler;

	private Event event;
	private DeleteProductCommand deleteCommand;
	private Product product;

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
		
		deleteCommand = new DeleteProductCommand(productId);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
		
		product = new Product();
		product.setProductId(productId);
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(product));	
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(deleteProductCommandHandler.handle(deleteCommand))
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
		.create(deleteProductCommandHandler.handle(deleteCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));	
	}

}
