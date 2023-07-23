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

import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;

@DisplayName("Tests for ClientRepositoryFactory class")
@Testcontainers
class ClientRepositoryFactoryTest {

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;

	private ClientSession session;

	private ClientRepositoryFactory clientRepositoryFactory;

	@BeforeAll
	public static void setupServer() {
		mongoClient = MongoClients.create(mongo.getConnectionString());
	}

	@BeforeEach
	void setUp() throws Exception {
		session = mongoClient.startSession();
		
		clientRepositoryFactory = new ClientRepositoryFactory();
	}

	@AfterEach
	void closeSession() throws Exception {
		session.close();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Tests for 'createClientRepository'")
	class CreateClientRepositoryTest {

		@Nested
		@DisplayName("ClientRepository for MongoDB")
		class ClientMongoRepositoryTest {

			@Test
			@DisplayName("Null mongoClient")
			void testCreateClientRepositoryWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(() -> clientRepositoryFactory.createClientRepository(null, session))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientMongoRepository from a null Mongo client.");
			}
	
			@Test
			@DisplayName("Null session")
			void testCreateClientRepositoryWhenSessionIsNullShouldThrow() {
				assertThatThrownBy(
						() -> clientRepositoryFactory.createClientRepository(mongoClient, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientMongoRepository from a null Mongo client session.");
			}

			@Test
			@DisplayName("Valid parameters")
			void testCreateClientRepositoryWhenParametersAreValidShouldReturnClientMongoRepository() {
				assertThat(clientRepositoryFactory.createClientRepository(mongoClient, session))
					.isInstanceOf(ClientMongoRepository.class);
			}
		}
	}
}
