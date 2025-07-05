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

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.ReviewDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetReviewRequest;
import dev.proto.PaginationRequest;
import dev.proto.ReviewProto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReviewGrpcQueryServiceImplTest {

	@Mock
	private QueryServicePort<ReviewDto> queryService;
	@Mock
	private ReviewMapper mapper;
	@Mock
	private ReviewRequestValidator requestValidator;

	@InjectMocks
	private ReviewGrpcQueryServiceImpl reviewGrpcQueryServiceImpl;	
	
	private ReviewDto reviewDto;
	private ReviewProto reviewProto;
	private PaginationRequest paginationRequest;	
	private GetReviewRequest getReviewRequest;

	private final UUID reviewId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID productId = UUID.randomUUID();
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	@BeforeEach
	private void setUp(){
		
		reviewDto = new ReviewDto(reviewId, email, productId, reviewText, rating);
		reviewProto = ReviewProto.newBuilder()
				.setReviewId(reviewId.toString())
				.setEmail(email)
				.setProductId(productId.toString())
				.setReviewText(reviewText)
				.setRating(rating)
				.build();	

		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.REV_RAT)
				.setSortDir(GlobalConstants.PS_D)
				.build();
		
		getReviewRequest = GetReviewRequest.newBuilder()
				.setReviewId(productId.toString())
				.build();	
	}

	@Test
	void listReviews_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(reviewDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(reviewProto));
	
		StepVerifier
		.create(reviewGrpcQueryServiceImpl.listReviews(Empty.getDefaultInstance()))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getReviewsCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}
	
	@Test
	void listReviewsPaginated_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(reviewDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(reviewProto));
	
		StepVerifier
		.create(reviewGrpcQueryServiceImpl.listReviewsPaginated(paginationRequest))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getReviewsCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}

	@Test
	void getReview_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetReviewRequest.class));
		Mockito.when(queryService.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(reviewDto));
		Mockito.when(mapper.fromDtoToProto(Mockito.any(ReviewDto.class)))
				.thenReturn(reviewProto);

		StepVerifier.create(reviewGrpcQueryServiceImpl.getReview(getReviewRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getReviewId()).isEqualTo(reviewId.toString()));
		}).verifyComplete();

		Mockito.verify(queryService).findById(Mockito.any(UUID.class));
		Mockito.verify(mapper).fromDtoToProto(Mockito.any(ReviewDto.class));
	}
	
}
