package dev.ime.api.endpoint;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateUserAppRequest;
import dev.proto.DeleteUserAppRequest;
import dev.proto.UpdateUserAppRequest;
import dev.proto.UserAppProto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserAppGrpcCommandServiceImplTest {

	@Mock
	private CommandServicePort<UserAppDto> commandService;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private DtoValidator dtoValidator;

	@InjectMocks
	private UserAppGrpcCommandServiceImpl userAppGrpcCommandServiceImpl;

	private UserAppProto userAppProto;
	private UserAppDto userAppDto;
	private CreateUserAppRequest createUserAppRequest;
	private UpdateUserAppRequest updateUserAppRequest;
	private DeleteUserAppRequest deleteUserAppRequest;
	private Event event;

	private final UUID userId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.USERAPP_CAT;
	private final String eventType = GlobalConstants.USERAPP_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp() {	

		userAppProto = UserAppProto.newBuilder()
				.setUserAppId(userId.toString())
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		userAppDto = new UserAppDto(userId, email, name, lastname);
		
		createUserAppRequest = CreateUserAppRequest.newBuilder()
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		updateUserAppRequest = UpdateUserAppRequest.newBuilder()
				.setUserAppId(userId.toString())
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		deleteUserAppRequest = DeleteUserAppRequest.newBuilder()
				.setUserAppId(userId.toString())
				.build();

		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);		
	}
		
	@Test
	void create_shouldReturnUserAppCreatedResponse() {

		Mockito.doNothing().when(dtoValidator).validateCreateUserAppRequest(Mockito.any(CreateUserAppRequest.class));
		Mockito.when(userAppMapper.fromCreateUserAppRequestToUserAppDto(Mockito.any(CreateUserAppRequest.class))).thenReturn(userAppDto);
		Mockito.when(commandService.create(Mockito.any(UserAppDto.class))).thenReturn(Mono.just(event));
		
		StepVerifier
		.create(userAppGrpcCommandServiceImpl.createUser(createUserAppRequest))
		.assertNext(response -> {
        	org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getResult()).isTrue()
					);
        	})
		.verifyComplete();		
		
		Mockito.verify(userAppMapper).fromCreateUserAppRequestToUserAppDto(Mockito.any(CreateUserAppRequest.class));
		Mockito.verify(commandService).create(Mockito.any(UserAppDto.class));
		
	}

	@Test
	void update_shouldReturnUserAppProto() {

		Mockito.doNothing().when(dtoValidator).validateUpdateUserAppRequest(Mockito.any(UpdateUserAppRequest.class));
		Mockito.when(userAppMapper.fromUpdateUserAppRequestToUserAppDto(Mockito.any(UpdateUserAppRequest.class)))
				.thenReturn(userAppDto);
		Mockito.when(commandService.update(Mockito.any(UserAppDto.class))).thenReturn(Mono.just(event));
		Mockito.when(userAppMapper.fromEventToUserAppProto(Mockito.any(Event.class))).thenReturn(userAppProto);

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(userAppGrpcCommandServiceImpl.updateUser(updateUserAppRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response).isEqualTo(userAppProto));
					}).verifyComplete();

			Mockito.verify(userAppMapper).fromUpdateUserAppRequestToUserAppDto(Mockito.any(UpdateUserAppRequest.class));
			Mockito.verify(commandService).update(Mockito.any(UserAppDto.class));
			Mockito.verify(userAppMapper).fromEventToUserAppProto(Mockito.any(Event.class));
		}
	}

	@Test
	void deleteById_shouldReturnDeleteUserAppResponse() {

		Mockito.doNothing().when(dtoValidator).validateDeleteUserAppRequest(Mockito.any(DeleteUserAppRequest.class));
		Mockito.when(commandService.deleteById(Mockito.any(UUID.class))).thenReturn(Mono.just(event));

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(userAppGrpcCommandServiceImpl.deleteUser(deleteUserAppRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response.getResult()).isTrue());
					}).verifyComplete();

			Mockito.verify(commandService).deleteById(Mockito.any(UUID.class));
		}
	}
	
}
