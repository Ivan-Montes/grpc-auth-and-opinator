package dev.ime.application.handlers.query;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.usecases.query.GetByIdVoteQuery;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetByIdVoteQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Vote> readRepository;

	@InjectMocks
	private GetByIdVoteQueryHandler getByIdVoteQueryHandler;

	private GetByIdVoteQuery getByIdQuery;
	private Vote vote;
	private Review review;
	
	private final UUID voteId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final boolean useful = true;

	private final UUID reviewId = UUID.randomUUID();

	@BeforeEach
	private void setUp(){

		getByIdQuery = new GetByIdVoteQuery(voteId);
		
		review = new Review();
		review.setReviewId(reviewId);
		
		vote = new Vote(voteId, email, review, useful);
	}

	@Test
	void handle_shouldReturnDomainObject() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(vote));
		
		StepVerifier
		.create(getByIdVoteQueryHandler.handle(getByIdQuery))
		.expectNext(vote)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

}
