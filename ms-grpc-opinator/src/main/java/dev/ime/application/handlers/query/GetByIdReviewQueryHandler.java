package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetByIdReviewQuery;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class GetByIdReviewQueryHandler implements QueryHandler<Mono<Review>> {

	private final ReadRepositoryPort<Review> readRepository;

	public GetByIdReviewQueryHandler(ReadRepositoryPort<Review> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Mono<Review> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetByIdReviewQuery.class)
				.map(GetByIdReviewQuery::id)
				.flatMap(readRepository::findById);
	}	

}
