package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetByIdVoteQuery;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class GetByIdVoteQueryHandler  implements QueryHandler<Mono<Vote>> {

	private final ReadRepositoryPort<Vote> readRepository;

	public GetByIdVoteQueryHandler(ReadRepositoryPort<Vote> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Mono<Vote> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetByIdVoteQuery.class)
				.map(GetByIdVoteQuery::id)
				.flatMap(readRepository::findById);
	}
	
}
