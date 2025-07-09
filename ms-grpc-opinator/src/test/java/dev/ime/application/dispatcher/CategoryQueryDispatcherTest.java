package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.query.GetAllCategoryQueryHandler;
import dev.ime.application.handlers.query.GetByIdCategoryQueryHandler;
import dev.ime.application.usecases.query.GetByIdCategoryQuery;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@ExtendWith(MockitoExtension.class)
class CategoryQueryDispatcherTest {

	@Mock
	private GetAllCategoryQueryHandler getAllQueryHandler;
	
	@Mock
	private GetByIdCategoryQueryHandler getByIdQueryHandler;

	@InjectMocks
	private CategoryQueryDispatcher queryDispatcher;
	
	private class QueryTest implements Query{}
	
	@Test
	void getQueryHandler_shouldReturnHandler() {
		
		GetByIdCategoryQuery query = new GetByIdCategoryQuery(UUID.randomUUID());	
		
		QueryHandler<Object> handler = queryDispatcher.getQueryHandler(query);
		
		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(handler).isNotNull()
				);
	}

	@Test
	void getQueryHandler_shouldThrowException() {
		
		QueryTest queryTest = new QueryTest();
		
		Exception ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalHandlerException.class, ()-> queryDispatcher.getQueryHandler(queryTest));
		
		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(ex).isNotNull(),
				() -> Assertions.assertThat(ex.getClass()).isEqualTo(IllegalHandlerException.class)
				);
	}

}
