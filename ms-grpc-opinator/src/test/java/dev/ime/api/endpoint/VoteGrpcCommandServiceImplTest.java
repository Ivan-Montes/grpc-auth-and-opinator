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

import com.google.protobuf.BoolValue;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.VoteDto;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.UpdateVoteRequest;
import dev.proto.VoteProto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class VoteGrpcCommandServiceImplTest {

	@Mock
	private CommandServicePort<VoteDto> commandService;
	@Mock
	private VoteMapper mapper;
	@Mock
	private VoteRequestValidator requestValidator;

	@InjectMocks
	private VoteGrpcCommandServiceImpl voteGrpcCommandServiceImpl;

	private VoteDto voteDto;
	private VoteProto voteProto;
	private CreateVoteRequest createRequest;	
	private UpdateVoteRequest updateRequest;	
	private DeleteVoteRequest deleteRequest;
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
		
		voteProto = VoteProto.newBuilder()
				.setVoteId(voteId.toString())
				.setEmail(email)
				.setReviewId(reviewId.toString())
				.setUseful(BoolValue.of(useful))
				.build();

		createRequest = CreateVoteRequest.newBuilder()
				.setReviewId(reviewId.toString())
				.setUseful(useful)
				.build();

		updateRequest = UpdateVoteRequest.newBuilder()
				.setVoteId(voteId.toString())
				.setUseful(useful)
				.build();

		deleteRequest = DeleteVoteRequest.newBuilder()
				.setVoteId(voteId.toString())
				.build();
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);		
	}

	@Test
	void create_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateVoteRequest.class));
		Mockito.when(mapper.fromCreateToDto(Mockito.any(CreateVoteRequest.class))).thenReturn(voteDto);
		Mockito.when(commandService.create(Mockito.any(VoteDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(voteProto);

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(voteGrpcCommandServiceImpl.createVote(createRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response).isEqualTo(voteProto));
					}).verifyComplete();

			Mockito.verify(mapper).fromCreateToDto(Mockito.any(CreateVoteRequest.class));
			Mockito.verify(commandService).create(Mockito.any(VoteDto.class));
			Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
		}
	}

	@Test
	void update_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateVoteRequest.class));
		Mockito.when(mapper.fromUpdateToDto(Mockito.any(UpdateVoteRequest.class)))
				.thenReturn(voteDto);
		Mockito.when(commandService.update(Mockito.any(VoteDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(voteProto);

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(voteGrpcCommandServiceImpl.updateVote(updateRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response).isEqualTo(voteProto));
					}).verifyComplete();

			Mockito.verify(mapper).fromUpdateToDto(Mockito.any(UpdateVoteRequest.class));
			Mockito.verify(commandService).update(Mockito.any(VoteDto.class));
			Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
		}
	}

	@Test
	void deleteById_shouldReturnDeleteResponse() {

		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteVoteRequest.class));
		Mockito.when(commandService.deleteById(Mockito.any(UUID.class))).thenReturn(Mono.just(event));

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(voteGrpcCommandServiceImpl.deleteVote(deleteRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response.hasSuccess()).isTrue());
					}).verifyComplete();

			Mockito.verify(commandService).deleteById(Mockito.any(UUID.class));
		}
	}
	
}
