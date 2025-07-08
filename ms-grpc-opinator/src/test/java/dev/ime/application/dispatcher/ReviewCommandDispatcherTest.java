package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.command.CreateReviewCommandHandler;
import dev.ime.application.handlers.command.DeleteReviewCommandHandler;
import dev.ime.application.handlers.command.UpdateReviewCommandHandler;
import dev.ime.application.usecases.command.DeleteReviewCommand;
import dev.ime.domain.command.Command;
import dev.ime.domain.command.CommandHandler;

@ExtendWith(MockitoExtension.class)
class ReviewCommandDispatcherTest {

	@Mock
	private CreateReviewCommandHandler createCommandHandler;

	@Mock
	private UpdateReviewCommandHandler updateCommandHandler;

	@Mock
	private DeleteReviewCommandHandler deleteCommandHandler;
	
	@InjectMocks
	private ReviewCommandDispatcher commandDispatcher;
	
	private class CommandTest implements Command{}
	
	@Test
	void getCommandHandler_shouldReturnHandler() {
		
		DeleteReviewCommand command = new DeleteReviewCommand(UUID.randomUUID());
		
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
