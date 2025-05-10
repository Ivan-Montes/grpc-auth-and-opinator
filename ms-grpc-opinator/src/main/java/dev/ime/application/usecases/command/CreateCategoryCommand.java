package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record CreateCategoryCommand(UUID categoryId, String categoryName) implements Command  {

}
