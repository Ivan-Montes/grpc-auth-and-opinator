package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.CreateUserAppCommandHandler;
import dev.ime.application.handlers.DeleteUserAppCommandHandler;
import dev.ime.application.handlers.UpdateUserAppCommandHandler;
import dev.ime.application.usecases.CreateUserAppCommand;
import dev.ime.application.usecases.DeleteUserAppCommand;
import dev.ime.application.usecases.UpdateUserAppCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

@Component
public class CommandDispatcher {

	private final Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();

	public CommandDispatcher(CreateUserAppCommandHandler createCommandHandler,
			UpdateUserAppCommandHandler updateCommandHandler, DeleteUserAppCommandHandler deleteCommandHandler) {
		super();
		commandHandlers.put(CreateUserAppCommand.class, createCommandHandler);
		commandHandlers.put(UpdateUserAppCommand.class, updateCommandHandler);
		commandHandlers.put(DeleteUserAppCommand.class, deleteCommandHandler);
	}

	public CommandHandler getCommandHandler(Command command) {

		Optional<CommandHandler> optHandler = Optional.ofNullable( commandHandlers.get(command.getClass()) );
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, command.getClass().getName())));	
		
	}	
	
}
