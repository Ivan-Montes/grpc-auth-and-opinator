package dev.ime.application.handlers.query;

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

import dev.ime.application.usecases.query.GetAllVoteQuery;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllVoteQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Vote> readRepository;

	@InjectMocks
	private GetAllVoteQueryHandler getAllVoteQueryHandler;

	private GetAllVoteQuery getAllQuery;
	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Vote vote01;
	private Vote vote02;
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
		
		getAllQuery = new GetAllVoteQuery(pageRequest);
		
		vote01 = new Vote(voteId01, email01, review, useful01);
		vote02 = new Vote(voteId02, email02, review, useful02);
		
	}

	@Test
	void handle_shouldReturnFlux() {

		Mockito.when(readRepository.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(vote01, vote02));
		
		StepVerifier
		.create(getAllVoteQueryHandler.handle(getAllQuery))
		.expectNext(vote01)
		.expectNext(vote02)
		.verifyComplete();

		Mockito.verify(readRepository).findAll(Mockito.any(Pageable.class));		
	}

}
