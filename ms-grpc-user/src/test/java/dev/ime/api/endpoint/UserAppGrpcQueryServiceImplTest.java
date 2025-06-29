package dev.ime.api.endpoint;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.google.protobuf.Empty;

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.UserAppDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetUserAppRequest;
import dev.proto.PaginationRequest;
import dev.proto.UserAppProto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserAppGrpcQueryServiceImplTest {

	@Mock
	private QueryServicePort<UserAppDto> queryService;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private DtoValidator dtoValidator;

	@InjectMocks
	private UserAppGrpcQueryServiceImpl userAppGrpcQueryServiceImpl;	

	private UserAppDto userAppDto;
	private UserAppProto userAppProto;
	private PaginationRequest paginationRequest;	
	private GetUserAppRequest getUserAppRequest;
	
	private final UUID userId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	@BeforeEach
	private void setUp(){		

		userAppDto = new UserAppDto(userId, email, name, lastname);

		userAppProto = UserAppProto.newBuilder()
				.setUserAppId(userId.toString())
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.USERAPP_EMAIL)
				.setSortDir(GlobalConstants.PS_D)
				.build();
		
		getUserAppRequest = GetUserAppRequest.newBuilder()
				.setUserAppId(userId.toString())
				.build();
	}
		
	@Test
	void listUsers_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(userAppDto));
		Mockito.when(userAppMapper.fromListUserAppDtoToListUserAppProto(Mockito.anyList())).thenReturn(List.of(userAppProto));
	
		StepVerifier
		.create(userAppGrpcQueryServiceImpl.listUsers(Empty.getDefaultInstance()))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getUsersCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(userAppMapper).fromListUserAppDtoToListUserAppProto(Mockito.anyList());	
	}
	
	@Test
	void llistUsersPaginated_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(userAppDto));
		Mockito.when(userAppMapper.fromListUserAppDtoToListUserAppProto(Mockito.anyList())).thenReturn(List.of(userAppProto));
	
		StepVerifier
		.create(userAppGrpcQueryServiceImpl.listUsersPaginated(paginationRequest))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getUsersCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(userAppMapper).fromListUserAppDtoToListUserAppProto(Mockito.anyList());	
	}

	@Test
	void getUser_shouldReturnUserAppProto() {

		Mockito.doNothing().when(dtoValidator).validateGetUserAppRequest(Mockito.any(GetUserAppRequest.class));
		Mockito.when(queryService.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(userAppDto));
		Mockito.when(userAppMapper.fromUserAppDtoToUserAppProto(Mockito.any(UserAppDto.class)))
				.thenReturn(userAppProto);

		StepVerifier.create(userAppGrpcQueryServiceImpl.getUser(getUserAppRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getUserAppId()).isEqualTo(userId.toString()));
		}).verifyComplete();

		Mockito.verify(queryService).findById(Mockito.any(UUID.class));
		Mockito.verify(userAppMapper).fromUserAppDtoToUserAppProto(Mockito.any(UserAppDto.class));
	}
	
}
