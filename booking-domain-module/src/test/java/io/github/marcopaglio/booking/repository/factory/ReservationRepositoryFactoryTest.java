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

import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.repository.postgres.ReservationPostgresRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Tests for ReservationRepositoryFactory class")
@Testcontainers
class ReservationRepositoryFactoryTest {
	private static final String BOOKING_DB_NAME = "ReservationRepositoryFactoryTest_db";

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

	private ReservationRepositoryFactory reservationRepositoryFactory;

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
		reservationRepositoryFactory = new ReservationRepositoryFactory();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
		
		emf.close();
	}

	@Nested
	@DisplayName("Tests for 'createReservationRepository'")
	class CreateReservationRepositoryTest {

		@Nested
		@DisplayName("ReservationRepository for MongoDB")
		class ReservationMongoRepositoryTest {

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
			void testCreateReservationRepositoryWhenParametersAreValidShouldReturnClientMongoRepository() {
				assertThat(reservationRepositoryFactory.createReservationRepository(mongoClient, session, BOOKING_DB_NAME))
					.isInstanceOf(ReservationMongoRepository.class);
			}

			@Test
			@DisplayName("Null mongoClient")
			void testCreateReservationRepositoryWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> reservationRepositoryFactory.createReservationRepository(null, session, BOOKING_DB_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ReservationMongoRepository from a null MongoDB client.");
			}

			@Test
			@DisplayName("Null session")
			void testCreateReservationRepositoryWhenSessionIsNullShouldThrow() {
				assertThatThrownBy(
						() -> reservationRepositoryFactory.createReservationRepository(mongoClient, null, BOOKING_DB_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage(
							"Cannot create a ReservationMongoRepository from a null MongoDB client session.");
			}
		}

		@Nested
		@DisplayName("ReservationRepository for PostgreSQL")
		class ReservationPostgreSQLRepositoryTest {

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
			void testCreateReservationRepositoryWhenParametersAreValidShouldReturnClientPostgreSQLRepository() {
				assertThat(reservationRepositoryFactory.createReservationRepository(em))
					.isInstanceOf(ReservationPostgresRepository.class);
			}

			@Test
			@DisplayName("Null entityManager")
			void testCreateReservationRepositoryWhenEntityManagerIsNullShouldThrow() {
				assertThatThrownBy(() -> reservationRepositoryFactory.createReservationRepository(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ReservationPostgresRepository from a null Entity Manager.");
			}
		}
	}
}