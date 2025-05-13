package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record CreateVoteCommand(UUID voteId, String email, UUID reviewId, Boolean useful) implements Command  {

}
