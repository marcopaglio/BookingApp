package io.github.marcopaglio.booking.repository.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.marcopaglio.booking.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;

@DisplayName("Tests for ClientPostegresRepository class")
@Testcontainers
class ClientPostgresRepositoryTest {
	
	@Container
	private static PostgreSQLContainer<?> postgreSQLContainer =
		new PostgreSQLContainer<>("postgres:15.2")
			.withDatabaseName("ClientPostgresRepositoryTest")
			.withUsername("postgres-test")
			.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private ClientPostgresRepository clientRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", postgreSQLContainer.getDatabaseName());
		
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}

	@BeforeEach
	void setUp() throws Exception {
		// start a new EM for communicating with the DB
		em = emf.createEntityManager();
		
		clientRepository = new ClientPostgresRepository(em);
		
		// make sure we always start with a clean database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
	}

	@AfterEach
	void tearDown() throws Exception {
		em.clear();
		em.close();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		emf.close();
	}

	@Test
	@DisplayName("ciao")
	void test() {
		assertThat(em.createQuery("SELECT c FROM Client c", Client.class).getResultList()).isEmpty();
	}
}
