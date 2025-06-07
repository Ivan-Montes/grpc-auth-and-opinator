package dev.ime.application.dto;

import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProductDto(
		UUID productId,
	    @NotBlank @Pattern(regexp = GlobalConstants.PATTERN_NAME_FULL) String productName,
	    @NotBlank @Pattern(regexp = GlobalConstants.PATTERN_DESC_FULL) String productDescription,
	    @NotNull UUID categoryId
		) {

}
