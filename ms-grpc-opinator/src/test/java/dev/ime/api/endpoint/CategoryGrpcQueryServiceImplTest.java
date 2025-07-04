package dev.ime.api.endpoint;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.google.protobuf.Empty;

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.CategoryProto;
import dev.proto.GetCategoryRequest;
import dev.proto.PaginationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CategoryGrpcQueryServiceImplTest {

	@Mock
	private QueryServicePort<CategoryDto> queryService;
	@Mock
	private CategoryMapper mapper;
	@Mock
	private CategoryRequestValidator requestValidator;

	@InjectMocks
	private CategoryGrpcQueryServiceImpl categoryGrpcQueryServiceImpl;

	private CategoryDto categoryDto;
	private CategoryProto categoryProto;
	private PaginationRequest paginationRequest;	
	private GetCategoryRequest getCategoryRequest;
	
	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	@BeforeEach
	private void setUp() {
		
		categoryDto = new CategoryDto(categoryId,categoryName);
		
		categoryProto = CategoryProto.newBuilder()
				.setCategoryId(categoryId.toString())
				.setCategoryName(categoryName)
				.build();

		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.CAT_NAME)
				.setSortDir(GlobalConstants.PS_D)
				.build();
		
		getCategoryRequest = GetCategoryRequest.newBuilder()
				.setCategoryId(categoryId.toString())
				.build();
	}

	@Test
	void listCategories_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(categoryDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(categoryProto));
	
		StepVerifier
		.create(categoryGrpcQueryServiceImpl.listCategories(Empty.getDefaultInstance()))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getCategoriesCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}
	
	@Test
	void listCategoriesPaginated_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(categoryDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(categoryProto));
	
		StepVerifier
		.create(categoryGrpcQueryServiceImpl.listCategoriesPaginated(paginationRequest))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getCategoriesCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}

	@Test
	void getCategory_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetCategoryRequest.class));
		Mockito.when(queryService.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(categoryDto));
		Mockito.when(mapper.fromDtoToProto(Mockito.any(CategoryDto.class)))
				.thenReturn(categoryProto);

		StepVerifier.create(categoryGrpcQueryServiceImpl.getCategory(getCategoryRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getCategoryId()).isEqualTo(categoryId.toString()));
		}).verifyComplete();

		Mockito.verify(queryService).findById(Mockito.any(UUID.class));
		Mockito.verify(mapper).fromDtoToProto(Mockito.any(CategoryDto.class));
	}
	
}
