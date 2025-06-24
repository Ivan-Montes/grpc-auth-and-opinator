package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("reviewReadRepositoryAdapter")
public class ReviewReadRepositoryAdapter implements ReadRepositoryPort<Review>{

	private final SessionFactory sessionFactory;
	private final ReviewMapper reviewMapper;
	
	public ReviewReadRepositoryAdapter(SessionFactory sessionFactory, ReviewMapper reviewMapper) {
		super();
		this.sessionFactory = sessionFactory;
		this.reviewMapper = reviewMapper;
	}

	@Override
	public Flux<Review> findAll(Pageable pageable) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ReviewJpaEntity> cq = cb.createQuery(ReviewJpaEntity.class);
		Root<ReviewJpaEntity> root = cq.from(ReviewJpaEntity.class);
		root.fetch("votes", JoinType.LEFT);

		List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
		cq.orderBy(orders);

		var uniQuery = sessionFactory.withSession(
				session -> session.createQuery(cq).setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
						.setMaxResults(pageable.getPageSize()).getResultList());

	    return Flux.from(uniQuery.convert().with(UniReactorConverters.toFlux()))
	            .flatMap(Flux::fromIterable)
	            .map(reviewMapper::fromJpaToDomain);
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<ReviewJpaEntity> root) {
	
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
	public Mono<Review> findById(UUID id) {

		String queryString = """
				SELECT
				 r 
				FROM
				 ReviewJpaEntity r 
				LEFT JOIN FETCH r.votes 
				WHERE reviewId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, ReviewJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.map(reviewMapper::fromJpaToDomain);
	}
	
}
