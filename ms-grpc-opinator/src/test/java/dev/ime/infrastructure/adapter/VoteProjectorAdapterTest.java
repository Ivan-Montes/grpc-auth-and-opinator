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
class VoteProjectorAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	private VoteProjectorAdapter voteProjectorAdapter;
	private final UUID voteIdInTestDb = UUID.fromString("4434c285-4ef5-4a25-a5fa-08866f7c89b5");
	private final UUID voteIdDispensableInTestDb = UUID.fromString("4434c285-4ef5-4a25-a5fa-08866f7c89b7");
	private final UUID reviewIdInTestDb = UUID.fromString("f6d705be-01db-44f4-bd31-51ead4e671c8");
	
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
		
		voteProjectorAdapter = new VoteProjectorAdapter(sessionFactory, new MapExtractorHelper());
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
		.create(voteProjectorAdapter.create(event))
		.verifyComplete();
	}

	private Event createEventForCreateTest() {
		
		UUID voteId = UUID.randomUUID();	
		String email = "email@yahoo.com";
		boolean useful = true;
		
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.VOT_ID, voteId.toString());
		eventData.put(GlobalConstants.USERAPP_EMAIL, email);
		eventData.put(GlobalConstants.REV_ID, reviewIdInTestDb.toString());
		eventData.put(GlobalConstants.VOT_US, useful);
		
		return createEvent(GlobalConstants.VOT_CREATED, eventData);
	}

	private Event createEvent(String eventType, Map<String, Object> eventData) {		

		UUID eventId = UUID.randomUUID();
		String eventCategory = GlobalConstants.VOT_CAT;
		Instant eventTimestamp = Instant.now();
		
		return new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);
	}

	@Test
	void update_shouldReturnMonoVoid() {

		boolean useful = false;
		
		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.VOT_ID, voteIdInTestDb.toString());
		eventData.put(GlobalConstants.VOT_US, useful);
		
		Event event =  createEvent(GlobalConstants.VOT_UPDATED, eventData);

		StepVerifier
		.create(voteProjectorAdapter.update(event))
		.verifyComplete();		
	}

	@Test
	void deleteById_shouldReturnMonoVoid() {		

		Map<String, Object> eventData = new HashMap<>();
		eventData.put(GlobalConstants.VOT_ID, voteIdDispensableInTestDb.toString());

		Event event =  createEvent(GlobalConstants.VOT_DELETED, eventData);

		StepVerifier
		.create(voteProjectorAdapter.deleteById(event))
		.verifyComplete();		
	}

}
