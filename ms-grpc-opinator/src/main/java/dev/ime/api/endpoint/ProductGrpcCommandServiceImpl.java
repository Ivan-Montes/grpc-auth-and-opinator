package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.ProductDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.ProductProto;
import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.DeleteProductResponse;
import dev.proto.ReactorProductGrpcCommandServiceGrpc;
import dev.proto.UpdateProductRequest;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class ProductGrpcCommandServiceImpl extends ReactorProductGrpcCommandServiceGrpc.ProductGrpcCommandServiceImplBase {

	private final CommandServicePort<ProductDto> commandService;
	private final ProductMapper mapper;
	private final ProductRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(ProductGrpcCommandServiceImpl.class);
	
	public ProductGrpcCommandServiceImpl(CommandServicePort<ProductDto> commandService, ProductMapper mapper,
			ProductRequestValidator requestValidator) {
		super();
		this.commandService = commandService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ProductProto> createProduct(Mono<CreateProductRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_PROD, request);

		return request
				.map(r -> {
					requestValidator.validateCreateRequest(r);
					return r;
				})
				.map(mapper::fromCreateToDto)
				.flatMap(commandService::create)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.CREATE_PROD, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);				
	}

	@Override
	public Mono<ProductProto> updateProduct(Mono<UpdateProductRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_PROD, request);

		return request
				.map(r -> {
					requestValidator.validateUpdateRequest(r);
					return r;
				})
				.map(mapper::fromUpdateToDto)
				.flatMap(commandService::update)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.UPDATE_PROD, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);
	}	

	@Override
	public Mono<DeleteProductResponse> deleteProduct(Mono<DeleteProductRequest> request){
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_PROD, request);

		return request
				.map(r -> {
					requestValidator.validateDeleteRequest(r);
					return r;
				})
				.map(r -> r.getProductId())
				.map(UUID::fromString)
				.flatMap(commandService::deleteById)
				.map(event -> {
					String id = extractValueFromEventData(event, GlobalConstants.PROD_ID);
					return DeleteProductResponse.newBuilder().setProductId(id).setSuccess(BoolValue.of(true)).build();
				})
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.DELETE_PROD, GlobalConstants.MSG_NODATA
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
