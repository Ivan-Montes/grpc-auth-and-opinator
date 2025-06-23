package dev.ime.api.endpoint;


import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.ProductDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.port.inbound.QueryServicePort;
import dev.proto.GetProductRequest;
import dev.proto.ListProductsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ProductProto;
import dev.proto.ReactorProductGrpcQueryServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class ProductGrpcQueryServiceImpl extends ReactorProductGrpcQueryServiceGrpc.ProductGrpcQueryServiceImplBase {

	private final QueryServicePort<ProductDto> queryService;
	private final ProductMapper mapper;
	private final ProductRequestValidator requestValidator;
	
	public ProductGrpcQueryServiceImpl(QueryServicePort<ProductDto> queryService, ProductMapper mapper,
			ProductRequestValidator requestValidator) {
		super();
		this.queryService = queryService;
		this.mapper = mapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public Mono<ListProductsResponse> listProducts(Mono<Empty> request) {

		return request.map(r -> defaultPaginationDto()).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListProductsResponse.newBuilder().addAllProducts(protoList).build());
	}

	@Override
	public Mono<ListProductsResponse> listProductsPaginated(Mono<PaginationRequest> request) {

		return request.map(this::createPaginationDto).map(this::createPageable).flatMapMany(queryService::findAll)
				.collectList().map(mapper::fromListDtoToListProto)
				.map(protoList -> ListProductsResponse.newBuilder().addAllProducts(protoList).build());
	}

	private PaginationDto defaultPaginationDto() {

		return createPaginationDto(PaginationRequest.newBuilder().setPage(0).setSize(1000)
				.setSortBy(GlobalConstants.PROD_ID).setSortDir(GlobalConstants.PS_A).build());

	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.PROD_ID,GlobalConstants.PROD_NAME,GlobalConstants.PROD_DESC);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.PROD_ID);
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
	public Mono<ProductProto> getProduct(Mono<GetProductRequest> request) {

		return request.map(r -> {
			requestValidator.validateGetRequest(r);
			return r;
		}).map(r -> r.getProductId()).map(UUID::fromString).flatMap(queryService::findById)
				.map(mapper::fromDtoToProto).switchIfEmpty(Mono.defer(() -> {
					String id = request.block().getProductId();
					return Mono.error(new ResourceNotFoundException(Map.of(GlobalConstants.PROD_CAT, id)));
				}));
	}

}
