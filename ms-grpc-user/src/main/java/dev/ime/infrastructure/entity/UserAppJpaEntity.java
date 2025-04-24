package dev.ime.infrastructure.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import dev.ime.common.constants.GlobalConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Table(name = GlobalConstants.USERAPP_CAT_DB)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UserAppJpaEntity {

	@Id
	@Column(value = GlobalConstants.USERAPP_ID_DB )
	private UUID userAppId;
	
	@Column
	private String email;
	
	@Column
	private String name;
	
	@Column
	private String lastname;
	
}
