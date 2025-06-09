package dev.ime.application.dto;

import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ReviewDto(
		UUID reviewId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_EMAIL) String email,
		@NotNull UUID productId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_DESC_FULL) String reviewText,
		@NotNull @Min(0) int rating
	    ) {

}
