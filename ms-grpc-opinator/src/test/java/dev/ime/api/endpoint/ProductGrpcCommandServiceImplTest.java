package dev.ime.api.endpoint;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.ProductDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.ProductProto;
import dev.proto.UpdateProductRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProductGrpcCommandServiceImplTest {

	@Mock
	private CommandServicePort<ProductDto> commandService;
	@Mock
	private ProductMapper mapper;
	@Mock
	private ProductRequestValidator requestValidator;

	@InjectMocks
	private ProductGrpcCommandServiceImpl productGrpcCommandServiceImpl;

	private ProductDto productDto;
	private ProductProto productProto;
	private CreateProductRequest createRequest;
	private UpdateProductRequest updateRequest;
	private DeleteProductRequest deleteRequest;
	private Event event;

	private final UUID productId = UUID.randomUUID();
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final UUID categoryId = UUID.randomUUID();

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.PROD_CAT;
	private final String eventType = GlobalConstants.PROD_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {

		productDto = new ProductDto(productId, productName, productDescription, categoryId);

		productProto = ProductProto.newBuilder()
				.setProductId(productId.toString())
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId.toString())
				.build();

		createRequest = CreateProductRequest.newBuilder()
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId.toString())
				.build();

		updateRequest = UpdateProductRequest.newBuilder()
				.setProductId(productId.toString())
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId.toString())
				.build();

		deleteRequest = DeleteProductRequest.newBuilder()
				.setProductId(productId.toString())
				.build();

		event = new Event(
				eventId, 
				eventCategory, 
				eventType, 
				eventTimestamp, 
				eventData);
	}

	@Test
	void create_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateProductRequest.class));
		Mockito.when(mapper.fromCreateToDto(Mockito.any(CreateProductRequest.class))).thenReturn(productDto);
		Mockito.when(commandService.create(Mockito.any(ProductDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(productProto);

		StepVerifier.create(productGrpcCommandServiceImpl.createProduct(createRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response).isEqualTo(productProto));
		}).verifyComplete();

		Mockito.verify(mapper).fromCreateToDto(Mockito.any(CreateProductRequest.class));
		Mockito.verify(commandService).create(Mockito.any(ProductDto.class));
		Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
	}

	@Test
	void update_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateProductRequest.class));
		Mockito.when(mapper.fromUpdateToDto(Mockito.any(UpdateProductRequest.class))).thenReturn(productDto);
		Mockito.when(commandService.update(Mockito.any(ProductDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(productProto);

		StepVerifier.create(productGrpcCommandServiceImpl.updateProduct(updateRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response).isEqualTo(productProto));
		}).verifyComplete();

		Mockito.verify(mapper).fromUpdateToDto(Mockito.any(UpdateProductRequest.class));
		Mockito.verify(commandService).update(Mockito.any(ProductDto.class));
		Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
	}

	@Test
	void deleteById_shouldReturnDeleteResponse() {

		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteProductRequest.class));
		Mockito.when(commandService.deleteById(Mockito.any(UUID.class))).thenReturn(Mono.just(event));

		StepVerifier.create(productGrpcCommandServiceImpl.deleteProduct(deleteRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.hasSuccess()).isTrue());
		}).verifyComplete();

		Mockito.verify(commandService).deleteById(Mockito.any(UUID.class));
	}

}
