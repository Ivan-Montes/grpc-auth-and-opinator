package dev.ime.application.service;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import dev.ime.application.dispatcher.QueryDispatcher;
import dev.ime.application.dto.UserAppDto;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

	@Mock
	private QueryDispatcher queryDispatcher;
	@Mock
	private UserAppMapper userAppMapper;
	
	@InjectMocks
	private QueryService queryService;

	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private UserApp userApp01;
	private UserApp userApp02;
	private UserAppDto userAppDto01;
	private UserAppDto userAppDto02;

	private final UUID userAppId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final String name01 = "name";
	private final String lastname01 = "lastname";
	
	private final UUID userAppId02 = UUID.randomUUID();
	private final String email02 = "email@email.tk";
	private final String name02 = "name";
	private final String lastname02 = "lastname";

	@BeforeEach
	private void setUp(){		
		
		userApp01 = new UserApp(userAppId01, email01, name01, lastname01);		
		userApp02 = new UserApp(userAppId02, email02, name02, lastname02);
		userAppDto01 = new UserAppDto(userAppId01, email01, name01, lastname01);
		userAppDto02 = new UserAppDto(userAppId02, email02, name02, lastname02);
		
	}
		
	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFluxMultiple() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Flux.just(userApp01,userApp02));
		Mockito.when(userAppMapper.fromDomainToDto(Mockito.any(UserApp.class))).thenReturn(userAppDto01, userAppDto02);
		
		StepVerifier
		.create(queryService.findAll(pageRequest))
		.expectNext(userAppDto01, userAppDto02)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(userAppMapper, Mockito.times(2)).fromDomainToDto(Mockito.any(UserApp.class));		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMonoWithDto() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Mono.just(userApp01));
		Mockito.when(userAppMapper.fromDomainToDto(Mockito.any(UserApp.class))).thenReturn(userAppDto01);
		
		StepVerifier
		.create(queryService.findById(userAppId01))
		.expectNext(userAppDto01)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(userAppMapper).fromDomainToDto(Mockito.any(UserApp.class));		
	}

}
