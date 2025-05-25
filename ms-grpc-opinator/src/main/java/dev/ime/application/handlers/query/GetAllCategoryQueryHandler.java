package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetAllCategoryQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetAllCategoryQueryHandler implements QueryHandler<Flux<Category>> {

	private final ReadRepositoryPort<Category> readRepository;
	
	public GetAllCategoryQueryHandler(ReadRepositoryPort<Category> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Flux<Category> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetAllCategoryQuery.class)
				.map(GetAllCategoryQuery::pageable)
				.flatMapMany(readRepository::findAll);		
	}

}
