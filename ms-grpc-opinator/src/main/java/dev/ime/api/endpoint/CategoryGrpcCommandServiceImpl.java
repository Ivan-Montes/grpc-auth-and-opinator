package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CategoryProto;
import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.DeleteCategoryResponse;
import dev.proto.ReactorCategoryGrpcCommandServiceGrpc;
import dev.proto.UpdateCategoryRequest;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class CategoryGrpcCommandServiceImpl extends ReactorCategoryGrpcCommandServiceGrpc.CategoryGrpcCommandServiceImplBase {

	private final CommandServicePort<CategoryDto> commandService;
	private final CategoryMapper mapper;
	private final CategoryRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(CategoryGrpcCommandServiceImpl.class);
	
	public CategoryGrpcCommandServiceImpl(CommandServicePort<CategoryDto> commandService, CategoryMapper mapper,
			CategoryRequestValidator requestValidator) {
		super();
		this.commandService = commandService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<CategoryProto> createCategory(Mono<CreateCategoryRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_CAT, request);

		return request
				.map(r -> {
					requestValidator.validateCreateRequest(r);
					return r;
				})
				.map(mapper::fromCreateToDto)
				.flatMap(commandService::create)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.CREATE_CAT, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);				
	}

	@Override
	public Mono<CategoryProto> updateCategory(Mono<UpdateCategoryRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_CAT, request);

		return request
				.map(r -> {
					requestValidator.validateUpdateRequest(r);
					return r;
				})
				.map(mapper::fromUpdateToDto)
				.flatMap(commandService::update)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.UPDATE_CAT, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);
	}	

	@Override
	public Mono<DeleteCategoryResponse> deleteCategory(Mono<DeleteCategoryRequest> request){
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_CAT, request);

		return request
				.map(r -> {
					requestValidator.validateDeleteRequest(r);
					return r;
				})
				.map(r -> r.getCategoryId())
				.map(UUID::fromString)
				.flatMap(commandService::deleteById)
				.map(event -> {
					String id = extractValueFromEventData(event, GlobalConstants.CAT_ID);
					return DeleteCategoryResponse.newBuilder().setCategoryId(id).setSuccess(BoolValue.of(true)).build();
				})
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.DELETE_CAT, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);
	}
	
	private String extractValueFromEventData(Event event, String key) {
		
		return Optional.ofNullable(event.getEventData())
				.map( eventData -> eventData.get(key))
	                   .map(Object::toString)
	                   .orElse(GlobalConstants.MSG_NODATA);
	}
	
	private <T> Mono<T> addFinalLog(Mono<T> reactiveFlow) {
		
		return reactiveFlow
				.doOnSubscribe( subscribed -> logInfo( GlobalConstants.MSG_FLOW_SUBS, subscribed.toString()) )
				.doOnSuccess( success -> logInfo( GlobalConstants.MSG_FLOW_OK, createExtraInfo(success) ))
	            .doOnCancel( () -> logInfo( GlobalConstants.MSG_FLOW_CANCEL, GlobalConstants.MSG_NODATA) )
	            .doOnError( error -> logInfo( GlobalConstants.MSG_FLOW_ERROR, error.toString()) )
		        .doFinally( signal -> logInfo( GlobalConstants.MSG_FLOW_RESULT, signal.toString()) );				
	}
	
	private void logInfo(String action, String extraInfo) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, action, extraInfo);
	}
	
	private <T> String createExtraInfo(T response) {
		
		return response instanceof Number? GlobalConstants.MSG_MODLINES + response.toString():response.toString();				
	}		
	
}
