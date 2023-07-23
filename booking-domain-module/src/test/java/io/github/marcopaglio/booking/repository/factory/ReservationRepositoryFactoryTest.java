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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;

@DisplayName("Tests for ReservationRepositoryFactory class")
@Testcontainers
class ReservationRepositoryFactoryTest {

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;

	private ClientSession session;

	private ReservationRepositoryFactory reservationRepositoryFactory;

	@BeforeAll
	public static void setupServer() {
		mongoClient = MongoClients.create(mongo.getConnectionString());
	}

	@BeforeEach
	void setUp() {
		session = mongoClient.startSession();
		
		reservationRepositoryFactory = new ReservationRepositoryFactory();
	}

	@AfterEach
	void closeSession() {
		session.close();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Tests for 'createReservationRepository'")
	class CreateReservationRepositoryTest {

		@Nested
		@DisplayName("ReservationRepository for MongoDB")
		class ReservationMongoRepositoryTest {

			@Test
			@DisplayName("Null mongoClient")
			void testCreateReservationRepositoryWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> reservationRepositoryFactory.createReservationRepository(null, session))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ReservationMongoRepository from a null Mongo client.");
			}
	
			@Test
			@DisplayName("Null session")
			void testCreateReservationRepositoryWhenSessionIsNullShouldThrow() {
				assertThatThrownBy(
						() -> reservationRepositoryFactory.createReservationRepository(mongoClient, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage(
							"Cannot create a ReservationMongoRepository from a null Mongo client session.");
			}

			@Test
			@DisplayName("Valid parameters")
			void testCreateReservationRepositoryWhenParametersAreValidShouldReturnClientMongoRepository() {
				assertThat(reservationRepositoryFactory.createReservationRepository(mongoClient, session))
					.isInstanceOf(ReservationMongoRepository.class);
			}
		}
	}

}
