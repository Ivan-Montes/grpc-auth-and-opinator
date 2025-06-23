package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.ProductSpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Qualifier("productReadRepositoryAdapter")
public class ProductReadRepositoryAdapter implements ReadRepositoryPort<Product>, ProductSpecificReadRepositoryPort {

	private final SessionFactory sessionFactory;
	private final ProductMapper productMapper;
	
	public ProductReadRepositoryAdapter(SessionFactory sessionFactory, ProductMapper productMapper) {
		super();
		this.sessionFactory = sessionFactory;
		this.productMapper = productMapper;
	}
	
	@Override
	public Flux<Product> findAll(Pageable pageable) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ProductJpaEntity> cq = cb.createQuery(ProductJpaEntity.class);
		Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);
		root.fetch("reviews", JoinType.LEFT);

		List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
		cq.orderBy(orders);

		var uniQuery = sessionFactory.withSession(
				session -> session.createQuery(cq).setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
						.setMaxResults(pageable.getPageSize()).getResultList());

	    return Flux.from(uniQuery.convert().with(UniReactorConverters.toFlux()))
	            .flatMap(Flux::fromIterable)
	            .map(productMapper::fromJpaToDomain);
	}
	
	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<ProductJpaEntity> root) {
	
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
	public Mono<Product> findById(UUID id) {

		String queryString = """
				SELECT
				 p
				FROM
				 ProductJpaEntity p
				LEFT JOIN FETCH p.reviews
				WHERE productId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, ProductJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.map(productMapper::fromJpaToDomain);
	}
	
	@Override
	public Mono<Boolean> existsByName(String name) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ProductJpaEntity> cq =  cb.createQuery(ProductJpaEntity.class);
		Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);
	    Predicate condition = cb.equal(root.get(GlobalConstants.PROD_NAME), name);
	    cq.where(condition);
	    
		return sessionFactory.withSession(session -> session.createQuery(cq)
				.getResultCount().map( count -> count > 0))
				.convert().with(UniReactorConverters.toMono());		
	}
	
	@Override
	public Mono<Boolean> isAvailableByIdAndName(UUID id, String name) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ProductJpaEntity> cq =  cb.createQuery(ProductJpaEntity.class);
		Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);
	    Predicate conditionName = cb.equal(root.get(GlobalConstants.PROD_NAME), name);
	    Predicate conditionId = cb.notEqual(root.get(GlobalConstants.PROD_ID), id);
	    cq.where(cb.and(conditionName, conditionId));
	    
		return sessionFactory.withSession(session -> session.createQuery(cq)
				.getResultCount().map( count -> count == 0))
				.convert().with(UniReactorConverters.toMono());		
	}
	
}
