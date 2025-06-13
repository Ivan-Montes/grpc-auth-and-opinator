package dev.ime.infrastructure.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = GlobalConstants.REV_CAT_DB)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ReviewJpaEntity {
	
	@Id
	@Column(name = GlobalConstants.REV_ID_DB)
	private UUID reviewId;

	@Column(name = "email", nullable = false, length = 100)
	private String email; 

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private ProductJpaEntity product;

	@Column(name = "review_text", nullable = false, length = 1000)
	private String reviewText;

	@Column(name = "rating", nullable = false)
	private int rating;

	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@Builder.Default
	private Set<VoteJpaEntity> votes = new HashSet<>();
}
