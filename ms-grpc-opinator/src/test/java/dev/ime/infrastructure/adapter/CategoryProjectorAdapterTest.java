package dev.ime.infrastructure.adapter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import dev.ime.application.utils.MapExtractorHelper;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import jakarta.persistence.Persistence;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
class CategoryProjectorAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	private CategoryProjectorAdapter categoryProjectorAdapter;
	
	@SuppressWarnings("resource")
	@Container
	@ServiceConnection
	private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:17.1-alpine")).withDatabaseName("testdb").withUsername("testuser")
			.withPassword("testpass").withInitScript("init.sql");

	@BeforeAll
	public static void initialSetUp() {

		sessionFactory = Persistence
				.createEntityManagerFactory("persistence-unit-opinator",
						Map.of("jakarta.persistence.jdbc.driver", "org.postgresql.Driver",
								"jakarta.persistence.jdbc.url", postgresContainer.getJdbcUrl(),
								"jakarta.persistence.jdbc.user", postgresContainer.getUsername(),
								"jakarta.persistence.jdbc.password", postgresContainer.getPassword()))
				.unwrap(Mutiny.SessionFactory.class);

	}

	@BeforeEach
	void setUp() {

		categoryProjectorAdapter = new CategoryProjectorAdapter(sessionFactory, new MapExtractorHelper());
	}

	@AfterAll
	public static void finalTearDown() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Test
	void connectionEstablished() {

		org.junit.jupiter.api.Assertions.assertAll(() -> Assertions.assertThat(postgresContainer.isCreated()).isTrue(),
				() -> Assertions.assertThat(postgresContainer.isRunning()).isTrue());
	}
	
	@Test
	void create_shouldReturnMonoVoid() {

		Event event = createEventForCreateTest();
		
		StepVerifier
		.create(categoryProjectorAdapter.create(event))
		.verifyComplete();
	}

	private Event createEventForCreateTest() {
		UUID categoryId = UUID.randomUUID();
		String categoryName = generateRandomString();
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		eventData.put(GlobalConstants.CAT_NAME, categoryName);
		return createEvent(GlobalConstants.CAT_CREATED, eventData);
	}

	private Event createEvent(String eventType, Map<String, Object> eventData) {		

		UUID eventId = UUID.randomUUID();
		String eventCategory = GlobalConstants.CAT_CAT;
		Instant eventTimestamp = Instant.now();
		
		return new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	private String generateRandomString() {
		
		return UUID.randomUUID().toString().substring(0, 7);
		
	}
	
	@Test
	void update_shouldReturnMonoVoid() {

		UUID categoryId = UUID.randomUUID();
		String categoryName = generateRandomString();
		String categoryNameUpdated = generateRandomString();
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		eventData.put(GlobalConstants.CAT_NAME, categoryNameUpdated);
		Event event = createEvent(GlobalConstants.CAT_UPDATED, eventData);
		CategoryJpaEntity categoyJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
		
		StepVerifier
		.create(insertAction(categoyJpaEntity))
		.verifyComplete();
		
		StepVerifier
		.create(categoryProjectorAdapter.update(event))
		.verifyComplete();
	}

	private Mono<Void> insertAction(CategoryJpaEntity entity) {

		return sessionFactory
		.withTransaction((session, tx) -> session.persist(entity))
		.convert().with(UniReactorConverters.toMono());
	}
	
	@Test
	void deleteById_shouldReturnMonoVoid() {

		UUID categoryId = UUID.randomUUID();
		String categoryName = generateRandomString();
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		Event event = createEvent(GlobalConstants.CAT_DELETED, eventData);
		CategoryJpaEntity categoyJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
		
		StepVerifier
		.create(insertAction(categoyJpaEntity))
		.verifyComplete();
		
		StepVerifier
		.create(categoryProjectorAdapter.deleteById(event))
		.verifyComplete();
	}

}
