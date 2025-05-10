package dev.ime.api.endpoint;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.utils.JwtContextHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.port.inbound.CommandServicePort;
import dev.proto.CreateUserAppRequest;
import dev.proto.DeleteUserAppRequest;
import dev.proto.DeleteUserAppResponse;
import dev.proto.ReactorUserAppGrpcCommandServiceGrpc;
import dev.proto.UpdateUserAppRequest;
import dev.proto.UserAppCreatedResponse;
import dev.proto.UserAppProto;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class UserAppGrpcCommandServiceImpl extends ReactorUserAppGrpcCommandServiceGrpc.UserAppGrpcCommandServiceImplBase {

	private final CommandServicePort<UserAppDto> commandService;
	private final UserAppMapper userAppMapper;
	private final DtoValidator dtoValidator;
	private static final Logger logger = LoggerFactory.getLogger(UserAppGrpcCommandServiceImpl.class);
	
	public UserAppGrpcCommandServiceImpl(CommandServicePort<UserAppDto> commandService, UserAppMapper userAppMapper,
			DtoValidator dtoValidator) {
		super();
		this.commandService = commandService;
		this.userAppMapper = userAppMapper;
		this.dtoValidator = dtoValidator;
	}

	@Override
	public Mono<UserAppCreatedResponse> createUser(Mono<CreateUserAppRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_USERAPP, request);

		return request
				.map(r -> {
					dtoValidator.validateCreateUserAppRequest(r);
					return r;
				})
				.map(userAppMapper::fromCreateUserAppRequestToUserAppDto)
				.flatMap(commandService::create)
				.map(b -> UserAppCreatedResponse.newBuilder().setResult(true).build())
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.CREATE_USERAPP, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog);				
	}

	@Override
	public Mono<UserAppProto> updateUser(Mono<UpdateUserAppRequest> request) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_USERAPP, request);

		return request
				.map(r -> {
					dtoValidator.validateUpdateUserAppRequest(r);
					return r;
				})
				.map(userAppMapper::fromUpdateUserAppRequestToUserAppDto)
				.flatMap(commandService::update)
				.map(userAppMapper::fromEventToUserAppProto)
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.UPDATE_USERAPP, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));								
	}	

	@Override
	public Mono<DeleteUserAppResponse> deleteUser(Mono<DeleteUserAppRequest> request){
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_USERAPP, request);

		return request
				.map(r -> {
					dtoValidator.validateDeleteUserAppRequest(r);
					return r;
				})
				.map(r -> r.getUserAppId())
				.map(UUID::fromString)
				.flatMap(commandService::deleteById)
				.map(b -> DeleteUserAppResponse.newBuilder().setUserAppId(b.getEventData().toString()).setResult(true).build())
				.switchIfEmpty(Mono.error(new EmptyResponseException(Map.of(
						GlobalConstants.DELETE_USERAPP, GlobalConstants.MSG_NODATA
		            ))))				
				.transform(this::addFinalLog)
			    .contextWrite(reactor.util.context.Context.of(GlobalConstants.JWT_TOKEN, JwtContextHelper.getJwtToken()));								
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
