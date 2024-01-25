package io.github.marcopaglio.booking.transaction.handler.factory;

import static io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager.TXN_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.github.marcopaglio.booking.transaction.handler.mongo.TransactionMongoHandler;
import io.github.marcopaglio.booking.transaction.handler.postgres.TransactionPostgresHandler;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Tests for TransactionHandlerFactory class")
@Testcontainers
class TransactionHandlerFactoryTest {

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");
	private static final String TXNOPTIONS_FIELD = "txnOptions";
	private static MongoClient mongoClient;

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.3")
		.withDatabaseName("TransactionHandlerFactoryTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");
	private static EntityManagerFactory emf;

	private TransactionHandlerFactory transactionHandlerFactory;

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
		transactionHandlerFactory = new TransactionHandlerFactory();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
		
		emf.close();
	}

	@Nested
	@DisplayName("Tests for 'createTransactionHandler'")
	class CreateTransactionHandlerTest {

		@Nested
		@DisplayName("Handler for MongoDB")
		class MongoDBHandlerTest {

			@Test
			@DisplayName("Valid parameters")
			void testCreateTransactionHandlerWhenParametersAreValidShouldReturnTransactionMongoHandler() {
				assertThat(transactionHandlerFactory.createTransactionHandler(mongoClient, TXN_OPTIONS))
					.isInstanceOf(TransactionMongoHandler.class)
					.extracting(TXNOPTIONS_FIELD).isNotNull();
			}

			@Test
			@DisplayName("Null mongoClient")
			void testCreateTransactionHandlerWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionHandlerFactory.createTransactionHandler(null, TXN_OPTIONS))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a TransactionMongoHandler from a null MongoDB client.");
			}

			@Test
			@DisplayName("Null txnOptions")
			void testCreateTransactionHandlerWhenTransactionOptionsAreNullShouldReturnTransactionMongoHandler() {
				assertThat(transactionHandlerFactory.createTransactionHandler(mongoClient, null))
					.isInstanceOf(TransactionMongoHandler.class)
					.extracting(TXNOPTIONS_FIELD).isNull();
			}
		}

		@Nested
		@DisplayName("Handler for PostgreSQL")
		class PostgreSQLHandlerTest {

			@Test
			@DisplayName("Valid entityManagerFactory")
			void testCreateTransactionHandlerWhenEntityManagerFactoryIsValidShouldReturnTransactionPostgresHandler() {
				assertThat(transactionHandlerFactory.createTransactionHandler(emf))
					.isInstanceOf(TransactionPostgresHandler.class);
			}

			@Test
			@DisplayName("Null entityManagerFactory")
			void testCreateTransactionHandlerWhenEntityManagerFactoryIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionHandlerFactory.createTransactionHandler(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a TransactionPostgresHandler from a null EntityManagerFactory.");
			}
		}
	}
}