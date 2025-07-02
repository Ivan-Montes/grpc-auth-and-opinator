package dev.ime.application.handlers;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.usecases.GetByIdUserAppQuery;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetByIdUserAppQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<UserApp> readRepository;

	@InjectMocks
	private GetByIdUserAppQueryHandler getByIdUserAppQueryHandler;
	
	private GetByIdUserAppQuery getByIdQuery;
	private UserApp userApp01;

	private final UUID userAppId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final String name01 = "name";
	private final String lastname01 = "lastname";
	
	@BeforeEach
	private void setUp() {	
		
		getByIdQuery = new GetByIdUserAppQuery(userAppId01);

		userApp01 = new UserApp(userAppId01, email01, name01, lastname01);		
	}
	
	@Test
	void handle_shouldReturnDomainObject() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userApp01));
		
		StepVerifier
		.create(getByIdUserAppQueryHandler.handle(getByIdQuery))
		.expectNext(userApp01)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

}
