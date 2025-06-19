package dev.ime.application.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.VoteDto;
import dev.ime.application.usecases.query.GetAllVoteQuery;
import dev.ime.application.usecases.query.GetByIdVoteQuery;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VoteQueryService implements QueryServicePort<VoteDto> {

	private final QueryDispatcher queryDispatcher;
	private final VoteMapper mapper;
	
	public VoteQueryService(@Qualifier("voteQueryDispatcher")QueryDispatcher queryDispatcher, VoteMapper mapper) {
		super();
		this.queryDispatcher = queryDispatcher;
		this.mapper = mapper;
	}
	
	@Override
	public Flux<VoteDto> findAll(Pageable pageable) {

		return Mono.just(new GetAllVoteQuery(pageable)).flatMapMany(this::processGetAllQuery);
	}

	private Flux<VoteDto> processGetAllQuery(GetAllVoteQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Flux<Vote>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMapMany(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
	@Override
	public Mono<VoteDto> findById(UUID id) {

		return Mono.just(new GetByIdVoteQuery(id))
				.flatMap(this::processGetByIdQuery);
	}

	private Mono<VoteDto> processGetByIdQuery(GetByIdVoteQuery query) {

		return Mono.fromSupplier(() -> {
			QueryHandler<Mono<Vote>> handler = queryDispatcher.getQueryHandler(query);
			return handler;
		}).flatMap(handler -> handler.handle(query)).map(mapper::fromDomainToDto);
	}
	
}
