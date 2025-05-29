package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetByIdCategoryQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class GetByIdCategoryQueryHandler implements QueryHandler<Mono<Category>> {

	private final ReadRepositoryPort<Category> readRepository;
	
	public GetByIdCategoryQueryHandler(ReadRepositoryPort<Category> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Mono<Category> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetByIdCategoryQuery.class)
				.map(GetByIdCategoryQuery::id)
				.flatMap(readRepository::findById);
	}

}
