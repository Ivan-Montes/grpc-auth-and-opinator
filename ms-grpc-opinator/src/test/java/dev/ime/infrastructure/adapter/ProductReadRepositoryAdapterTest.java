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
import dev.ime.common.mapper.ProductMapper;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import jakarta.persistence.Persistence;
import reactor.test.StepVerifier;

@Testcontainers
class ProductReadRepositoryAdapterTest {

    private static Mutiny.SessionFactory sessionFactory;

    private ProductJpaEntity product01;
    private ProductJpaEntity product02;
	private CategoryJpaEntity category;
	private final UUID productId01 = UUID.randomUUID();
	private final String productName01 = "Tomatoes";
	private final String productDescription01 = "full of red";
	private final UUID productId02 = UUID.randomUUID();
	private final String productName02 = "French Bean";
	private final String productDescription02 = "full of green";
    private final UUID categoryId = UUID.randomUUID();
	private final String categoryName = "Vegetables";

	private PageRequest pageRequest;
	private Sort sort;

	private ProductReadRepositoryAdapter productReadRepositoryAdapter;
	
	@SuppressWarnings("resource")
	@Container
	@ServiceConnection
    private static final PostgreSQLContainer<?> postgresContainer = 
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.1-alpine"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init.sql")
            ;

    @BeforeAll
    public static void initialSetUp() {
    	
    	sessionFactory = Persistence.createEntityManagerFactory("persistence-unit-opinator", Map.of(
            "jakarta.persistence.jdbc.driver", "org.postgresql.Driver",
            "jakarta.persistence.jdbc.url", postgresContainer.getJdbcUrl(),
            "jakarta.persistence.jdbc.user", postgresContainer.getUsername(),
            "jakarta.persistence.jdbc.password", postgresContainer.getPassword()
        )).unwrap(Mutiny.SessionFactory.class);
    }

	@BeforeEach
	void setUp() {

		category = new CategoryJpaEntity(categoryId, categoryName, new HashSet<>());
		product01 = new ProductJpaEntity(productId01, productName01, productDescription01, category, new HashSet<>());
		product02 = new ProductJpaEntity(productId02, productName02, productDescription02, category, new HashSet<>());
		sessionFactory.withTransaction((session, tx) -> session.persist(category)).await().indefinitely();
		sessionFactory.withTransaction((session, tx) -> session.persist(product01)).await().indefinitely();
		sessionFactory.withTransaction((session, tx) -> session.persist(product02)).await().indefinitely();

		productReadRepositoryAdapter = new ProductReadRepositoryAdapter(sessionFactory, new ProductMapper());

	}

	@AfterEach
	void tearDown() {

		sessionFactory
				.withTransaction(
						(session, tx) -> session.createMutationQuery("DELETE FROM ProductJpaEntity").executeUpdate())
				.await().indefinitely();
		
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
      
        org.junit.jupiter.api.Assertions.assertAll(
    			()->Assertions.assertThat(postgresContainer.isCreated()).isTrue(),
    			()->Assertions.assertThat(postgresContainer.isRunning()).isTrue()
    			);
    }

	@Test
	void findById_shouldReturnCoincidence() {

		StepVerifier.create(productReadRepositoryAdapter.findById(productId01)).expectNextCount(1).verifyComplete();
	}

	@Test
	void findAll_shouldReturnCountOfElements() {

		sort = Sort.by(Sort.Direction.fromString(GlobalConstants.PS_D), GlobalConstants.PROD_NAME);
		pageRequest = PageRequest.of(0, 5, sort);

		StepVerifier.create(productReadRepositoryAdapter.findAll(pageRequest)).expectNextCount(2).verifyComplete();
	}

	@Test
	void existsByName_withUnknownName_shouldReturnFalse() {

		StepVerifier.create(productReadRepositoryAdapter.existsByName("L")).expectNext(false).verifyComplete();
	}

	@Test
	void isAvailableByIdAndName_withUnknownName_shouldReturnFalse() {

		StepVerifier.create(productReadRepositoryAdapter.isAvailableByIdAndName(productId01, productName01))
				.expectNext(true).verifyComplete();
	}

}
