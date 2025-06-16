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

import dev.ime.application.dto.ReviewDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import dev.proto.CreateReviewRequest;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;

@Component
public class ReviewMapper {

	public ReviewDto fromCreateToDto(CreateReviewRequest request) {

		return new ReviewDto(null, null, UUID.fromString(request.getProductId()), request.getReviewText(),
				request.getRating());

	}

	public ReviewDto fromUpdateToDto(UpdateReviewRequest request) {

		return new ReviewDto(UUID.fromString(request.getReviewId()), null, null, request.getReviewText(), request.getRating());

	}

	public Review fromDtoToDomain(ReviewDto dto) {

		Product product = new Product();
		product.setProductId(dto.productId());
		return new Review(
				dto.reviewId(), 
				dto.email(), 
				product, 
				dto.reviewText(), 
				dto.rating(), 
				new HashSet<>());
	}

	public ReviewJpaEntity fromDomainToJpa(Review dom, ProductJpaEntity productJpaEntity) {
		
		ReviewJpaEntity reviewJpaEntity = new ReviewJpaEntity(
				dom.getReviewId(),
				dom.getEmail(),
				productJpaEntity,
				dom.getReviewText(),
				dom.getRating(),
				new HashSet<>()
				);
		productJpaEntity.getReviews().add(reviewJpaEntity);
		
		return reviewJpaEntity;
	}

	public Review fromJpaToDomain(ReviewJpaEntity entity) {
		
		Product product = fromProductJpaToProductDomain(entity.getProduct());
		
		Review review = new Review(
				entity.getReviewId(),
				entity.getEmail(),
				product,
				entity.getReviewText(),
				entity.getRating(),
				null
				);	
		Set<Vote> votes = fromSetVoteJpaToSetVoteDom(entity.getVotes(), review);
		review.setVotes(votes);
		
		return review;
	}

	public Product fromProductJpaToProductDomain(ProductJpaEntity entity) {		
		
		Category category = new Category();
		category.setCategoryId(entity.getCategory().getCategoryId());

		Product product = new Product(
				entity.getProductId(),
				entity.getProductName(),
				entity.getProductDescription(),
				category,
				new HashSet<>()
				);
		category.addProduct(product);
		
		return product;
	}

	private Vote fromVoteJpaToVoteDom(VoteJpaEntity voteJpaEntity, Review review) {

		return new Vote(
				voteJpaEntity.getVoteId(),
				voteJpaEntity.getEmail(),
				review,
				voteJpaEntity.isUseful()
				);
	}
	
	private Set<Vote> fromSetVoteJpaToSetVoteDom(Set<VoteJpaEntity> votesJpa, Review review) {

		if ( votesJpa == null ) {
			return new HashSet<>();
		}

		return votesJpa.stream()
				.map(  v -> fromVoteJpaToVoteDom(v, review))
				.collect(Collectors.toSet());	
	}

	public List<Review> fromListJpaToListDomain(List<ReviewJpaEntity> listJpa) {

		if (listJpa == null) {
			return new ArrayList<>();
		}

		return listJpa.stream().map(this::fromJpaToDomain).toList();
	}
	
	public ReviewDto fromDomainToDto(Review dom) {
		
		return new ReviewDto(
				dom.getReviewId(),
				dom.getEmail(),
				dom.getProduct().getProductId(),
				dom.getReviewText(),
				dom.getRating()
				);
	}
	
	public List<ReviewDto> fromListDomainToListDto(List<Review> list) {

		if (list == null) {
			return new ArrayList<>();
		}

		return list.stream().map(this::fromDomainToDto).toList();
	}

	public ReviewProto fromDtoToProto(ReviewDto dto) {

		return ReviewProto.newBuilder().setReviewId(dto.reviewId().toString()).setEmail(dto.email()).setProductId(dto.productId().toString())
				.setReviewText(dto.reviewText()).setRating(dto.rating()).build();

	}
	
	public List<ReviewProto> fromListDtoToListProto(List<ReviewDto> listDto) {

		if (listDto == null) {
			return new ArrayList<>();
		}

		return listDto.stream().map(this::fromDtoToProto).toList();

	}


	public ReviewProto fromEventToProto(Event event) {
		
		Map<String, Object> eventData = event.getEventData();
		
		String revId = extractString(eventData, GlobalConstants.REV_ID);
        String email = extractString(eventData, GlobalConstants.USERAPP_EMAIL);
        String productId = extractString(eventData, GlobalConstants.PROD_ID);
        String revTxt = extractString(eventData, GlobalConstants.REV_TXT);
        Integer revRating = extractInt(eventData, GlobalConstants.REV_RAT);

		return ReviewProto.newBuilder()
				.setReviewId(revId)
				.setEmail(email)
				.setProductId(productId)
				.setReviewText(revTxt)
				.setRating(revRating)
				.build();		
	}
	
	private String extractString(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse(GlobalConstants.MSG_NODATA);
	}

	private Integer extractInt(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .filter( str -> str.matches("\\d"))
	                   .map(Integer::valueOf)
	                   .orElse(0);
	}	
	
}
