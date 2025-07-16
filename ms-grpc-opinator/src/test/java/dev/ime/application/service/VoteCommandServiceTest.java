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

import dev.ime.application.dto.VoteDto;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandDispatcher;
import dev.ime.domain.port.outbound.PublisherPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class VoteCommandServiceTest {

	@Mock
	private CommandDispatcher commandDispatcher;
	@Mock
	private PublisherPort publisherPort;
	@Mock
	private JwtUtil jwtUtil;
	
	@InjectMocks
	private VoteCommandService voteCommandService;

	@Mock
	private CommandHandler handler;

	private VoteDto voteDto;
	private Event event;
	
	private final UUID voteId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID reviewId = UUID.randomUUID();
	private final boolean useful = true;

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.VOT_CAT;
	private final String eventType = GlobalConstants.VOT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp(){
		
		voteDto = new VoteDto(voteId, email, reviewId, useful);
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);		
	}

	@Test
	void create_shouldReturnEvent() {

		Mockito.when(jwtUtil.getJwtTokenFromContext()).thenReturn(Mono.just(email));
		Mockito.when(commandDispatcher.getCommandHandler(Mockito.any(Command.class))).thenReturn(handler);
		Mockito.when(handler.handle(Mockito.any(Command.class))).thenReturn(Mono.just(event));
		Mockito.when(publisherPort.publishEvent(Mockito.any(Event.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(voteCommandService.create(voteDto))
		.expectNext(event)
		.verifyComplete();
		
		Mockito.verify(jwtUtil).getJwtTokenFromContext();
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
		.create(voteCommandService.update(voteDto))
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
		.create(voteCommandService.deleteById(voteId))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(commandDispatcher).getCommandHandler(Mockito.any(Command.class));
		Mockito.verify(handler).handle(Mockito.any(Command.class));
		Mockito.verify(publisherPort).publishEvent(Mockito.any(Event.class));
	}

}
