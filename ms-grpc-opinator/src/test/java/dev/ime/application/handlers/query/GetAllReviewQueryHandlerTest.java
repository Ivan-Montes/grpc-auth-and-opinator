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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.ime.application.usecases.query.GetAllReviewQuery;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllReviewQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Review> readRepository;

	@InjectMocks
	private GetAllReviewQueryHandler getAllReviewQueryHandler;

	private GetAllReviewQuery getAllQuery;
	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Review review01;
	private Review review02;
	private Product product;

	private final UUID reviewId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final String reviewText01 = "Excellent";
	private final Integer rating01 = 5;

	private final UUID reviewId02 = UUID.randomUUID();
	private final String email02 = "noemail@email.tk";
	private final String reviewText02 = "Mhe";
	private final Integer rating02 = 1;

	private final UUID productId = UUID.randomUUID();
	
	@BeforeEach
	private void setUp(){

		getAllQuery = new GetAllReviewQuery(pageRequest);
		
		product = new Product();
		product.setProductId(productId);
		
		review01 = new Review(reviewId01, email01, product, reviewText01, rating01, new HashSet<>());
		review02 = new Review(reviewId02, email02, product, reviewText02, rating02, new HashSet<>());

	}

	@Test
	void handle_shouldReturnFlux() {

		Mockito.when(readRepository.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(review01, review02));
		
		StepVerifier
		.create(getAllReviewQueryHandler.handle(getAllQuery))
		.expectNext(review01)
		.expectNext(review02)
		.verifyComplete();

		Mockito.verify(readRepository).findAll(Mockito.any(Pageable.class));		
	}

}
