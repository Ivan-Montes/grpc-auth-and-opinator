package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetAllReviewQuery;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetAllReviewQueryHandler implements QueryHandler<Flux<Review>> {

	private final ReadRepositoryPort<Review> readRepository;

	public GetAllReviewQueryHandler(ReadRepositoryPort<Review> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Flux<Review> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetAllReviewQuery.class)
				.map(GetAllReviewQuery::pageable)
				.flatMapMany(readRepository::findAll);		
	}
	
}
