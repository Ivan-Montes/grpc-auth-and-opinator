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
import dev.ime.application.usecases.command.CreateVoteCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateVoteCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<Review> reviewRepository;
	@Mock
	private EventMapper eventMapper;

	@InjectMocks
	private CreateVoteCommandHandler createVoteCommandHandler;

	private Event event;
	private CreateVoteCommand createCommand;
	private Review review;

	private final UUID voteId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID reviewId = UUID.randomUUID();
	private final boolean useful = true;

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.VOT_CAT;
	private final String eventType = GlobalConstants.VOT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.VOT_ID, voteId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.REV_ID, reviewId.toString());
		eventData.put(GlobalConstants.VOT_US, useful);
		
		createCommand = new CreateVoteCommand(voteId, email, reviewId, useful);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);		
		
		review = new Review();
		review.setReviewId(reviewId);
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(reviewRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(review));
		Mockito.when(eventMapper.createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class))).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(createVoteCommandHandler.handle(createCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(reviewRepository).findById(Mockito.any(UUID.class));
		Mockito.verify(eventMapper).createEvent(Mockito.anyString(), Mockito.anyString(), Mockito.any(Command.class));
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithExistedName_shouldReturnError() {

		Mockito.when(reviewRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());

		StepVerifier
		.create(createVoteCommandHandler.handle(createCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(reviewRepository).findById(Mockito.any(UUID.class));
	}

}
