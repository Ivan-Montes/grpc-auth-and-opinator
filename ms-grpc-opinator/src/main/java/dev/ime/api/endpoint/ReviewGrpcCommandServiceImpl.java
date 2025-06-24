package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.ReviewDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.DeleteReviewResponse;
import dev.proto.ReactorReviewGrpcCommandServiceGrpc;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class ReviewGrpcCommandServiceImpl extends ReactorReviewGrpcCommandServiceGrpc.ReviewGrpcCommandServiceImplBase {

	private final CommandServicePort<ReviewDto> commandService;
	private final ReviewMapper mapper;
	private final ReviewRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(ReviewGrpcCommandServiceImpl.class);
	
	public ReviewGrpcCommandServiceImpl(CommandServicePort<ReviewDto> commandService, ReviewMapper mapper,
			ReviewRequestValidator requestValidator) {
		super();
		this.commandService = commandService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ReviewProto> createReview(Mono<CreateReviewRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_REV, request);

		return request
				.map(r -> {
					requestValidator.validateCreateRequest(r);
					return r;
				})
				.map(mapper::fromCreateToDto)
				.flatMap(commandService::create)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.CREATE_REV, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));				
	}

	@Override
	public Mono<ReviewProto> updateReview(Mono<UpdateReviewRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_REV, request);

		return request
				.map(r -> {
					requestValidator.validateUpdateRequest(r);
					return r;
				})
				.map(mapper::fromUpdateToDto)
				.flatMap(commandService::update)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.UPDATE_REV, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));	
	}	

	@Override
	public Mono<DeleteReviewResponse> deleteReview(Mono<DeleteReviewRequest> request){
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_REV, request);

		return request
				.map(r -> {
					requestValidator.validateDeleteRequest(r);
					return r;
				})
				.map(r -> r.getReviewId())
				.map(UUID::fromString)
				.flatMap(commandService::deleteById)
				.map(event -> {
					String id = extractValueFromEventData(event, GlobalConstants.REV_ID);
					return DeleteReviewResponse.newBuilder().setReviewId(id).setSuccess(BoolValue.of(true)).build();
				})
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.DELETE_REV, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));	
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
