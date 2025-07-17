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
import jakarta.persistence.Persistence;
import reactor.test.StepVerifier;

@Testcontainers
class ReviewProjectorAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	private ReviewProjectorAdapter reviewProjectorAdapter;
	private final UUID reviewIdInTestDb = UUID.fromString("f6d705be-01db-44f4-bd31-51ead4e671c8");
	private final UUID reviewIdDispensableInTestDb = UUID.fromString("f6d705be-01db-44f4-bd31-51ead4e671c9");
	private final UUID productIdInTestDb = UUID.fromString("54f16ed2-4ccb-42fb-81ec-aa53afa80f4a");
	private final String emailInTestDb = "email01@email.tk";
	
	@SuppressWarnings("resource")
	@Container
	@ServiceConnection
	private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:17.1-alpine")).withInitScript("init-with-data.sql");

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
		
		reviewProjectorAdapter = new ReviewProjectorAdapter(sessionFactory, new MapExtractorHelper());		
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
		.create(reviewProjectorAdapter.create(event))
		.verifyComplete();
	}

	private Event createEventForCreateTest() {
		
		UUID reviewId = UUID.randomUUID();	
		String reviewText = generateRandomString();
		Integer rating = 5;
		
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.REV_ID, reviewId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, emailInTestDb);
		eventData.put(GlobalConstants.PROD_ID, productIdInTestDb.toString());
		eventData.put(GlobalConstants.REV_TXT, reviewText);
		eventData.put(GlobalConstants.REV_RAT, rating);
		
		return createEvent(GlobalConstants.REV_CREATED, eventData);
	}

	private Event createEvent(String eventType, Map<String, Object> eventData) {		

		UUID eventId = UUID.randomUUID();
		String eventCategory = GlobalConstants.REV_CAT;
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

		String reviewTextUpdated = generateRandomString();
		Integer ratingUpdated = 1;
		
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.REV_ID, reviewIdInTestDb.toString());
		eventData.put(GlobalConstants.REV_TXT, reviewTextUpdated);
		eventData.put(GlobalConstants.REV_RAT, ratingUpdated);
		
		Event event =  createEvent(GlobalConstants.REV_UPDATED, eventData);

		StepVerifier
		.create(reviewProjectorAdapter.update(event))
		.verifyComplete();		
	}

	@Test
	void deleteById_shouldReturnMonoVoid() {		

		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.REV_ID, reviewIdDispensableInTestDb.toString());

		Event event =  createEvent(GlobalConstants.REV_DELETED, eventData);

		StepVerifier
		.create(reviewProjectorAdapter.deleteById(event))
		.verifyComplete();		
	}

}
