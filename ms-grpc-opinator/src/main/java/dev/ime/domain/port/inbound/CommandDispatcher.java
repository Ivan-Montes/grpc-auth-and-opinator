package dev.ime.domain.port.inbound;

import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

public interface CommandDispatcher {

	CommandHandler getCommandHandler(Command command);
	
}
