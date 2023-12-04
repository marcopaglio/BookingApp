package io.github.marcopaglio.booking.repository.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.postgres.ClientPostgresRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Tests for ClientRepositoryFactory class")
@Testcontainers
class ClientRepositoryFactoryTest {

	private static final String BOOKING_DB_NAME = "ClientRepositoryFactoryTest_db";

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");
	private static MongoClient mongoClient;
	private ClientSession session;

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.3")
		.withDatabaseName(BOOKING_DB_NAME)
		.withUsername("postgres-test")
		.withPassword("postgres-test");
	private static EntityManagerFactory emf;
	private EntityManager em;

	private ClientRepositoryFactory clientRepositoryFactory;

	@BeforeAll
	public static void setupServer() {
		mongoClient = MongoClients.create(mongo.getConnectionString());
		
		System.setProperty("db.host", postgreSQLContainer.getHost());
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", postgreSQLContainer.getDatabaseName());
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}

	@BeforeEach
	void setUp() throws Exception {
		clientRepositoryFactory = new ClientRepositoryFactory();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
		
		emf.close();
	}

	@Nested
	@DisplayName("Tests for 'createClientRepository'")
	class CreateClientRepositoryTest {

		@Nested
		@DisplayName("ClientRepository for MongoDB")
		class ClientMongoRepositoryTest {

			@BeforeEach
			void startSession() throws Exception {
				session = mongoClient.startSession();
			}

			@AfterEach
			void closeSession() throws Exception {
				session.close();
			}

			@Test
			@DisplayName("Valid parameters")
			void testCreateClientRepositoryWhenParametersAreValidShouldReturnClientMongoRepository() {
				assertThat(clientRepositoryFactory.createClientRepository(mongoClient, session, BOOKING_DB_NAME))
					.isInstanceOf(ClientMongoRepository.class);
			}

			@Test
			@DisplayName("Null mongoClient")
			void testCreateClientRepositoryWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> clientRepositoryFactory.createClientRepository(null, session, BOOKING_DB_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientMongoRepository from a null Mongo client.");
			}

			@Test
			@DisplayName("Null session")
			void testCreateClientRepositoryWhenSessionIsNullShouldThrow() {
				assertThatThrownBy(
						() -> clientRepositoryFactory.createClientRepository(mongoClient, null, BOOKING_DB_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientMongoRepository from a null Mongo client session.");
			}
		}

		@Nested
		@DisplayName("ClientRepository for PostgreSQL")
		class ClientPostgresRepositoryTest {

			@BeforeEach
			void startEntityManager() throws Exception {
				em = emf.createEntityManager();
			}

			@AfterEach
			void closeEntityManager() throws Exception {
				em.close();
			}

			@Test
			@DisplayName("Valid parameters")
			void testCreateClientRepositoryWhenParametersAreValidShouldReturnClientPostgresRepository() {
				assertThat(clientRepositoryFactory.createClientRepository(em))
					.isInstanceOf(ClientPostgresRepository.class);
			}

			@Test
			@DisplayName("Null entityManager")
			void testCreateClientRepositoryWhenEntityManagerIsNullShouldThrow() {
				assertThatThrownBy(() -> clientRepositoryFactory.createClientRepository(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientPostgresRepository from a null Entity Manager.");
			}
		}
	}
}
