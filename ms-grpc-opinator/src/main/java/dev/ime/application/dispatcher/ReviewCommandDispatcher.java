package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateReviewCommandHandler;
import dev.ime.application.handlers.command.DeleteReviewCommandHandler;
import dev.ime.application.handlers.command.UpdateReviewCommandHandler;
import dev.ime.application.usecases.command.CreateReviewCommand;
import dev.ime.application.usecases.command.DeleteReviewCommand;
import dev.ime.application.usecases.command.UpdateReviewCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.port.inbound.CommandDispatcher;

@Component
@Qualifier("reviewCommandDispatcher")
public class ReviewCommandDispatcher implements CommandDispatcher{

	private final Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();

	public ReviewCommandDispatcher(CreateReviewCommandHandler createCommandHandler,
			UpdateReviewCommandHandler updateCommandHandler, DeleteReviewCommandHandler deleteCommandHandler) {
		super();
		commandHandlers.put(CreateReviewCommand.class, createCommandHandler);
		commandHandlers.put(UpdateReviewCommand.class, updateCommandHandler);
		commandHandlers.put(DeleteReviewCommand.class, deleteCommandHandler);
	}

	public CommandHandler getCommandHandler(Command command) {

		Optional<CommandHandler> optHandler = Optional.ofNullable( commandHandlers.get(command.getClass()) );
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, command.getClass().getName())));	
		
	}	
	
}
