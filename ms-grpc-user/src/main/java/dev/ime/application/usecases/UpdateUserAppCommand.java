package dev.ime.application.usecases;

import java.util.UUID;

import dev.ime.domain.command.Command;

public record UpdateUserAppCommand(
		UUID userAppId,
		String email, 
		String name, 
		String lastname
		) implements Command  {

}
