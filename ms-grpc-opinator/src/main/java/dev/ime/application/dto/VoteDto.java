package dev.ime.application.dto;

import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VoteDto(
		UUID voteId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_EMAIL)String email,
		@NotNull UUID reviewId,
		@NotNull boolean useful
	    ) {

}
