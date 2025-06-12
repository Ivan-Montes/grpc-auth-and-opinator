package dev.ime.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.CategoryDto;
import dev.ime.application.usecases.query.GetAllCategoryQuery;
import dev.ime.application.usecases.query.GetByIdCategoryQuery;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryQueryService implements QueryServicePort<CategoryDto> {

	private final QueryDispatcher queryDispatcher;
	private final CategoryMapper mapper;
	
	public CategoryQueryService(@Qualifier("categoryQueryDispatcher")QueryDispatcher queryDispatcher, CategoryMapper mapper) {
		super();
		this.queryDispatcher = queryDispatcher;
		this.mapper = mapper;
	}

	@Override
	public Flux<CategoryDto> findAll(Pageable pageable) {
		
		return Mono.just(new GetAllCategoryQuery(pageable)).flatMapMany(this::processGetAllQuery);
	}

	private Flux<CategoryDto> processGetAllQuery(GetAllCategoryQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Flux<Category>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMapMany(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}

	@Override
	public Mono<CategoryDto> findById(UUID id) {

		return Mono.just(new GetByIdCategoryQuery(id))
				.flatMap(this::processGetByIdQuery);
	}

	private Mono<CategoryDto> processGetByIdQuery(GetByIdCategoryQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Mono<Category>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMap(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
}
