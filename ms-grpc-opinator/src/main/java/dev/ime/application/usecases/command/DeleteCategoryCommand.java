package dev.ime.application.usecases.command;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record DeleteCategoryCommand(UUID categoryId) implements Command  {

}
