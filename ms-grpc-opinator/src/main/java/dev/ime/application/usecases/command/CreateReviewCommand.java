package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record CreateReviewCommand(UUID reviewId, String email, UUID productId, String reviewText, int rating) implements Command  {

}
