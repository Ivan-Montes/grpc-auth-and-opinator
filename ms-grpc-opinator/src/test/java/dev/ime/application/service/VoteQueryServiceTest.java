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

import dev.ime.application.dto.VoteDto;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class VoteQueryServiceTest {

	@Mock
	private QueryDispatcher queryDispatcher;
	@Mock
	private VoteMapper mapper;
	
	@InjectMocks
	private VoteQueryService voteQueryService;

	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Vote vote01;
	private Vote vote02;
	private VoteDto voteDto01;
	private VoteDto voteDto02;
	private Review review;

	private final UUID voteId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final boolean useful01 = true;

	private final UUID voteId02 = UUID.randomUUID();
	private final String email02 = "noemail@email.tk";
	private final boolean useful02 = false;

	private final UUID reviewId = UUID.randomUUID();
	
	@BeforeEach
	private void setUp(){
		
		review = new Review();
		review.setReviewId(reviewId);
		
		voteDto01 = new VoteDto(voteId01, email01, reviewId, useful01);
		voteDto02 = new VoteDto(voteId02, email02, reviewId, useful02);
		
		vote01 = new Vote(voteId01, email01, review, useful01);
		vote02 = new Vote(voteId02, email02, review, useful02);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFluxMultiple() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Flux.just(vote01,vote02));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Vote.class))).thenReturn(voteDto01, voteDto02);
		
		StepVerifier
		.create(voteQueryService.findAll(pageRequest))
		.expectNext(voteDto01, voteDto02)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper, Mockito.times(2)).fromDomainToDto(Mockito.any(Vote.class));		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMonoWithDto() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Mono.just(vote01));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Vote.class))).thenReturn(voteDto01);
		
		StepVerifier
		.create(voteQueryService.findById(voteId01))
		.expectNext(voteDto01)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper).fromDomainToDto(Mockito.any(Vote.class));		
	}

}
