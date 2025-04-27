package dev.ime.application.handlers;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.GetByIdUserAppQuery;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class GetByIdUserAppQueryHandler implements QueryHandler<Mono<UserApp>> {

	private final ReadRepositoryPort<UserApp> readRepository;

	public GetByIdUserAppQueryHandler(ReadRepositoryPort<UserApp> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Mono<UserApp> handle(Query query) {
		
		return Mono.justOrEmpty(query)
				.cast(GetByIdUserAppQuery.class)
				.map(GetByIdUserAppQuery::id)
				.flatMap(readRepository::findById);
	}
	
}
