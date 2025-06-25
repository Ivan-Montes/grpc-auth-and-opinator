package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.VoteDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetVoteRequest;
import dev.proto.ListVotesResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReactorVoteGrpcQueryServiceGrpc;
import dev.proto.VoteProto;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class VoteGrpcQueryServiceImpl extends ReactorVoteGrpcQueryServiceGrpc.VoteGrpcQueryServiceImplBase {

	private final QueryServicePort<VoteDto> queryService;
	private final VoteMapper mapper;
	private final VoteRequestValidator requestValidator;
	
	public VoteGrpcQueryServiceImpl(QueryServicePort<VoteDto> queryService, VoteMapper mapper,
			VoteRequestValidator requestValidator) {
		super();
		this.queryService = queryService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ListVotesResponse> listVotes(Mono<Empty> request) {

		return request.map(r -> defaultPaginationDto()).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListVotesResponse.newBuilder().addAllVotes(protoList).build());
	}

	@Override
	public Mono<ListVotesResponse> listVotesPaginated(Mono<PaginationRequest> request) {

		return request.map(this::createPaginationDto).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListVotesResponse.newBuilder().addAllVotes(protoList).build());
	}

	private PaginationDto defaultPaginationDto() {

		return createPaginationDto(PaginationRequest.newBuilder().setPage(0).setSize(1000)
				.setSortBy(GlobalConstants.VOT_ID).setSortDir(GlobalConstants.PS_A).build());

	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.VOT_ID,GlobalConstants.USER_EMAIL,GlobalConstants.VOT_US);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.VOT_ID);
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
	public Mono<VoteProto> getVote(Mono<GetVoteRequest> request) {

		return request.map(r -> {
			requestValidator.validateGetRequest(r);
			return r;
		}).map(r -> r.getVoteId()).map(UUID::fromString).flatMap(queryService::findById)
				.map(mapper::fromDtoToProto).switchIfEmpty(Mono.defer(() -> {
					String id = request.block().getVoteId();
					return Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.VOT_CAT, id)));
				}));
	}
	
}
