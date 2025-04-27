package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.GetAllUserAppQueryHandler;
import dev.ime.application.handlers.GetByIdUserAppQueryHandler;
import dev.ime.application.usecases.GetAllUserAppQuery;
import dev.ime.application.usecases.GetByIdUserAppQuery;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@Component
public class QueryDispatcher {

	private final Map<Class<? extends Query>, QueryHandler<?>> queryHandlers = new HashMap<>();

	public QueryDispatcher(GetAllUserAppQueryHandler getAllQueryHandler, GetByIdUserAppQueryHandler getByIdQueryHandler) {
		super();
		queryHandlers.put(GetAllUserAppQuery.class, getAllQueryHandler);
		queryHandlers.put(GetByIdUserAppQuery.class, getByIdQueryHandler);
	}

	public <T> QueryHandler<T> getQueryHandler(Query query){

		@SuppressWarnings("unchecked")
		Optional<QueryHandler<T>> optHandler = Optional.ofNullable((QueryHandler<T>)queryHandlers.get(query.getClass()));
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, query.getClass().getName())));	

	}
	
}
