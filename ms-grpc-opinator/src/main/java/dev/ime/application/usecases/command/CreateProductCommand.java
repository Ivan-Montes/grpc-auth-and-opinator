package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record CreateProductCommand(UUID productId, String productName, String productDescription, UUID categoryId) implements Command  {

}
