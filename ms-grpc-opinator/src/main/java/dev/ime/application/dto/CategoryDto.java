package dev.ime.application.dto;

import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryDto(
		UUID categoryId, 
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_NAME_FULL) String categoryName 
		) {

}
