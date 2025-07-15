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

import dev.ime.application.usecases.query.GetByIdCategoryQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetByIdCategoryQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Category> readRepository;

	@InjectMocks
	private GetByIdCategoryQueryHandler getByIdCategoryQueryHandler;

	private GetByIdCategoryQuery getByIdQuery;
	private Category category;

	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	@BeforeEach
	private void setUp() {

		getByIdQuery = new GetByIdCategoryQuery(categoryId);

		category = new Category(categoryId, categoryName, new HashSet<>());
	
	}

	@Test
	void handle_shouldReturnDomainObject() {

		Mockito.when(readRepository.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(category));
		
		StepVerifier
		.create(getByIdCategoryQueryHandler.handle(getByIdQuery))
		.expectNext(category)
		.verifyComplete();

		Mockito.verify(readRepository).findById(Mockito.any(UUID.class));		
	}

}
