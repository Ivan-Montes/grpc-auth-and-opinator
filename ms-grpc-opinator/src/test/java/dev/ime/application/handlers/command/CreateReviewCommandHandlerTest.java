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
import dev.ime.application.usecases.command.CreateReviewCommand;
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
class CreateReviewCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<Product> productRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private CreateReviewCommandHandler createReviewCommandHandler;

	private Event event;
	private CreateReviewCommand createCommand;
	private Product product;

	private final UUID reviewId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID productId = UUID.randomUUID();
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.REV_CAT;
	private final String eventType = GlobalConstants.REV_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.REV_ID, reviewId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.PROD_ID, productId.toString());
		eventData.put(GlobalConstants.REV_TXT, reviewText);
		eventData.put(GlobalConstants.REV_RAT, rating);
		
		createCommand = new CreateReviewCommand(reviewId, email, productId, reviewText, rating);		

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

		Mockito.when(productRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(product));
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(createReviewCommandHandler.handle(createCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(productRepository).findById(Mockito.any(UUID.class));
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithExistedName_shouldReturnError() {

		Mockito.when(productRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());

		StepVerifier
		.create(createReviewCommandHandler.handle(createCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(productRepository).findById(Mockito.any(UUID.class));
	}

}
