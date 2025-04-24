package dev.ime.application.dto;

import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserAppDto(
		UUID userAppId,
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_EMAIL ) String email,
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_NAME_FULL ) String name,	
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_NAME_FULL ) String lastname	
		) {

}
