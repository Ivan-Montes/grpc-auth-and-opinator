package dev.ime.infrastructure.entity;


import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = GlobalConstants.VOT_CAT_DB)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class VoteJpaEntity {
	
	@Id
	@Column(name = GlobalConstants.VOT_ID_DB)
	private UUID voteId;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@ManyToOne
	@JoinColumn(name = "review_id", nullable = false)
	private ReviewJpaEntity review;

	@Column(name = "useful", nullable = false)
	private boolean useful;
}
