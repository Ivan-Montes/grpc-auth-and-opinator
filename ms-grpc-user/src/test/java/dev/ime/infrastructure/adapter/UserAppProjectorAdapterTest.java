package dev.ime.infrastructure.adapter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserAppProjectorAdapterTest {

	@Mock
	private R2dbcEntityTemplate r2dbcEntityTemplate;
	
	@InjectMocks
	private UserAppProjectorAdapter userAppProjectorAdapter;

	private Event event;

	private final UUID userAppId = UUID.randomUUID();
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	private UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.USERAPP_CAT;
	private final String eventType = GlobalConstants.USERAPP_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp() {	

		eventData = new HashMap<>();
		eventData.put(GlobalConstants.USERAPP_ID, userAppId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.USERAPP_NAME, name);
		eventData.put(GlobalConstants.USERAPP_LASTNAME, lastname);
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);

	}

	@SuppressWarnings("unchecked")
	@Test
	void create_shouldReturnMonoVoid() {
		
		Mockito.when(r2dbcEntityTemplate.selectOne(Mockito.any(Query.class), Mockito.any(Class.class))).thenReturn(Mono.empty());
		Mockito.when(r2dbcEntityTemplate.insert(Mockito.any(UserAppJpaEntity.class))).thenReturn(Mono.just(new UserAppJpaEntity()));

		StepVerifier
		.create(userAppProjectorAdapter.create(event))
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).selectOne(Mockito.any(Query.class), Mockito.any(Class.class));
		Mockito.verify(r2dbcEntityTemplate).insert(Mockito.any(UserAppJpaEntity.class));			
	}

	@Test
	void update_shouldReturnMonoVoid() {
		
		Mockito.when(r2dbcEntityTemplate.update(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.any(Class.class))).thenReturn(Mono.just(2L));
		
		StepVerifier
		.create(userAppProjectorAdapter.update(event))
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).update(Mockito.any(Query.class), Mockito.any(Update.class), Mockito.any(Class.class));		
	}

	@Test
	void deleteById_shouldReturnMonoVoid() {
		
		Mockito.when(r2dbcEntityTemplate.delete(Mockito.any(Query.class), Mockito.any(Class.class))).thenReturn(Mono.just(1L));

		StepVerifier
		.create(userAppProjectorAdapter.deleteById(event))
		.verifyComplete();

		Mockito.verify(r2dbcEntityTemplate).delete(Mockito.any(Query.class), Mockito.any(Class.class));		
	}
	

}
