package dev.ime.application.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dispatcher.QueryDispatcher;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.usecases.GetAllUserAppQuery;
import dev.ime.application.usecases.GetByIdUserAppQuery;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class QueryService implements QueryServicePort<UserAppDto> {

	private final QueryDispatcher queryDispatcher;
	private final UserAppMapper userAppMapper;

	public QueryService(QueryDispatcher queryDispatcher, UserAppMapper userAppMapper) {
		super();
		this.queryDispatcher = queryDispatcher;
		this.userAppMapper = userAppMapper;
	}

	@Override
	public Flux<UserAppDto> findAll(Pageable pageable) {

		return Mono.just(new GetAllUserAppQuery(pageable)).flatMapMany(this::processGetAllQuery);
	}

	private Flux<UserAppDto> processGetAllQuery(GetAllUserAppQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Flux<UserApp>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMapMany(handler -> handler.handle(query)).map(userAppMapper::fromDomainToDto);
	}

	@Override
	public Mono<UserAppDto> findById(UUID id) {

		return Mono.just(new GetByIdUserAppQuery(id))
				.flatMap(this::processGetByIdQuery);
	}

	private Mono<UserAppDto> processGetByIdQuery(GetByIdUserAppQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Mono<UserApp>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMap(handler -> handler.handle(query)).map(userAppMapper::fromDomainToDto);
	}
	
}
