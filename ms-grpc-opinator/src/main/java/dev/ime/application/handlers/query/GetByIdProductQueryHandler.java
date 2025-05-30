package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetByIdProductQuery;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Mono;

@Component
public class GetByIdProductQueryHandler implements QueryHandler<Mono<Product>> {

	private final ReadRepositoryPort<Product> readRepository;

	public GetByIdProductQueryHandler(ReadRepositoryPort<Product> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Mono<Product> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetByIdProductQuery.class)
				.map(GetByIdProductQuery::id)
				.flatMap(readRepository::findById);
	}	

}
