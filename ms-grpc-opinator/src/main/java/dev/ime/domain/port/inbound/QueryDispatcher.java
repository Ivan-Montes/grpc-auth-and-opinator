package dev.ime.domain.port.inbound;

import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;

public interface QueryDispatcher {

	<T> QueryHandler<T> getQueryHandler(Query query);
}
