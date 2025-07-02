package dev.ime.application.handlers;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.ime.application.usecases.GetAllUserAppQuery;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllUserAppQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<UserApp> readRepository;

	@InjectMocks
	private GetAllUserAppQueryHandler getAllUserAppQueryHandler;
	
	private GetAllUserAppQuery getAllQuery;
	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private UserApp userApp01;
	private UserApp userApp02;

	private final UUID userAppId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final String name01 = "name";
	private final String lastname01 = "lastname";
	
	private final UUID userAppId02 = UUID.randomUUID();
	private final String email02 = "noemail@email.tk";
	private final String name02 = "noname";
	private final String lastname02 = "nolastname";
	
	@BeforeEach
	private void setUp() {	
		
		getAllQuery = new GetAllUserAppQuery(pageRequest);

		userApp01 = new UserApp(userAppId01, email01, name01, lastname01);		
		userApp02 = new UserApp(userAppId02, email02, name02, lastname02);
	}
	
	@Test
	void handle_shouldReturnFlux() {

		Mockito.when(readRepository.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(userApp01, userApp02));
		
		StepVerifier
		.create(getAllUserAppQueryHandler.handle(getAllQuery))
		.expectNext(userApp01)
		.expectNext(userApp02)
		.verifyComplete();

		Mockito.verify(readRepository).findAll(Mockito.any(Pageable.class));		
	}

}
