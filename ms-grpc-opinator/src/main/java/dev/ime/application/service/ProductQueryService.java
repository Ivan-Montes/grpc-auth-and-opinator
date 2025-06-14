package dev.ime.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.ProductDto;
import dev.ime.application.usecases.query.GetAllProductQuery;
import dev.ime.application.usecases.query.GetByIdProductQuery;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductQueryService implements QueryServicePort<ProductDto> {

	private final QueryDispatcher queryDispatcher;
	private final ProductMapper mapper;
	public ProductQueryService(@Qualifier("productQueryDispatcher")QueryDispatcher queryDispatcher, ProductMapper mapper) {
		super();
		this.queryDispatcher = queryDispatcher;
		this.mapper = mapper;
	}
	
	@Override
	public Flux<ProductDto> findAll(Pageable pageable) {
		
		return Mono.just(new GetAllProductQuery(pageable)).flatMapMany(this::processGetAllQuery);
	}

	private Flux<ProductDto> processGetAllQuery(GetAllProductQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Flux<Product>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMapMany(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
	@Override
	public Mono<ProductDto> findById(UUID id) {

		return Mono.just(new GetByIdProductQuery(id))
				.flatMap(this::processGetByIdQuery);
	}

	private Mono<ProductDto> processGetByIdQuery(GetByIdProductQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Mono<Product>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMap(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
}
