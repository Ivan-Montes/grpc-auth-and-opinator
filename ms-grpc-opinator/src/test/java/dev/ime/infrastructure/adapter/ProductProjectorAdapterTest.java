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
import dev.ime.infrastructure.entity.ProductJpaEntity;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import jakarta.persistence.Persistence;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
class ProductProjectorAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	private ProductProjectorAdapter productProjectorAdapter;

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

		productProjectorAdapter = new ProductProjectorAdapter(sessionFactory, new MapExtractorHelper());
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
		UUID categoryId = UUID.fromString(event.getEventData().get(GlobalConstants.CAT_ID).toString());
		String categoryName = generateRandomString();
		CategoryJpaEntity categoyJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
		
		StepVerifier
		.create(insertAction(categoyJpaEntity))
		.verifyComplete();
		
		StepVerifier
		.create(productProjectorAdapter.create(event))
		.verifyComplete();
	}

	private Event createEventForCreateTest() {
		
		UUID productId = UUID.randomUUID();
		String productName = generateRandomString();
		String productDescription = generateRandomString();
		UUID categoryId = UUID.randomUUID();
		
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.PROD_ID, productId.toString());
		eventData.put(GlobalConstants.PROD_NAME, productName);
		eventData.put(GlobalConstants.PROD_DESC, productDescription);
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		
		return createEvent(GlobalConstants.PROD_CREATED, eventData);
	}

	private Event createEvent(String eventType, Map<String, Object> eventData) {		

		UUID eventId = UUID.randomUUID();
		String eventCategory = GlobalConstants.PROD_CAT;
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

	private <T> Mono<Void> insertAction(T entity) {

		return sessionFactory
		.withTransaction((session, tx) -> session.persist(entity))
		.convert().with(UniReactorConverters.toMono());
		
	}

	@Test
	void update_shouldReturnMonoVoid() {

		UUID productId = UUID.randomUUID();
		String productName = generateRandomString();
		String productDescription = generateRandomString();
		String productNameUpdated = generateRandomString();
		UUID categoryId = UUID.randomUUID();
		String categoryName = generateRandomString();
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.PROD_ID, productId.toString());
		eventData.put(GlobalConstants.PROD_NAME, productNameUpdated);
		eventData.put(GlobalConstants.PROD_DESC, productDescription);
		eventData.put(GlobalConstants.CAT_ID, categoryId.toString());
		Event event = createEvent(GlobalConstants.PROD_UPDATED, eventData);
		CategoryJpaEntity categoyJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
		ProductJpaEntity productJpaEntity = ProductJpaEntity.builder().productId(productId).productName(productName).productDescription(productDescription).category(categoyJpaEntity).build();
				
		StepVerifier
		.create(insertAction(categoyJpaEntity))
		.verifyComplete();

		StepVerifier
		.create(insertAction(productJpaEntity))
		.verifyComplete();
		
		StepVerifier
		.create(productProjectorAdapter.update(event))
		.verifyComplete();
	}

	@Test
	void deleteById_shouldReturnMonoVoid() {

		UUID productId = UUID.randomUUID();
		String productName = generateRandomString();
		String productDescription = generateRandomString();
		UUID categoryId = UUID.randomUUID();
		String categoryName = generateRandomString();
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.PROD_ID, productId.toString());
		Event event = createEvent(GlobalConstants.PROD_DELETED, eventData);
		CategoryJpaEntity categoyJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
		ProductJpaEntity productJpaEntity = ProductJpaEntity.builder().productId(productId).productName(productName).productDescription(productDescription).category(categoyJpaEntity).build();
				
		StepVerifier
		.create(insertAction(categoyJpaEntity))
		.verifyComplete();

		StepVerifier
		.create(insertAction(productJpaEntity))
		.verifyComplete();
		
		StepVerifier
		.create(productProjectorAdapter.deleteById(event))
		.verifyComplete();
	}

}
