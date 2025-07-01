package dev.ime.application.handlers;

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

import dev.ime.application.exception.EmailUsedException;
import dev.ime.application.usecases.CreateUserAppCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CreateUserAppCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<UserApp> readRepository;
	@Mock
	private UserAppMapper userAppMapper;

	@InjectMocks
	private CreateUserAppCommandHandler createUserAppCommandHandler;

	private Event event;
	private CreateUserAppCommand createCommand;

	private final UUID userAppId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.USERAPP_CAT;
	private final String eventType = GlobalConstants.USERAPP_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.USERAPP_ID, userAppId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.USERAPP_NAME, name);
		eventData.put(GlobalConstants.USERAPP_LASTNAME, lastname);
		
		createCommand = new CreateUserAppCommand(userAppId, email, name, lastname);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);

	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.countByEmail(Mockito.anyString())).thenReturn(Mono.just(0L));
		Mockito.when(userAppMapper.createEvent(Mockito.any(Command.class), Mockito.anyString())).thenReturn(event);		
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(createUserAppCommandHandler.handle(createCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(readRepository).countByEmail(Mockito.anyString());
		Mockito.verify(userAppMapper).createEvent(Mockito.any(Command.class), Mockito.anyString());
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithRepeatedEmail_shouldReturnError() {

		Mockito.when(readRepository.countByEmail(Mockito.anyString())).thenReturn(Mono.just(1L));

		StepVerifier
		.create(createUserAppCommandHandler.handle(createCommand))
		.expectError(EmailUsedException.class)
		.verify();

		Mockito.verify(readRepository).countByEmail(Mockito.anyString());		
	}	
	
}
