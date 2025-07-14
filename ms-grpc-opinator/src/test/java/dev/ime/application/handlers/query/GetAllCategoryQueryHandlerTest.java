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

import dev.ime.application.usecases.query.GetAllCategoryQuery;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.ReadRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class GetAllCategoryQueryHandlerTest {

	@Mock
	private ReadRepositoryPort<Category> readRepository;

	@InjectMocks
	private GetAllCategoryQueryHandler getAllCategoryQueryHandler;

	private GetAllCategoryQuery getAllQuery;
	private final PageRequest pageRequest = PageRequest.of(0, 100);
	private Category category01;
	private Category category02;

	private final UUID categoryId01 = UUID.randomUUID();
	private final String categoryName01 = "Vegetables";
	
	private final UUID categoryId02 = UUID.randomUUID();
	private final String categoryName02 = "NoVegetables";

	@BeforeEach
	private void setUp() {

		getAllQuery = new GetAllCategoryQuery(pageRequest);

		category01 = new Category(categoryId01, categoryName01, new HashSet<>());
		category02 = new Category(categoryId02, categoryName02, new HashSet<>());
	
	}

	@Test
	void handle_shouldReturnFlux() {

		Mockito.when(readRepository.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(category01, category02));
		
		StepVerifier
		.create(getAllCategoryQueryHandler.handle(getAllQuery))
		.expectNext(category01)
		.expectNext(category02)
		.verifyComplete();

		Mockito.verify(readRepository).findAll(Mockito.any(Pageable.class));		
	}

}
