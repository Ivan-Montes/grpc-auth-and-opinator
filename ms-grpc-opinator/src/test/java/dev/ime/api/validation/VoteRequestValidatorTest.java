package dev.ime.api.validation;


import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.GetVoteRequest;
import dev.proto.UpdateVoteRequest;

@ExtendWith(MockitoExtension.class)
class VoteRequestValidatorTest {

	private VoteRequestValidator dtoValidator;

	private final String voteId = UUID.randomUUID().toString();
	private final String reviewId = UUID.randomUUID().toString();
	private final boolean useful = true;
	
	@BeforeEach
	private void setUp(){
		
		dtoValidator = new VoteRequestValidator();
	}

	@Test
	void validateCreateRequest_shouldValidateRight() {
		
		CreateVoteRequest request = CreateVoteRequest.newBuilder()
				.setReviewId(reviewId)
				.setUseful(useful)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateRequest(request));

	}

	@Test
	void validateUpdateRequest_shouldValidateRight() {
		
		UpdateVoteRequest request = UpdateVoteRequest.newBuilder()
				.setVoteId(voteId)
				.setUseful(useful)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateUpdateRequest(request));

	}

	@Test
	void validateDeleteRequest_shouldValidateRight() {
		
		DeleteVoteRequest request = DeleteVoteRequest.newBuilder()
				.setVoteId(voteId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateDeleteRequest(request));

	}

	@Test
	void validateGetRequest_shouldValidateRight() {
		
		GetVoteRequest request = GetVoteRequest.newBuilder()
				.setVoteId(voteId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateGetRequest(request));

	}

}
