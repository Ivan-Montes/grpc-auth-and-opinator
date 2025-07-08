package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateVoteCommandHandler;
import dev.ime.application.handlers.command.DeleteVoteCommandHandler;
import dev.ime.application.handlers.command.UpdateVoteCommandHandler;
import dev.ime.application.usecases.command.DeleteVoteCommand;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

@ExtendWith(MockitoExtension.class)
class VoteCommandDispatcherTest {

	@Mock
	private CreateVoteCommandHandler createCommandHandler;

	@Mock
	private UpdateVoteCommandHandler updateCommandHandler;

	@Mock
	private DeleteVoteCommandHandler deleteCommandHandler;
	
	@InjectMocks
	private VoteCommandDispatcher commandDispatcher;
	
	private class CommandTest implements Command{}
	
	@Test
	void getCommandHandler_shouldReturnHandler() {
		
		DeleteVoteCommand command = new DeleteVoteCommand(UUID.randomUUID());
		
		CommandHandler handler = commandDispatcher.getCommandHandler(command);
		
		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(handler).isNotNull()
				);		
	}

	@Test
	void getCommandHandler_WithUnknownCommand_ThrowError() {
		
		CommandTest commandTest = new CommandTest();
		
		Exception ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalHandlerException.class, () -> commandDispatcher.getCommandHandler(commandTest));
		
		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(ex).isNotNull(),
				() -> Assertions.assertThat(ex.getClass()).isEqualTo(IllegalHandlerException.class)
				);			
	}	

}
