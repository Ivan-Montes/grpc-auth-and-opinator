package dev.ime.infrastructure.adapter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("userAppReadRepositoryAdapter")
public class UserAppReadRepositoryAdapter implements ReadRepositoryPort<UserApp> {

	private final R2dbcEntityTemplate r2dbcEntityTemplate;
	private final UserAppMapper userAppMapper;

	public UserAppReadRepositoryAdapter(R2dbcEntityTemplate r2dbcEntityTemplate, UserAppMapper userAppMapper) {
		super();
		this.r2dbcEntityTemplate = r2dbcEntityTemplate;
		this.userAppMapper = userAppMapper;
	}

	@Override
	public Flux<UserApp> findAll(Pageable pageable) {
		return r2dbcEntityTemplate
				.select(UserAppJpaEntity.class).matching(Query.empty().sort(pageable.getSort())
						.limit(pageable.getPageSize()).offset(pageable.getOffset()))
				.all().map(userAppMapper::fromJpaToDomain);
	}

	@Override
	public Mono<UserApp> findById(UUID id) {

		return r2dbcEntityTemplate
				.selectOne(Query.query(Criteria.where(GlobalConstants.USERAPP_ID_DB).is(id)), UserAppJpaEntity.class)
				.map(userAppMapper::fromJpaToDomain);
	}

	@Override
	public Mono<Long> countByEmail(String email) {

		return r2dbcEntityTemplate
				.count(Query.query(Criteria.where(GlobalConstants.USERAPP_EMAIL).is(email)), UserAppJpaEntity.class);
	}

	@Override
	public Mono<UserApp> findByEmail(String email) {
		
		return r2dbcEntityTemplate
				.selectOne(Query.query(Criteria.where(GlobalConstants.USERAPP_EMAIL).is(email)), UserAppJpaEntity.class)
				.map(userAppMapper::fromJpaToDomain);
	}
	
}
