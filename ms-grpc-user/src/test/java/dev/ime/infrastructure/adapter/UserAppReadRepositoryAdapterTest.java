package dev.ime.infrastructure.adapter;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation.ReactiveSelect;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation.TerminatingSelect;
import org.springframework.data.relational.core.query.Query;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserAppReadRepositoryAdapterTest {

	@Mock
	private R2dbcEntityTemplate r2dbcEntityTemplate;
	@Mock
	private UserAppMapper userAppMapper;
	@InjectMocks
	private UserAppReadRepositoryAdapter userAppReadRepositoryAdapter;

	private PageRequest pageRequest;
	private Sort sort;
	
	private UserApp userApp01;
	private UserApp userApp02;
	private UserAppJpaEntity userAppJpaEntity01;
	private UserAppJpaEntity userAppJpaEntity02;

	private final UUID userAppId01 = UUID.randomUUID();
	private final String email01 = "email@email.tk";
	private final String name01 = "name";
	private final String lastname01 = "lastname";
	
	private final UUID userAppId02 = UUID.randomUUID();
	private final String email02 = "noemail@email.tk";
	private final String name02 = "noname";
	private final String lastname02 = "nolastname";
	
	@BeforeEach
	private void setUp() {			

		sort = Sort.by(Sort.Direction.fromString(GlobalConstants.PS_D), GlobalConstants.USERAPP_EMAIL);
		pageRequest = PageRequest.of(0, 3, sort);

		userApp01 = new UserApp(userAppId01, email01, name01, lastname01);		
		userApp02 = new UserApp(userAppId02, email02, name02, lastname02);
		userAppJpaEntity01 = new UserAppJpaEntity(userAppId01, email01, name01, lastname01);
		userAppJpaEntity02 = new UserAppJpaEntity();
		userAppJpaEntity02.setUserAppId(userAppId02);
		userAppJpaEntity02.setEmail(email02);
		userAppJpaEntity02.setName(name02);
		userAppJpaEntity02.setLastname(lastname02);		
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnFlux() {

		var reactiveSelect = Mockito.mock(ReactiveSelect.class);
		Mockito.when(r2dbcEntityTemplate.select(Mockito.any(Class.class))).thenReturn(reactiveSelect);
		var terminatingSelect = Mockito.mock(TerminatingSelect.class);
		Mockito.when(reactiveSelect.matching(Mockito.any(Query.class))).thenReturn(terminatingSelect);
		Mockito.when(terminatingSelect.all()).thenReturn(Flux.just(userAppJpaEntity01,userAppJpaEntity02));
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp01,userApp02);
	
		StepVerifier
		.create(userAppReadRepositoryAdapter.findAll(pageRequest))
		.expectNext(userApp01, userApp02)
		.verifyComplete();
		
		Mockito.verify(r2dbcEntityTemplate).select(Mockito.any(Class.class));
		Mockito.verify(reactiveSelect).matching(Mockito.any(Query.class));
		Mockito.verify(terminatingSelect).all();
		Mockito.verify(userAppMapper, Mockito.times(2)).fromJpaToDomain(Mockito.any(UserAppJpaEntity.class));		
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnMono() {
		
		Mockito.when(r2dbcEntityTemplate.selectOne(Mockito.any(Query.class), Mockito.any(Class.class))).thenReturn(Mono.just(userAppJpaEntity01));
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp01);
		
		StepVerifier
		.create(userAppReadRepositoryAdapter.findById(userAppId01))
		.assertNext( areaFound -> {
			org.junit.jupiter.api.Assertions.assertAll(
					()->Assertions.assertThat(areaFound).isEqualTo(userApp01),
					()->Assertions.assertThat(areaFound).isNotEqualTo(userApp02),
        			()->Assertions.assertThat(areaFound).hasSameHashCodeAs(userApp01)
        			);
		})
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).selectOne(Mockito.any(Query.class), Mockito.any(Class.class));
		Mockito.verify(userAppMapper).fromJpaToDomain(Mockito.any(UserAppJpaEntity.class));		
	}

	@Test
	void countByEmail_shouldReturnMono() {
		
		Mockito.when(r2dbcEntityTemplate.count(Mockito.any(Query.class), Mockito.any(Class.class))).thenReturn(Mono.just(0L));
		
		StepVerifier
		.create(userAppReadRepositoryAdapter.countByEmail(email02))
		.expectNext(0L)
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).count(Mockito.any(Query.class), Mockito.any(Class.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findByEmail_shouldReturnMono() {
		
		Mockito.when(r2dbcEntityTemplate.selectOne(Mockito.any(Query.class), Mockito.any(Class.class))).thenReturn(Mono.just(userAppJpaEntity01));
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp01);
		
		StepVerifier
		.create(userAppReadRepositoryAdapter.findByEmail(email01))
		.assertNext( areaFound -> {
			org.junit.jupiter.api.Assertions.assertAll(
					()->Assertions.assertThat(areaFound).isEqualTo(userApp01),
					()->Assertions.assertThat(areaFound).isNotEqualTo(userApp02),
        			()->Assertions.assertThat(areaFound).hasSameHashCodeAs(userApp01)
        			);
		})
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).selectOne(Mockito.any(Query.class), Mockito.any(Class.class));
		Mockito.verify(userAppMapper).fromJpaToDomain(Mockito.any(UserAppJpaEntity.class));		
	}

}
