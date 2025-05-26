package dev.ime.application.handlers.query;

import org.springframework.stereotype.Component;

import dev.ime.application.usecases.query.GetAllProductQuery;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GetAllProductQueryHandler implements QueryHandler<Flux<Product>> {

	private final ReadRepositoryPort<Product> readRepository;

	public GetAllProductQueryHandler(ReadRepositoryPort<Product> readRepository) {
		super();
		this.readRepository = readRepository;
	}

	@Override
	public Flux<Product> handle(Query query) {

		return Mono.justOrEmpty(query)
				.cast(GetAllProductQuery.class)
				.map(GetAllProductQuery::pageable)
				.flatMapMany(readRepository::findAll);		
	}

}
