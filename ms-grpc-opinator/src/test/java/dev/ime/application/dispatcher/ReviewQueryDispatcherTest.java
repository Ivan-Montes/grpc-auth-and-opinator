package dev.ime.application.dispatcher;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.query.GetAllReviewQueryHandler;
import dev.ime.application.handlers.query.GetByIdReviewQueryHandler;
import dev.ime.application.usecases.query.GetByIdReviewQuery;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@ExtendWith(MockitoExtension.class)
class ReviewQueryDispatcherTest {

	@Mock
	private GetAllReviewQueryHandler getAllQueryHandler;
	
	@Mock
	private GetByIdReviewQueryHandler getByIdQueryHandler;

	@InjectMocks
	private ReviewQueryDispatcher queryDispatcher;
	
	private class QueryTest implements Query{}
	
	@Test
	void getQueryHandler_shouldReturnHandler() {
		
		GetByIdReviewQuery query = new GetByIdReviewQuery(UUID.randomUUID());	
		
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
