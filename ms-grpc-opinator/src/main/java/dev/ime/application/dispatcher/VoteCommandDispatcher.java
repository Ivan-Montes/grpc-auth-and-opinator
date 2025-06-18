package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateVoteCommandHandler;
import dev.ime.application.handlers.command.DeleteVoteCommandHandler;
import dev.ime.application.handlers.command.UpdateVoteCommandHandler;
import dev.ime.application.usecases.command.CreateVoteCommand;
import dev.ime.application.usecases.command.DeleteVoteCommand;
import dev.ime.application.usecases.command.UpdateVoteCommand;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;
import dev.ime.domain.port.inbound.CommandDispatcher;

@Component
@Qualifier("voteCommandDispatcher")
public class VoteCommandDispatcher implements CommandDispatcher{

	private final Map<Class<? extends Command>, CommandHandler> commandHandlers = new HashMap<>();

	public VoteCommandDispatcher(CreateVoteCommandHandler createCommandHandler,
			UpdateVoteCommandHandler updateCommandHandler, DeleteVoteCommandHandler deleteCommandHandler) {
		super();
		commandHandlers.put(CreateVoteCommand.class, createCommandHandler);
		commandHandlers.put(UpdateVoteCommand.class, updateCommandHandler);
		commandHandlers.put(DeleteVoteCommand.class, deleteCommandHandler);
	}
	
	public CommandHandler getCommandHandler(Command command) {

		Optional<CommandHandler> optHandler = Optional.ofNullable( commandHandlers.get(command.getClass()) );
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, command.getClass().getName())));	
		
	}	
	
}
