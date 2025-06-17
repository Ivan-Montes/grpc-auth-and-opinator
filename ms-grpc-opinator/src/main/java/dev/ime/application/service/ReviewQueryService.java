package dev.ime.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.ReviewDto;
import dev.ime.application.usecases.query.GetAllReviewQuery;
import dev.ime.application.usecases.query.GetByIdReviewQuery;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReviewQueryService implements QueryServicePort<ReviewDto> {

	private final QueryDispatcher queryDispatcher;
	private final ReviewMapper mapper;
	
	public ReviewQueryService(@Qualifier("reviewQueryDispatcher")QueryDispatcher queryDispatcher, ReviewMapper mapper) {
		super();
		this.queryDispatcher = queryDispatcher;
		this.mapper = mapper;
	}
	
	@Override
	public Flux<ReviewDto> findAll(Pageable pageable) {

		return Mono.just(new GetAllReviewQuery(pageable)).flatMapMany(this::processGetAllQuery);
	}

	private Flux<ReviewDto> processGetAllQuery(GetAllReviewQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Flux<Review>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMapMany(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
	@Override
	public Mono<ReviewDto> findById(UUID id) {

		return Mono.just(new GetByIdReviewQuery(id))
				.flatMap(this::processGetByIdQuery);
	}

	private Mono<ReviewDto> processGetByIdQuery(GetByIdReviewQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Mono<Review>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMap(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
}
