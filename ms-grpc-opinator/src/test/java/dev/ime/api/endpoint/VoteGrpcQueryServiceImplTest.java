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

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.VoteDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetVoteRequest;
import dev.proto.PaginationRequest;
import dev.proto.VoteProto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class VoteGrpcQueryServiceImplTest {

	@Mock
	private QueryServicePort<VoteDto> queryService;
	@Mock
	private VoteMapper mapper;
	@Mock
	private VoteRequestValidator requestValidator;

	@InjectMocks
	private VoteGrpcQueryServiceImpl voteGrpcQueryServiceImpl;

	private VoteDto voteDto;
	private VoteProto voteProto;
	private PaginationRequest paginationRequest;	
	private GetVoteRequest getVoteRequest;
	
	private final UUID voteId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID reviewId = UUID.randomUUID();
	private final boolean useful = true;
	
	@BeforeEach
	private void setUp(){
		
		voteDto = new VoteDto(voteId, email, reviewId, useful);
		
		voteProto = VoteProto.newBuilder()
				.setVoteId(voteId.toString())
				.setEmail(email)
				.setReviewId(reviewId.toString())
				.setUseful(BoolValue.of(useful))
				.build();
		
		
		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.VOT_ID)
				.setSortDir(GlobalConstants.PS_D)
				.build();
		
		getVoteRequest = GetVoteRequest.newBuilder()
				.setVoteId(voteId.toString())
				.build();				
	}

	@Test
	void listVotes_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(voteDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(voteProto));
	
		StepVerifier
		.create(voteGrpcQueryServiceImpl.listVotes(Empty.getDefaultInstance()))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getVotesCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}
	
	@Test
	void listVotesPaginated_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(voteDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(voteProto));
	
		StepVerifier
		.create(voteGrpcQueryServiceImpl.listVotesPaginated(paginationRequest))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getVotesCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}

	@Test
	void getVote_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetVoteRequest.class));
		Mockito.when(queryService.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(voteDto));
		Mockito.when(mapper.fromDtoToProto(Mockito.any(VoteDto.class)))
				.thenReturn(voteProto);

		StepVerifier.create(voteGrpcQueryServiceImpl.getVote(getVoteRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getReviewId()).isEqualTo(reviewId.toString()));
		}).verifyComplete();

		Mockito.verify(queryService).findById(Mockito.any(UUID.class));
		Mockito.verify(mapper).fromDtoToProto(Mockito.any(VoteDto.class));
	}

}
