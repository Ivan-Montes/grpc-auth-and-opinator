package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.ReviewDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetReviewRequest;
import dev.proto.ListReviewsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReactorReviewGrpcQueryServiceGrpc;
import dev.proto.ReviewProto;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class ReviewGrpcQueryServiceImpl extends ReactorReviewGrpcQueryServiceGrpc.ReviewGrpcQueryServiceImplBase {

	private final QueryServicePort<ReviewDto> queryService;
	private final ReviewMapper mapper;
	private final ReviewRequestValidator requestValidator;
	
	public ReviewGrpcQueryServiceImpl(QueryServicePort<ReviewDto> queryService, ReviewMapper mapper,
			ReviewRequestValidator requestValidator) {
		super();
		this.queryService = queryService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ListReviewsResponse> listReviews(Mono<Empty> request) {

		return request.map(r -> defaultPaginationDto()).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListReviewsResponse.newBuilder().addAllReviews(protoList).build());
	}

	@Override
	public Mono<ListReviewsResponse> listReviewsPaginated(Mono<PaginationRequest> request) {

		return request.map(this::createPaginationDto).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListReviewsResponse.newBuilder().addAllReviews(protoList).build());
	}

	private PaginationDto defaultPaginationDto() {

		return createPaginationDto(PaginationRequest.newBuilder().setPage(0).setSize(1000)
				.setSortBy(GlobalConstants.REV_ID).setSortDir(GlobalConstants.PS_A).build());

	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.REV_ID,GlobalConstants.USER_EMAIL,GlobalConstants.REV_TXT, GlobalConstants.REV_RAT);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.REV_ID);
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
	public Mono<ReviewProto> getReview(Mono<GetReviewRequest> request) {

		return request.map(r -> {
			requestValidator.validateGetRequest(r);
			return r;
		}).map(r -> r.getReviewId()).map(UUID::fromString).flatMap(queryService::findById)
				.map(mapper::fromDtoToProto).switchIfEmpty(Mono.defer(() -> {
					String id = request.block().getReviewId();
					return Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.REV_CAT, id)));
				}));
	}

}
