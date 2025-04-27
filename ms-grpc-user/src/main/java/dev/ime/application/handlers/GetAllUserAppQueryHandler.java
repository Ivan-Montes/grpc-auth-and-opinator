package dev.ime.application.handlers;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.GetAllUserAppQuery;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetAllUserAppQueryHandler implements QueryHandler<Flux<UserApp>> {

	private final ReadRepositoryPort<UserApp> readRepository;
	
	public GetAllUserAppQueryHandler(ReadRepositoryPort<UserApp> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Flux<UserApp> handle(Query query) {		
		
		return Mono.justOrEmpty(query)
				.cast(GetAllUserAppQuery.class)
				.map(GetAllUserAppQuery::pageable)
				.flatMapMany(readRepository::findAll);		
	}

}
