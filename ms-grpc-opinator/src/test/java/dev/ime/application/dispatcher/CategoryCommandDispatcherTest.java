package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateCategoryCommandHandler;
import dev.ime.application.handlers.command.DeleteCategoryCommandHandler;
import dev.ime.application.handlers.command.UpdateCategoryCommandHandler;
import dev.ime.application.usecases.command.DeleteCategoryCommand;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

@ExtendWith(MockitoExtension.class)
class CategoryCommandDispatcherTest {

	@Mock
	private CreateCategoryCommandHandler createCommandHandler;

	@Mock
	private UpdateCategoryCommandHandler updateCommandHandler;

	@Mock
	private DeleteCategoryCommandHandler deleteCommandHandler;
	
	@InjectMocks
	private CategoryCommandDispatcher commandDispatcher;
	
	private class CommandTest implements Command{}
	
	@Test
	void getCommandHandler_shouldReturnHandler() {
		
		DeleteCategoryCommand command = new DeleteCategoryCommand(UUID.randomUUID());
		
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
