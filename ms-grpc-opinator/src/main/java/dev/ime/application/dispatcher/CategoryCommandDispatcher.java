package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateCategoryCommandHandler;
import dev.ime.application.handlers.command.DeleteCategoryCommandHandler;
import dev.ime.application.handlers.command.UpdateCategoryCommandHandler;
import dev.ime.application.usecases.command.CreateCategoryCommand;
import dev.ime.application.usecases.command.DeleteCategoryCommand;
import dev.ime.application.usecases.command.UpdateCategoryCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.port.inbound.CommandDispatcher;

@Component
@Qualifier("categoryCommandDispatcher")
public class CategoryCommandDispatcher implements CommandDispatcher{

	private final Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();

	public CategoryCommandDispatcher(CreateCategoryCommandHandler createCommandHandler,
			UpdateCategoryCommandHandler updateCommandHandler, DeleteCategoryCommandHandler deleteCommandHandler) {
		super();
		commandHandlers.put(CreateCategoryCommand.class, createCommandHandler);
		commandHandlers.put(UpdateCategoryCommand.class, updateCommandHandler);
		commandHandlers.put(DeleteCategoryCommand.class, deleteCommandHandler);
	}

	public CommandHandler getCommandHandler(Command command) {

		Optional<CommandHandler> optHandler = Optional.ofNullable( commandHandlers.get(command.getClass()) );
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, command.getClass().getName())));	
		
	}	
	
}
