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

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.ProductDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetProductRequest;
import dev.proto.PaginationRequest;
import dev.proto.ProductProto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductGrpcQueryServiceImplTest {

	@Mock
	private QueryServicePort<ProductDto> queryService;
	@Mock
	private ProductMapper mapper;
	@Mock
	private ProductRequestValidator requestValidator;

	@InjectMocks
	private ProductGrpcQueryServiceImpl productGrpcQueryServiceImpl;

	private ProductDto productDto;
	private ProductProto productProto;
	private PaginationRequest paginationRequest;	
	private GetProductRequest getProductRequest;

	private final UUID productId = UUID.randomUUID();
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final UUID categoryId = UUID.randomUUID();

	@BeforeEach
	private void setUp() {
		
		productDto = new ProductDto(productId, productName, productDescription, categoryId);
		
		productProto = ProductProto.newBuilder()
				.setProductId(productId.toString())
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId.toString())
				.build();

		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.PROD_NAME)
				.setSortDir(GlobalConstants.PS_D)
				.build();
		
		getProductRequest = GetProductRequest.newBuilder()
				.setProductId(productId.toString())
				.build();
	}

	@Test
	void listProducts_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(productDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(productProto));
	
		StepVerifier
		.create(productGrpcQueryServiceImpl.listProducts(Empty.getDefaultInstance()))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getProductsCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}
	
	@Test
	void listProductsPaginated_shouldReturnList() {
		
		Mockito.when(queryService.findAll(Mockito.any(Pageable.class))).thenReturn(Flux.just(productDto));
		Mockito.when(mapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(productProto));
	
		StepVerifier
		.create(productGrpcQueryServiceImpl.listProductsPaginated(paginationRequest))
		.assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(
					() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getProductsCount()).isEqualTo(1));
		}).verifyComplete();
		
		Mockito.verify(queryService).findAll(Mockito.any(Pageable.class));
		Mockito.verify(mapper).fromListDtoToListProto(Mockito.anyList());	
	}

	@Test
	void getProduct_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetProductRequest.class));
		Mockito.when(queryService.findById(Mockito.any(UUID.class))).thenReturn(Mono.just(productDto));
		Mockito.when(mapper.fromDtoToProto(Mockito.any(ProductDto.class)))
				.thenReturn(productProto);

		StepVerifier.create(productGrpcQueryServiceImpl.getProduct(getProductRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.getCategoryId()).isEqualTo(categoryId.toString()));
		}).verifyComplete();

		Mockito.verify(queryService).findById(Mockito.any(UUID.class));
		Mockito.verify(mapper).fromDtoToProto(Mockito.any(ProductDto.class));
	}
	
}
