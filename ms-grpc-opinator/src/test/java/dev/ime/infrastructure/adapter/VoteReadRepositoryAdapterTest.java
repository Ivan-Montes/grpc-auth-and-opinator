package dev.ime.infrastructure.adapter;

import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import jakarta.persistence.Persistence;
import reactor.test.StepVerifier;

@Testcontainers
class VoteReadRepositoryAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	UUID voteId = UUID.fromString("4434c285-4ef5-4a25-a5fa-08866f7c89b5");
	private PageRequest pageRequest;
	private Sort sort;

	private VoteReadRepositoryAdapter voteReadRepositoryAdapter;

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

		voteReadRepositoryAdapter = new VoteReadRepositoryAdapter(sessionFactory, new VoteMapper());
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
	void findById_shouldReturnCoincidence() {

		StepVerifier.create(voteReadRepositoryAdapter.findById(voteId)).expectNextCount(1).verifyComplete();
	}

	@Test
	void findAll_shouldReturnCountOfElements() {

		sort = Sort.by(Sort.Direction.fromString(GlobalConstants.PS_D), GlobalConstants.VOT_ID);
		pageRequest = PageRequest.of(0, 5, sort);

		StepVerifier.create(voteReadRepositoryAdapter.findAll(pageRequest)).expectNextCount(3).verifyComplete();
	}

}
