package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("voteReadRepositoryAdapter")
public class VoteReadRepositoryAdapter implements ReadRepositoryPort<Vote>{

	private final SessionFactory sessionFactory;
	private final VoteMapper voteMapper;
	
	public VoteReadRepositoryAdapter(SessionFactory sessionFactory, VoteMapper voteMapper) {
		super();
		this.sessionFactory = sessionFactory;
		this.voteMapper = voteMapper;
	}
	
	@Override
	public Flux<Vote> findAll(Pageable pageable) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<VoteJpaEntity> cq = cb.createQuery(VoteJpaEntity.class);
		Root<VoteJpaEntity> root = cq.from(VoteJpaEntity.class);

		List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
		cq.orderBy(orders);

		var uniQuery = sessionFactory.withSession(
				session -> session.createQuery(cq).setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
						.setMaxResults(pageable.getPageSize()).getResultList());

	    return Flux.from(uniQuery.convert().with(UniReactorConverters.toFlux()))
	            .flatMap(Flux::fromIterable)
	            .map(voteMapper::fromJpaToDomain);
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<VoteJpaEntity> root) {
	
		return sort.stream()
		.map( order ->{
			if (order.isAscending()) {
				return criteriaBuilder.asc(root.get(order.getProperty()));
			} else {
				return criteriaBuilder.desc(root.get(order.getProperty()));
			}
		})
		.toList();		
	}
	
	@Override
	public Mono<Vote> findById(UUID id) {

		String queryString = """
				SELECT
				 v 
				FROM
				 VoteJpaEntity v 
				WHERE voteId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, VoteJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.map(voteMapper::fromJpaToDomain);
	}
	
}
