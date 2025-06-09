package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.CategorySpecificReadRepositoryPort;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
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
@Qualifier("categoryReadRepositoryAdapter")
public class CategoryReadRepositoryAdapter implements ReadRepositoryPort<Category>, CategorySpecificReadRepositoryPort {

	private final SessionFactory sessionFactory;
	private final CategoryMapper categoryMapper;
	
	public CategoryReadRepositoryAdapter(SessionFactory factory, CategoryMapper categoryMapper) {
		super();
		this.sessionFactory = factory;
		this.categoryMapper = categoryMapper;
	}

	@Override
	public Flux<Category> findAll(Pageable pageable) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<CategoryJpaEntity> cq = cb.createQuery(CategoryJpaEntity.class);
		Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);
		root.fetch("products", JoinType.LEFT);

		List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
		cq.orderBy(orders);

		var uniQuery = sessionFactory.withSession(
				session -> session.createQuery(cq).setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
						.setMaxResults(pageable.getPageSize()).getResultList());

	    return Flux.from(uniQuery.convert().with(UniReactorConverters.toFlux()))
	            .flatMap(Flux::fromIterable)
	            .map(categoryMapper::fromJpaToDomain);
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<CategoryJpaEntity> root) {
	
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
	public Mono<Category> findById(UUID id) {

		String queryString = """
				SELECT
				 c
				FROM
				 CategoryJpaEntity c
				LEFT JOIN FETCH c.products
				WHERE categoryId = ?1
				""";

		return sessionFactory
				.withSession(session -> session.createQuery(queryString, CategoryJpaEntity.class)
						.setParameter(1, id)
						.getSingleResultOrNull())
				.convert().with(UniReactorConverters.toMono())
				.map(categoryMapper::fromJpaToDomain);
	}

	@Override
	public Mono<Boolean> existsByName(String name) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<CategoryJpaEntity> cq =  cb.createQuery(CategoryJpaEntity.class);
		Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);
	    Predicate condition = cb.equal(root.get(GlobalConstants.CAT_NAME), name);
	    cq.where(condition);
	    
		return sessionFactory.withSession(session -> session.createQuery(cq)
				.getResultCount().map( count -> count > 0))
				.convert().with(UniReactorConverters.toMono());		
	}

	@Override
	public Mono<Boolean> isAvailableByIdAndName(UUID id, String name) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<CategoryJpaEntity> cq =  cb.createQuery(CategoryJpaEntity.class);
		Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);
	    Predicate conditionName = cb.equal(root.get(GlobalConstants.CAT_NAME), name);
	    Predicate conditionId = cb.notEqual(root.get(GlobalConstants.CAT_ID), id);
	    cq.where(cb.and(conditionName, conditionId));
	    
		return sessionFactory.withSession(session -> session.createQuery(cq)
				.getResultCount().map( count -> count == 0))
				.convert().with(UniReactorConverters.toMono());		
	}

}
