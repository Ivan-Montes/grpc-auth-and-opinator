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

import dev.ime.application.exception.EmailNotChageException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.usecases.UpdateUserAppCommand;
import dev.ime.application.utils.JwtUtil;
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
class UpdateUserAppCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<UserApp> readRepository;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private UpdateUserAppCommandHandler updateUserAppCommandHandler;

	private UpdateUserAppCommand updateCommand;
	private UserApp userApp;
	private Event event;

	private final UUID userAppId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	private final String email2 = "mail@mail.coom";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.USERAPP_CAT;
	private final String eventType = GlobalConstants.USERAPP_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp() {	

		updateCommand = new UpdateUserAppCommand(userAppId, email, name, lastname);		
		
		userApp = new UserApp(userAppId, email, name, lastname);	
		
		eventData = new HashMap<>();
		eventData.put(GlobalConstants.USERAPP_ID, userAppId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.USERAPP_NAME, name);
		eventData.put(GlobalConstants.USERAPP_LASTNAME, lastname);		

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp));	
		Mockito.when(jwtUtil.getJwtTokenFromContext()).thenReturn(Mono.just(email));
		Mockito.when(userAppMapper.createEvent(Mockito.any(Command.class), Mockito.anyString())).thenReturn(event);	
		Mockito.when(eventWriteRepository.save(Mockito.any(Event.class))).thenReturn(Mono.just(event));

		StepVerifier
		.create(updateUserAppCommandHandler.handle(updateCommand))
		.expectNext(event)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));	
		Mockito.verify(jwtUtil).getJwtTokenFromContext();		
		Mockito.verify(userAppMapper).createEvent(Mockito.any(Command.class), Mockito.anyString());
		Mockito.verify(eventWriteRepository).save(Mockito.any(Event.class));
	}

	@Test
	void handle_WithUnknownUser_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(updateUserAppCommandHandler.handle(updateCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

	@Test
	void handle_WithEmailDifferent_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp));
		userApp.setEmail(email2);
		
		StepVerifier
		.create(updateUserAppCommandHandler.handle(updateCommand))
		.expectError(EmailNotChageException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

	@Test
	void handle_WithTokenOwnerDifferent_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp));
		Mockito.when(jwtUtil.getJwtTokenFromContext()).thenReturn(Mono.just(email2));

		StepVerifier
		.create(updateUserAppCommandHandler.handle(updateCommand))
		.expectError(JwtTokenEmailRestriction.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}
	
}
