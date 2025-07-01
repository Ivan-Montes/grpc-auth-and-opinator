package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.CreateUserAppCommandHandler;
import dev.ime.application.handlers.DeleteUserAppCommandHandler;
import dev.ime.application.handlers.UpdateUserAppCommandHandler;
import dev.ime.application.usecases.DeleteUserAppCommand;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

	@Mock
	private CreateUserAppCommandHandler createCommandHandler;

	@Mock
	private UpdateUserAppCommandHandler updateCommandHandler;

	@Mock
	private DeleteUserAppCommandHandler deleteCommandHandler;
	
	@InjectMocks
	private CommandDispatcher commandDispatcher;
	
	private class CommandTest implements Command{}
	
	@Test
	void getCommandHandler_shouldReturnHandler() {
		
		DeleteUserAppCommand command = new DeleteUserAppCommand(UUID.randomUUID());
		
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
