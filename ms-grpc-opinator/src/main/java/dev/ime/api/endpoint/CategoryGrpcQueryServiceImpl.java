package dev.ime.api.endpoint;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.CategoryProto;
import dev.proto.GetCategoryRequest;
import dev.proto.ListCategoriesResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReactorCategoryGrpcQueryServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class CategoryGrpcQueryServiceImpl extends ReactorCategoryGrpcQueryServiceGrpc.CategoryGrpcQueryServiceImplBase {

	private final QueryServicePort<CategoryDto> queryService;
	private final CategoryMapper mapper;
	private final CategoryRequestValidator requestValidator;

	public CategoryGrpcQueryServiceImpl(QueryServicePort<CategoryDto> queryService, CategoryMapper mapper,
			CategoryRequestValidator requestValidator) {
		super();
		this.queryService = queryService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ListCategoriesResponse> listCategories(Mono<Empty> request) {

		return request.map(r -> defaultPaginationDto()).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListCategoriesResponse.newBuilder().addAllCategories(protoList).build());
	}

	@Override
	public Mono<ListCategoriesResponse> listCategoriesPaginated(Mono<PaginationRequest> request) {

		return request.map(this::createPaginationDto).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListCategoriesResponse.newBuilder().addAllCategories(protoList).build());
	}

	private PaginationDto defaultPaginationDto() {

		return createPaginationDto(PaginationRequest.newBuilder().setPage(0).setSize(1000)
				.setSortBy(GlobalConstants.CAT_ID).setSortDir(GlobalConstants.PS_A).build());

	}

	private PaginationDto createPaginationDto(PaginationRequest request) {

		Set<String> attrsSet = Set.of(GlobalConstants.CAT_ID, GlobalConstants.CAT_NAME);
		Integer page = Optional.ofNullable(request.getPage()).filter(i -> i >= 0).orElse(0);
		Integer size = Optional.ofNullable(request.getSize()).filter(i -> i > 0).orElse(100);
		String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains)
				.orElse(GlobalConstants.CAT_NAME);
		String sortDir = Optional.ofNullable(request.getSortDir()).map(String::toUpperCase)
				.filter(sorting -> sorting.equals(GlobalConstants.PS_A) || sorting.equals(GlobalConstants.PS_D))
				.orElse(GlobalConstants.PS_A);

		return new PaginationDto(page, size, sortBy, sortDir);
	}

	private PageRequest createPageable(PaginationDto paginationDto) {

		Sort sort = Sort.by(Sort.Direction.fromString(paginationDto.sortDir()), paginationDto.sortBy());
		return PageRequest.of(paginationDto.page(), paginationDto.size(), sort);
	}

	@Override
	public Mono<CategoryProto> getCategory(Mono<GetCategoryRequest> request) {

		return request.map(r -> {
			requestValidator.validateGetRequest(r);
			return r;
		}).map(r -> r.getCategoryId()).map(UUID::fromString).flatMap(queryService::findById)
				.map(mapper::fromDtoToProto).switchIfEmpty(Mono.defer(() -> {
					String id = request.block().getCategoryId();
					return Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.CAT_CAT, id)));
				}));
	}

}
