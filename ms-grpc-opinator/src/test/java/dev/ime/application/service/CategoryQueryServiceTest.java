package dev.ime.application.service;

import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import dev.ime.application.dto.CategoryDto;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

	@Mock
	private QueryDispatcher queryDispatcher;
	@Mock
	private CategoryMapper mapper;
	
	@InjectMocks
	private CategoryQueryService categoryQueryService;

	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Category category01;
	private Category category02;
	private CategoryDto categoryDto01;
	private CategoryDto categoryDto02;

	private final UUID categoryId01 = UUID.randomUUID();
	private final String categoryName01 = "Vegetables";
	
	private final UUID categoryId02 = UUID.randomUUID();
	private final String categoryName02 = "NoVegetables";

	@BeforeEach
	private void setUp() {

		categoryDto01 = new CategoryDto(categoryId01,categoryName01);
		categoryDto02 = new CategoryDto(categoryId02,categoryName02);

		category01 = new Category(categoryId01, categoryName01, new HashSet<>());
		category02 = new Category(categoryId02, categoryName02, new HashSet<>());
	
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFluxMultiple() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Flux.just(category01,category02));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Category.class))).thenReturn(categoryDto01, categoryDto02);
		
		StepVerifier
		.create(categoryQueryService.findAll(pageRequest))
		.expectNext(categoryDto01, categoryDto02)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper, Mockito.times(2)).fromDomainToDto(Mockito.any(Category.class));		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMonoWithDto() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Mono.just(category01));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Category.class))).thenReturn(categoryDto01);
		
		StepVerifier
		.create(categoryQueryService.findById(categoryId01))
		.expectNext(categoryDto01)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper).fromDomainToDto(Mockito.any(Category.class));		
	}

}
