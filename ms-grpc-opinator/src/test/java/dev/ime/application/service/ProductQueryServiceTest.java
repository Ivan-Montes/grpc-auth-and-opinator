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

import dev.ime.application.dto.ProductDto;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.inbound.QueryDispatcher;
import dev.ime.domain.query.Query;
import dev.ime.domain.query.QueryHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

	@Mock
	private QueryDispatcher queryDispatcher;
	@Mock
	private ProductMapper mapper;
	
	@InjectMocks
	private ProductQueryService productQueryService;

	private final PageRequest pageRequest = PageRequest.of(0, 100);

	private Product product01;
	private Product product02;
	private ProductDto productDto01;
	private ProductDto productDto02;
	private Category category;

	private final UUID productId01 = UUID.randomUUID();
	private final String productName01 = "Tomatoes";
	private final String productDescription01 = "full of red";

	private final UUID productId02 = UUID.randomUUID();
	private final String productName02 = "Tomatoes";
	private final String productDescription02 = "full of red";

	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";
	
	
	@BeforeEach
	private void setUp() {

		productDto01 = new ProductDto(productId01, productName01, productDescription01, categoryId);
		productDto02 = new ProductDto(productId02, productName02, productDescription02, categoryId);
		
		category = new Category();
		category.setCategoryId(categoryId);
		category.setCategoryName(categoryName);
		
		product01 = new Product(productId01, productName01, productDescription01, category, new HashSet<>());
		product02 = new Product(productId02, productName02, productDescription02, category, new HashSet<>());
		
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFluxMultiple() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Flux.just(product01,product02));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Product.class))).thenReturn(productDto01, productDto02);
		
		StepVerifier
		.create(productQueryService.findAll(pageRequest))
		.expectNext(productDto01, productDto02)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper, Mockito.times(2)).fromDomainToDto(Mockito.any(Product.class));		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMonoWithDto() {

		QueryHandler<Object> queryHandler = Mockito.mock(QueryHandler.class);
		Mockito.when(queryDispatcher.getQueryHandler(Mockito.any(Query.class))).thenReturn(queryHandler);
		Mockito.when(queryHandler.handle(Mockito.any(Query.class))).thenReturn(Mono.just(product01));
		Mockito.when(mapper.fromDomainToDto(Mockito.any(Product.class))).thenReturn(productDto01);
		
		StepVerifier
		.create(productQueryService.findById(productId01))
		.expectNext(productDto01)
		.verifyComplete();

		Mockito.verify(queryDispatcher).getQueryHandler(Mockito.any(Query.class));
		Mockito.verify(queryHandler).handle(Mockito.any(Query.class));
		Mockito.verify(mapper).fromDomainToDto(Mockito.any(Product.class));		
	}

}
