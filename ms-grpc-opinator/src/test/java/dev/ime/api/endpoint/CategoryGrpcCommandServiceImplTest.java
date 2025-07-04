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

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CategoryProto;
import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.UpdateCategoryRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CategoryGrpcCommandServiceImplTest {

	@Mock
	private CommandServicePort<CategoryDto> commandService;
	@Mock
	private CategoryMapper mapper;
	@Mock
	private CategoryRequestValidator requestValidator;

	@InjectMocks
	private CategoryGrpcCommandServiceImpl categoryGrpcCommandServiceImpl;

	private CategoryDto categoryDto;
	private CategoryProto categoryProto;
	private CreateCategoryRequest createRequest;
	private UpdateCategoryRequest updateRequest;
	private DeleteCategoryRequest deleteRequest;
	private Event event;
	
	private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.CAT_CAT;
	private final String eventType = GlobalConstants.CAT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	@BeforeEach
	private void setUp() {
		
		categoryDto = new CategoryDto(categoryId,categoryName);
		
		categoryProto = CategoryProto.newBuilder()
				.setCategoryId(categoryId.toString())
				.setCategoryName(categoryName)
				.build();

		createRequest = CreateCategoryRequest.newBuilder()
				.setCategoryName(categoryName)
				.build();

		updateRequest = UpdateCategoryRequest.newBuilder()
				.setCategoryId(categoryId.toString())
				.setCategoryName(categoryName)
				.build();

		deleteRequest = DeleteCategoryRequest.newBuilder()
				.setCategoryId(categoryId.toString())
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

		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateCategoryRequest.class));
		Mockito.when(mapper.fromCreateToDto(Mockito.any(CreateCategoryRequest.class))).thenReturn(categoryDto);
		Mockito.when(commandService.create(Mockito.any(CategoryDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(categoryProto);

		StepVerifier.create(categoryGrpcCommandServiceImpl.createCategory(createRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response).isEqualTo(categoryProto));
		}).verifyComplete();

		Mockito.verify(mapper).fromCreateToDto(Mockito.any(CreateCategoryRequest.class));
		Mockito.verify(commandService).create(Mockito.any(CategoryDto.class));
		Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
	}

	@Test
	void update_shouldReturnProto() {

		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateCategoryRequest.class));
		Mockito.when(mapper.fromUpdateToDto(Mockito.any(UpdateCategoryRequest.class))).thenReturn(categoryDto);
		Mockito.when(commandService.update(Mockito.any(CategoryDto.class))).thenReturn(Mono.just(event));
		Mockito.when(mapper.fromEventToProto(Mockito.any(Event.class))).thenReturn(categoryProto);

		StepVerifier.create(categoryGrpcCommandServiceImpl.updateCategory(updateRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response).isEqualTo(categoryProto));
		}).verifyComplete();

		Mockito.verify(mapper).fromUpdateToDto(Mockito.any(UpdateCategoryRequest.class));
		Mockito.verify(commandService).update(Mockito.any(CategoryDto.class));
		Mockito.verify(mapper).fromEventToProto(Mockito.any(Event.class));
	}

	@Test
	void deleteById_shouldReturnDeleteResponse() {

		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteCategoryRequest.class));
		Mockito.when(commandService.deleteById(Mockito.any(UUID.class))).thenReturn(Mono.just(event));

		StepVerifier.create(categoryGrpcCommandServiceImpl.deleteCategory(deleteRequest)).assertNext(response -> {
			org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(response).isNotNull(),
					() -> Assertions.assertThat(response.hasSuccess()).isTrue());
		}).verifyComplete();

		Mockito.verify(commandService).deleteById(Mockito.any(UUID.class));
	}
	
}
