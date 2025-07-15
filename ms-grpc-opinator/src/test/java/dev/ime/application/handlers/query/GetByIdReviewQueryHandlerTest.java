package dev.ime.application.handlers.query;

import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.usecases.query.GetByIdReviewQuery;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetByIdReviewQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Review> readRepository;

	@InjectMocks
	private GetByIdReviewQueryHandler getByIdReviewQueryHandler;

	private GetByIdReviewQuery getByIdQuery;
	private Review review;
	private Product product;
	
	private final UUID reviewId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	private final UUID productId = UUID.randomUUID();
	
	@BeforeEach
	private void setUp(){

		getByIdQuery = new GetByIdReviewQuery(reviewId);
		
		product = new Product();
		product.setProductId(productId);
		
		review = new Review(reviewId, email, product, reviewText, rating, new HashSet<>());
		
	}

	@Test
	void handle_shouldReturnDomainObject() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(review));
		
		StepVerifier
		.create(getByIdReviewQueryHandler.handle(getByIdQuery))
		.expectNext(review)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}
	
}
