package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.ime.application.dto.ProductDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.proto.CreateProductRequest;
import dev.proto.ProductProto;
import dev.proto.UpdateProductRequest;

@Component
public class ProductMapper {

	public ProductDto fromCreateToDto(CreateProductRequest request) {
	
		return new ProductDto(
				null,
				request.getProductName(),
				request.getProductDescription(),
				UUID.fromString(request.getCategoryId())
				);		
	}	

	public ProductDto fromUpdateToDto(UpdateProductRequest request) {
		
		return new ProductDto(
				UUID.fromString(request.getProductId()),
				request.getProductName(),
				request.getProductDescription(),
				UUID.fromString(request.getCategoryId())
				);		
	}	

	public Product fromDtoToDomain(ProductDto dto) {
		
		Category category = new Category();
		category.setCategoryId(dto.categoryId());
		
		return new Product(
				dto.productId(),
				dto.productName(),
				dto.productDescription(),
				category,
				new HashSet<>()
				); 			
	}
	
	public ProductJpaEntity fromDomainToJpa(Product dom, CategoryJpaEntity categoryJpaEntity) {
		
		ProductJpaEntity productJpaEntity = new ProductJpaEntity(
				dom.getProductId(),
				dom.getProductName(),
				dom.getProductDescription(),
				categoryJpaEntity,
				new HashSet<>()		
				);		
	    categoryJpaEntity.getProducts().add(productJpaEntity);
		
		return productJpaEntity;
	}

	public Product fromJpaToDomain(ProductJpaEntity entity) {
		
		Category category = fromCategoryJpaToCategoryDomain(entity);		
				
		Product product = new Product(
				entity.getProductId(),
				entity.getProductName(),
				entity.getProductDescription(),
				category,
				null
				);
		category.addProduct(product);
		Set<Review> reviews = fromSetReviewJpaToSetReviewDom(entity.getReviews(), product);
		product.setReviews(reviews);
				
		return product;
	}
	
	public Category fromCategoryJpaToCategoryDomain(ProductJpaEntity entity) {

		CategoryJpaEntity catEntity = entity.getCategory();
		return new Category(
				catEntity.getCategoryId(),
				catEntity.getCategoryName(),
				new HashSet<>()
				);
	}
	
	private Review fromReviewJpaToReviewDom(ReviewJpaEntity reviewJpaEntity, Product product) {
		
		return new Review(
				reviewJpaEntity.getReviewId(),
				reviewJpaEntity.getEmail(),
				product,
				reviewJpaEntity.getReviewText(),
				reviewJpaEntity.getRating(),
				new HashSet<>()
				);		
	}
	
	private Set<Review> fromSetReviewJpaToSetReviewDom(Set<ReviewJpaEntity> reviewsJpa, Product product) {
				
		if ( reviewsJpa == null ) {
			return new HashSet<>();
		}

		return reviewsJpa.stream()
				.map(  r -> fromReviewJpaToReviewDom(r, product))
				.collect(Collectors.toSet());	
	}

	public List<Product> fromListJpaToListDomain(List<ProductJpaEntity> listJpa) {
				
		if ( listJpa == null ) {
			return new ArrayList<>();
		}

		return listJpa.stream()
				.map(this::fromJpaToDomain)
				.toList();	
	}
	
	public ProductDto fromDomainToDto(Product dom) {
		
		return new ProductDto(
				dom.getProductId(),
				dom.getProductName(),
				dom.getProductDescription(),
				dom.getCategory().getCategoryId()				
				);		
	}
	
	public List<ProductDto> fromListDomainToListDto(List<Product> list) {

		if ( list == null ) {
			return new ArrayList<>();
		}

		return list.stream()
				.map(this::fromDomainToDto)
				.toList();	
	}

	public ProductProto fromDtoToProto(ProductDto dto) {
		
		return ProductProto.newBuilder()
				.setProductId(dto.productId().toString())
				.setProductName(dto.productName())
				.setProductDescription(dto.productDescription())
				.setCategoryId(dto.categoryId().toString())
				.build();
	}

	public List<ProductProto> fromListDtoToListProto(List<ProductDto> list) {

		if ( list == null ) {
			return new ArrayList<>();
		}

		return list.stream()
				.map(this::fromDtoToProto)
				.toList();	
	}

	public ProductProto fromEventToProto(Event event) {
		
		Map<String, Object> eventData = event.getEventData();
		
		String prodId = extractString(eventData, GlobalConstants.PROD_ID);
        String name = extractString(eventData, GlobalConstants.PROD_NAME);
        String description = extractString(eventData, GlobalConstants.PROD_DESC);
        String catId = extractString(eventData, GlobalConstants.CAT_ID);

		return ProductProto.newBuilder()
				.setProductId(prodId)
				.setProductName(name)
				.setProductDescription(description)
				.setCategoryId(catId)
				.build();		
	}
	
	private String extractString(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse(GlobalConstants.MSG_NODATA);
	}
	
}
