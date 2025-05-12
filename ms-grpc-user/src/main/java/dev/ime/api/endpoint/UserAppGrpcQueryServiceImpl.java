package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetUserAppRequest;
import dev.proto.ListUsersAppResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReactorUserAppGrpcQueryServiceGrpc;
import dev.proto.UserAppProto;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class UserAppGrpcQueryServiceImpl extends ReactorUserAppGrpcQueryServiceGrpc.UserAppGrpcQueryServiceImplBase {

	private final QueryServicePort<UserAppDto> queryService;
	private final UserAppMapper userAppMapper;
	private final DtoValidator dtoValidator;
	
	public UserAppGrpcQueryServiceImpl(QueryServicePort<UserAppDto> queryService, UserAppMapper userAppMapper,
			DtoValidator dtoValidator) {
		super();
		this.queryService = queryService;
		this.userAppMapper = userAppMapper;
		this.dtoValidator = dtoValidator;
	}

	@Override
	public Mono<ListUsersAppResponse> listUsers(Mono<Empty> request) {
		
		return request				
				.map(r -> defaultPaginationDto())
				.map(this::createPageable)
				.flatMapMany(queryService::findAll)
				.collectList()
				.map(userAppMapper::fromListUserAppDtoToListUserAppProto)
				.map(protoList -> ListUsersAppResponse.newBuilder().addAllUsers(protoList).build());
	}

	@Override
	public Mono<ListUsersAppResponse> listUsersPaginated(Mono<PaginationRequest> request) {

		return request				
				.map(this::createPaginationDto)
				.map(this::createPageable)
				.flatMapMany(queryService::findAll)
				.collectList()
				.map(userAppMapper::fromListUserAppDtoToListUserAppProto)
				.map(protoList -> ListUsersAppResponse.newBuilder().addAllUsers(protoList).build());
	}	
	
	private PaginationDto defaultPaginationDto() {
		
		return createPaginationDto(PaginationRequest.newBuilder().setPage(0).setSize(1000).setSortBy(GlobalConstants.USERAPP_ID).setSortDir(GlobalConstants.PS_A).build());
		
	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.USERAPP_ID,GlobalConstants.USERAPP_EMAIL,GlobalConstants.USERAPP_NAME, GlobalConstants.USERAPP_LASTNAME);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.USERAPP_ID);
        String sortDir = Optional.ofNullable(request.getSortDir())
        		.map(String::toUpperCase)
        		.filter( sorting -> sorting.equals(GlobalConstants.PS_A) || sorting.equals(GlobalConstants.PS_D))
        		.orElse(GlobalConstants.PS_A);
        
        return new PaginationDto(page, size, sortBy, sortDir);
	}
	
	private PageRequest createPageable(PaginationDto paginationDto) {

		Sort sort = Sort.by(Sort.Direction.fromString(paginationDto.sortDir()), paginationDto.sortBy());
		return PageRequest.of(paginationDto.page(), paginationDto.size(), sort);
	}

	@Override
	public Mono<UserAppProto> getUser(Mono<GetUserAppRequest> request) {
		
		return request
				.map(r -> {
					dtoValidator.validateGetUserAppRequest(r);
					return r;
				})
				.map(r -> r.getUserAppId())
				.map(UUID::fromString)
				.flatMap(queryService::findById)
				.map(userAppMapper::fromUserAppDtoToUserAppProto)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException(Map.of(
						GlobalConstants.USERAPP_CAT, request.toString()
		            ))));
	}	
	
}
