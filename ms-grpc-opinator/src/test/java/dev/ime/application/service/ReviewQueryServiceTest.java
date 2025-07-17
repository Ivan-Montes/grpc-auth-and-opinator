package dev.ime.application.service;

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

import dev.ime.application.dto.ReviewDto;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

	@Mock
	private QueryDispatcher queryDispatcher;
	@Mock
	private ReviewMapper mapper;
	
	@InjectMocks
	private ReviewQueryService reviewQueryService;

	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Review review01;
	private Review review02;
	private ReviewDto reviewDto01;
	private ReviewDto reviewDto02;
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

		reviewDto01 = new ReviewDto(reviewId01, email01, productId, reviewText01, rating01);
		reviewDto02 = new ReviewDto(reviewId02, email02, productId, reviewText02, rating02);

		product = new Product();
		product.setProductId(productId);
		
		review01 = new Review(reviewId01, email01, product, reviewText01, rating01, new HashSet<>());
		review02 = new Review(reviewId02, email02, product, reviewText02, rating02, new HashSet<>());

	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFluxMultiple() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Flux.just(review01,review02));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Review.class))).thenReturn(reviewDto01, reviewDto02);
		
		StepVerifier
		.create(reviewQueryService.findAll(pageRequest))
		.expectNext(reviewDto01, reviewDto02)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper, Mockito.times(2)).fromDomainToDto(Mockito.any(Review.class));		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMonoWithDto() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Mono.just(review01));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Review.class))).thenReturn(reviewDto01);
		
		StepVerifier
		.create(reviewQueryService.findById(reviewId01))
		.expectNext(reviewDto01)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper).fromDomainToDto(Mockito.any(Review.class));		
	}

}
