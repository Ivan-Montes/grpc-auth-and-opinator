package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateProductCommandHandler;
import dev.ime.application.handlers.command.DeleteProductCommandHandler;
import dev.ime.application.handlers.command.UpdateProductCommandHandler;
import dev.ime.application.usecases.command.CreateProductCommand;
import dev.ime.application.usecases.command.DeleteProductCommand;
import dev.ime.application.usecases.command.UpdateProductCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.port.inbound.CommandDispatcher;

@Component
@Qualifier("productCommandDispatcher")
public class ProductCommandDispatcher implements CommandDispatcher{

	private final Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();

	public ProductCommandDispatcher(CreateProductCommandHandler createCommandHandler,
			UpdateProductCommandHandler updateCommandHandler, DeleteProductCommandHandler deleteCommandHandler) {
		super();
		commandHandlers.put(CreateProductCommand.class, createCommandHandler);
		commandHandlers.put(UpdateProductCommand.class, updateCommandHandler);
		commandHandlers.put(DeleteProductCommand.class, deleteCommandHandler);
	}

	public CommandHandler getCommandHandler(Command command) {

		Optional<CommandHandler> optHandler = Optional.ofNullable( commandHandlers.get(command.getClass()) );
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, command.getClass().getName())));	
		
	}	
	
}
