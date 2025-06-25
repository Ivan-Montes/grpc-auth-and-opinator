package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.VoteDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.DeleteVoteResponse;
import dev.proto.ReactorVoteGrpcCommandServiceGrpc;
import dev.proto.UpdateVoteRequest;
import dev.proto.VoteProto;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class VoteGrpcCommandServiceImpl extends ReactorVoteGrpcCommandServiceGrpc.VoteGrpcCommandServiceImplBase {

	private final CommandServicePort<VoteDto> commandService;
	private final VoteMapper mapper;
	private final VoteRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(VoteGrpcCommandServiceImpl.class);
	
	public VoteGrpcCommandServiceImpl(CommandServicePort<VoteDto> commandService, VoteMapper mapper,
			VoteRequestValidator requestValidator) {
		super();
		this.commandService = commandService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<VoteProto> createVote(Mono<CreateVoteRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_VOT, request);

		return request
				.map(r -> {
					requestValidator.validateCreateRequest(r);
					return r;
				})
				.map(mapper::fromCreateToDto)
				.flatMap(commandService::create)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.CREATE_VOT, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));				
	}

	@Override
	public Mono<VoteProto> updateVote(Mono<UpdateVoteRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_VOT, request);

		return request
				.map(r -> {
					requestValidator.validateUpdateRequest(r);
					return r;
				})
				.map(mapper::fromUpdateToDto)
				.flatMap(commandService::update)
				.map(mapper::fromEventToProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.UPDATE_VOT, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));	
	}	

	@Override
	public Mono<DeleteVoteResponse> deleteVote(Mono<DeleteVoteRequest> request){
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_VOT, request);

		return request
				.map(r -> {
					requestValidator.validateDeleteRequest(r);
					return r;
				})
				.map(r -> r.getVoteId())
				.map(UUID::fromString)
				.flatMap(commandService::deleteById)
				.map(event -> {
					String id = extractValueFromEventData(event, GlobalConstants.VOT_ID);
					return DeleteVoteResponse.newBuilder().setVoteId(id).setSuccess(BoolValue.of(true)).build();
				})
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.DELETE_VOT, GlobalConstants.MSG_NODATA
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
