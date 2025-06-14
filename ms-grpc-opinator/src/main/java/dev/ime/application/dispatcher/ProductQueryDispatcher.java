package dev.ime.application.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.ime.application.exception.IllegalHandlerException;
import dev.ime.application.handlers.query.GetAllProductQueryHandler;
import dev.ime.application.handlers.query.GetByIdProductQueryHandler;
import dev.ime.application.usecases.query.GetAllProductQuery;
import dev.ime.application.usecases.query.GetByIdProductQuery;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

@Component
@Qualifier("productQueryDispatcher")
public class ProductQueryDispatcher implements QueryDispatcher {

	private final Map<Class<? extends Query>, QueryHandler<?>> queryHandlers = new HashMap<>();

	public ProductQueryDispatcher(GetAllProductQueryHandler getAllQueryHandler, GetByIdProductQueryHandler getByIdQueryHandler) {
		super();
		queryHandlers.put(GetAllProductQuery.class, getAllQueryHandler);
		queryHandlers.put(GetByIdProductQuery.class, getByIdQueryHandler);
	}

	public <T> QueryHandler<T> getQueryHandler(Query query){

		@SuppressWarnings("unchecked")
		Optional<QueryHandler<T>> optHandler = Optional.ofNullable((QueryHandler<T>)queryHandlers.get(query.getClass()));
		
		return optHandler.orElseThrow( () -> new IllegalHandlerException(Map.of(GlobalConstants.OBJ_VALUE, query.getClass().getName())));	

	}
	
}
