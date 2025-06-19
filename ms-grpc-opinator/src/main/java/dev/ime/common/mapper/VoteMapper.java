package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.google.protobuf.BoolValue;

import dev.ime.application.dto.VoteDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import dev.proto.CreateVoteRequest;
import dev.proto.UpdateVoteRequest;
import dev.proto.VoteProto;

@Component
public class VoteMapper {

	public VoteDto fromCreateToDto(CreateVoteRequest request) {
	
		return new VoteDto(
				null,
				null,
				UUID.fromString(request.getReviewId()),
				request.getUseful()
				);		
	}	

	public VoteDto fromUpdateToDto(UpdateVoteRequest request) {
		
		return new VoteDto(
				UUID.fromString(request.getVoteId()),
				null,
				null,
				request.getUseful()
				);		
	}

	public Vote fromDtoToDomain(VoteDto dto) {
		
		Review review = new Review();
		review.setReviewId( dto.reviewId());
		return new Vote(
				dto.voteId(),
				dto.email(),
				review,
				dto.useful()
				);		
	}
	
	public VoteJpaEntity fromDomainToJpa(Vote dom, ReviewJpaEntity reviewJpaEntity) {
		
		VoteJpaEntity voteJpaEntity = new VoteJpaEntity(
				dom.getVoteId(),
				dom.getEmail(),
				reviewJpaEntity,
				dom.isUseful()
				);
		reviewJpaEntity.getVotes().add(voteJpaEntity);
		
		return voteJpaEntity;
	}
	
	public Vote fromJpaToDomain(VoteJpaEntity entity) {
		
		Review review = fromReviewJpaToReviewDom(entity);	
		
		Vote vote = new Vote(
				entity.getVoteId(),
				entity.getEmail(),
				review,
				entity.isUseful()
				);		
		review.addVote(vote);
		return vote;
	}

	private Review fromReviewJpaToReviewDom(VoteJpaEntity entity) {
		
		ReviewJpaEntity reviewJpaEntity = entity.getReview();
		Product product = new Product();
		product.setProductId(reviewJpaEntity.getProduct().getProductId());
		
		return new Review(
				reviewJpaEntity.getReviewId(),
				reviewJpaEntity.getEmail(),
				product,
				reviewJpaEntity.getReviewText(),
				reviewJpaEntity.getRating(),
				new HashSet<>()
				);
	}

	public List<Vote> fromListJpaToListDomain(List<VoteJpaEntity> listJpa) {

		if (listJpa == null) {
			return new ArrayList<>();
		}

		return listJpa.stream().map(this::fromJpaToDomain).toList();
	}
	
	public VoteDto fromDomainToDto(Vote dom) {
		
		return new VoteDto(
				dom.getVoteId(),
				dom.getEmail(),
				dom.getReview().getReviewId(),
				dom.isUseful()
				);
	}
	
	public List<VoteDto> fromListDomainToListDto(List<Vote> list) {

		if (list == null) {
			return new ArrayList<>();
		}

		return list.stream().map(this::fromDomainToDto).toList();
	}

	public VoteProto fromDtoToProto(VoteDto dto) {
		
		return VoteProto.newBuilder()
				.setVoteId(dto.voteId().toString())
				.setEmail(dto.email())
				.setReviewId(dto.reviewId().toString())
				.setUseful(BoolValue.of(dto.useful()))
				.build();
	}

	public List<VoteProto> fromListDtoToListProto(List<VoteDto> list) {

		if ( list == null ) {
			return new ArrayList<>();
		}

		return list.stream()
				.map(this::fromDtoToProto)
				.toList();	
	}

	public VoteProto fromEventToProto(Event event) {
		
		Map<String, Object> eventData = event.getEventData();
		
		String votId = extractString(eventData, GlobalConstants.VOT_ID);
        String email = extractString(eventData, GlobalConstants.USERAPP_EMAIL);
        String revId = extractString(eventData, GlobalConstants.REV_ID);
        Boolean votUs = extractBool(eventData, GlobalConstants.VOT_US);

		return VoteProto.newBuilder()
				.setVoteId(votId)
				.setEmail(email)
				.setReviewId(revId)
				.setUseful(BoolValue.of(votUs))
				.build();		
	}
	
	private String extractString(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse(GlobalConstants.MSG_NODATA);
	}

	private Boolean extractBool(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .map(String::toLowerCase)
	                   .map(Boolean::parseBoolean)
	                   .orElse(false);
	}	
	
}
