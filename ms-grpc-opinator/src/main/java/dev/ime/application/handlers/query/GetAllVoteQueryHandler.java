package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetAllVoteQuery;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetAllVoteQueryHandler implements QueryHandler<Flux<Vote>> {

	private final ReadRepositoryPort<Vote> readRepository;

	public GetAllVoteQueryHandler(ReadRepositoryPort<Vote> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Flux<Vote> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetAllVoteQuery.class)
				.map(GetAllVoteQuery::pageable)
				.flatMapMany(readRepository::findAll);		
	}

}
