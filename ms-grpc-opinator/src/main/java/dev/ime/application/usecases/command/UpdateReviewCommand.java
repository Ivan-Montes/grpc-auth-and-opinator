package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record UpdateReviewCommand(UUID reviewId, String reviewText, int rating) implements Command  {

}
