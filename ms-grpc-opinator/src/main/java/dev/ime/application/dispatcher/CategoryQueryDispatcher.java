package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.query.GetAllCategoryQueryHandler;
import dev.ime.application.handlers.query.GetByIdCategoryQueryHandler;
import dev.ime.application.usecases.query.GetAllCategoryQuery;
import dev.ime.application.usecases.query.GetByIdCategoryQuery;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@Component
@Qualifier("categoryQueryDispatcher")
public class CategoryQueryDispatcher implements QueryDispatcher {

	private final Map<Class<? extends Query>, QueryHandler<?>> queryHandlers = new HashMap<>();

	public CategoryQueryDispatcher(GetAllCategoryQueryHandler getAllQueryHandler, GetByIdCategoryQueryHandler getByIdQueryHandler) {
		super();
		queryHandlers.put(GetAllCategoryQuery.class, getAllQueryHandler);
		queryHandlers.put(GetByIdCategoryQuery.class, getByIdQueryHandler);
	}

	public <T> QueryHandler<T> getQueryHandler(Query query){

		@SuppressWarnings("unchecked")
		Optional<QueryHandler<T>> optHandler = Optional.ofNullable((QueryHandler<T>)queryHandlers.get(query.getClass()));
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, query.getClass().getName())));	

	}
	
}
