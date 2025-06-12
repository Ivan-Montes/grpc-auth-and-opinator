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
@Table(name = GlobalConstants.PROD_CAT_DB)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ProductJpaEntity {
	
	@Id
	@Column(name = GlobalConstants.PROD_ID_DB)
	private UUID productId;

	@Column(name = "product_name", nullable = false, unique = true, length = 100)
	private String productName;

	@Column(name = "product_description", length = 500)
	private String productDescription;

	@ManyToOne
    @JoinColumn(name = "category_id", nullable = false) 
    private CategoryJpaEntity category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@Builder.Default
    private Set<ReviewJpaEntity> reviews = new HashSet<>();    
}
