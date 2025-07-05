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

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.ReviewDto;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReviewGrpcCommandServiceImplTest {

	@Mock
	private CommandServicePort<ReviewDto> commandService;
	@Mock
	private ReviewMapper mapper;
	@Mock
	private ReviewRequestValidator requestValidator;

	@InjectMocks
	private ReviewGrpcCommandServiceImpl reviewGrpcCommandServiceImpl;

	private ReviewDto reviewDto;
	private ReviewProto reviewProto;
	private CreateReviewRequest createRequest;	
	private UpdateReviewRequest updateRequest;	
	private DeleteReviewRequest deleteRequest;
	private Event event;

	private final UUID reviewId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final UUID productId = UUID.randomUUID();
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.REV_CAT;
	private final String eventType = GlobalConstants.REV_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	
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

		createRequest = CreateReviewRequest.newBuilder()
				.setProductId(productId.toString())
				.setReviewText(reviewText)
				.setRating(rating)
				.build();

		updateRequest = UpdateReviewRequest.newBuilder()
				.setReviewId(reviewId.toString())
				.setReviewText(reviewText)
				.setRating(rating)
				.build();

		deleteRequest = DeleteReviewRequest.newBuilder()
				.setReviewId(reviewId.toString())
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

		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateReviewRequest.class));
		Mockito.when(mapper.fromCreateToDto(Mockito.any(CreateReviewRequest.class))).thenReturn(reviewDto);
		Mockito.when(commandService.create(Mockito.any(ReviewDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(reviewProto);

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(reviewGrpcCommandServiceImpl.createReview(createRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response).isEqualTo(reviewProto));
					}).verifyComplete();

			Mockito.verify(mapper).fromCreateToDto(Mockito.any(CreateReviewRequest.class));
			Mockito.verify(commandService).create(Mockito.any(ReviewDto.class));
			Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
		}
	}

	@Test
	void update_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateReviewRequest.class));
		Mockito.when(mapper.fromUpdateToDto(Mockito.any(UpdateReviewRequest.class)))
				.thenReturn(reviewDto);
		Mockito.when(commandService.update(Mockito.any(ReviewDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(reviewProto);

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(reviewGrpcCommandServiceImpl.updateReview(updateRequest)
		                    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken())))
					.assertNext(response -> {
						org.junit.jupiter.api.Assertions.assertAll(
								() -> Assertions.assertThat(response).isNotNull(),
								() -> Assertions.assertThat(response).isEqualTo(reviewProto));
					}).verifyComplete();

			Mockito.verify(mapper).fromUpdateToDto(Mockito.any(UpdateReviewRequest.class));
			Mockito.verify(commandService).update(Mockito.any(ReviewDto.class));
			Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
		}
	}

	@Test
	void deleteById_shouldReturnDeleteResponse() {

		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteReviewRequest.class));
		Mockito.when(commandService.deleteById(Mockito.any(UUID.class))).thenReturn(Mono.just(event));

		try (MockedStatic<JwtContextHelper> mockedStaticJwtContextHelper = Mockito.mockStatic(JwtContextHelper.class)) {

			mockedStaticJwtContextHelper.when(JwtContextHelper::getJwtToken).thenReturn("mock-jwt-token");

			StepVerifier
					.create(reviewGrpcCommandServiceImpl.deleteReview(deleteRequest)
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
