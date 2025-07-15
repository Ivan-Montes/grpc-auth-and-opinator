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

import dev.ime.application.usecases.query.GetByIdProductQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetByIdProductQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Product> readRepository;

	@InjectMocks
	private GetByIdProductQueryHandler getByIdProductQueryHandler;

	private GetByIdProductQuery getByIdQuery;
	private Product product;
	private Category category;

	private final UUID productId = UUID.randomUUID();
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	
	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	@BeforeEach
	private void setUp() {

		getByIdQuery = new GetByIdProductQuery(productId);
		
		category = new Category();
		category.setCategoryId(categoryId);
		category.setCategoryName(categoryName);
		
		product = new Product(productId, productName, productDescription, category, new HashSet<>());
		
	}

	@Test
	void handle_shouldReturnDomainObject() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(product));
		
		StepVerifier
		.create(getByIdProductQueryHandler.handle(getByIdQuery))
		.expectNext(product)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

}
