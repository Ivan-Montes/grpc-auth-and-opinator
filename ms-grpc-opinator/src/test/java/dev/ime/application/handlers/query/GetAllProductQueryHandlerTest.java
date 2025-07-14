package dev.ime.application.handlers.query;

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
import org.springframework.data.domain.Pageable;

import dev.ime.application.usecases.query.GetAllProductQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllProductQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Product> readRepository;

	@InjectMocks
	private GetAllProductQueryHandler getAllProductQueryHandler;

	private GetAllProductQuery getAllQuery;
	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Product product01;
	private Product product02;
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

		getAllQuery = new GetAllProductQuery(pageRequest);
		
		category = new Category();
		category.setCategoryId(categoryId);
		category.setCategoryName(categoryName);
		
		product01 = new Product(productId01, productName01, productDescription01, category, new HashSet<>());
		product02 = new Product(productId02, productName02, productDescription02, category, new HashSet<>());
		
	}
	
	@Test
	void handle_shouldReturnFlux() {

		Mockito.when(readRepository.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(product01, product02));
		
		StepVerifier
		.create(getAllProductQueryHandler.handle(getAllQuery))
		.expectNext(product01)
		.expectNext(product02)
		.verifyComplete();

		Mockito.verify(readRepository).findAll(Mockito.any(Pageable.class));		
	}

}
