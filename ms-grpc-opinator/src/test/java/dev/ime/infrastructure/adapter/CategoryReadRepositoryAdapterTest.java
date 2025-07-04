package dev.ime.infrastructure.adapter;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import jakarta.persistence.Persistence;
import reactor.test.StepVerifier;

@Testcontainers
class CategoryReadRepositoryAdapterTest {

	private static Mutiny.SessionFactory sessionFactory;

	private CategoryJpaEntity category01;
	private CategoryJpaEntity category02;
	private final UUID categoryId01 = UUID.randomUUID();
	private final String categoryName01 = "Vegetables";
	private final UUID categoryId02 = UUID.randomUUID();
	private final String categoryName02 = "NoVegetables";

	private PageRequest pageRequest;
	private Sort sort;

	private CategoryReadRepositoryAdapter categoryReadRepositoryAdapter;

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

		category01 = new CategoryJpaEntity(categoryId01, categoryName01, new HashSet<>());
		category02 = new CategoryJpaEntity(categoryId02, categoryName02, new HashSet<>());

		sessionFactory.withTransaction((session, tx) -> session.persist(category01)).await().indefinitely();
		sessionFactory.withTransaction((session, tx) -> session.persist(category02)).await().indefinitely();

		categoryReadRepositoryAdapter = new CategoryReadRepositoryAdapter(sessionFactory, new CategoryMapper());

	}

	@AfterEach
	void tearDown() {

		sessionFactory
				.withTransaction(
						(session, tx) -> session.createMutationQuery("DELETE FROM CategoryJpaEntity").executeUpdate())
				.await().indefinitely();

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

		StepVerifier.create(categoryReadRepositoryAdapter.findById(categoryId01)).expectNextCount(1).verifyComplete();
	}

	@Test
	void findAll_shouldReturnCountOfElements() {

		sort = Sort.by(Sort.Direction.fromString(GlobalConstants.PS_D), GlobalConstants.CAT_NAME);
		pageRequest = PageRequest.of(0, 5, sort);

		StepVerifier.create(categoryReadRepositoryAdapter.findAll(pageRequest)).expectNextCount(2).verifyComplete();
	}

	@Test
	void existsByName_withUnknownName_shouldReturnFalse() {

		StepVerifier.create(categoryReadRepositoryAdapter.existsByName("L")).expectNext(false).verifyComplete();
	}

	@Test
	void isAvailableByIdAndName_withUnknownName_shouldReturnFalse() {

		StepVerifier.create(categoryReadRepositoryAdapter.isAvailableByIdAndName(categoryId01, categoryName01))
				.expectNext(true).verifyComplete();
	}

}
