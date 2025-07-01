package dev.ime.application.handlers;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.ValidationException;
import dev.ime.application.usecases.DeleteUserAppCommand;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.EventWriteRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class DeleteUserAppCommandHandlerTest {

	@Mock
	private EventWriteRepositoryPort eventWriteRepository;
	@Mock
	private ReadRepositoryPort<UserApp> readRepository;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private DeleteUserAppCommandHandler deleteUserAppCommandHandler;

	private DeleteUserAppCommand deleteCommand;
	private UserApp userApp;

	private final UUID userAppId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	private final String email2 = "mail@mail.coom";

	
	@BeforeEach
	private void setUp() {	

		deleteCommand = new DeleteUserAppCommand(userAppId);		
		
		userApp = new UserApp(userAppId, email, name, lastname);	

	}

	@Test
	void handle_shouldReturnEvent() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp));	
		Mockito.when(jwtUtil.getJwtTokenFromContext()).thenReturn(Mono.just(email));

		StepVerifier
		.create(deleteUserAppCommandHandler.handle(deleteCommand))
		.expectError(ValidationException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));	
		Mockito.verify(jwtUtil).getJwtTokenFromContext();		
	}

	@Test
	void handle_WithUnknownUser_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.empty());
		
		StepVerifier
		.create(deleteUserAppCommandHandler.handle(deleteCommand))
		.expectError(ResourceNotFoundException.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

	@Test
	void handle_WithTokenOwnerDifferent_shouldReturnError() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp));
		Mockito.when(jwtUtil.getJwtTokenFromContext()).thenReturn(Mono.just(email2));

		StepVerifier
		.create(deleteUserAppCommandHandler.handle(deleteCommand))
		.expectError(JwtTokenEmailRestriction.class)
		.verify();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

}
