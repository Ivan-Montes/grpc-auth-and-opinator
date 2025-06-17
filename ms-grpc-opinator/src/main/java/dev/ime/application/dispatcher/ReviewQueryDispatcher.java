package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.query.GetAllReviewQueryHandler;
import dev.ime.application.handlers.query.GetByIdReviewQueryHandler;
import dev.ime.application.usecases.query.GetAllReviewQuery;
import dev.ime.application.usecases.query.GetByIdReviewQuery;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@Component
@Qualifier("reviewQueryDispatcher")
public class ReviewQueryDispatcher implements QueryDispatcher {

	private final Map<Class<? extends Query>, QueryHandler<?>> queryHandlers = new HashMap<>();

	public ReviewQueryDispatcher(GetAllReviewQueryHandler getAllQueryHandler, GetByIdReviewQueryHandler getByIdQueryHandler) {
		super();
		queryHandlers.put(GetAllReviewQuery.class, getAllQueryHandler);
		queryHandlers.put(GetByIdReviewQuery.class, getByIdQueryHandler);
	}

	public <T> QueryHandler<T> getQueryHandler(Query query){

		@SuppressWarnings("unchecked")
		Optional<QueryHandler<T>> optHandler = Optional.ofNullable((QueryHandler<T>)queryHandlers.get(query.getClass()));
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, query.getClass().getName())));	

	}
	
}
