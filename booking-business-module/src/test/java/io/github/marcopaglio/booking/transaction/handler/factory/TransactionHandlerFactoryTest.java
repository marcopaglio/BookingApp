package io.github.marcopaglio.booking.transaction.handler.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterAll;
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

@DisplayName("Tests for TransactionHandlerFactory class")
@Testcontainers
class TransactionHandlerFactoryTest {

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;

	private TransactionHandlerFactory transactionHandlerFactory;

	@BeforeAll
	public static void setupServer() {
		mongoClient = MongoClients.create(mongo.getConnectionString());
	}

	@BeforeEach
	void setUp() {
		transactionHandlerFactory = new TransactionHandlerFactory();
	}

	@AfterAll
	static void closeClient() {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Tests for 'createTransactionHandler'")
	class CreateTransactionHandlerTest {

		@Nested
		@DisplayName("Handler for MongoDB")
		class MongoDBHandlerTest {
	
			@Test
			@DisplayName("Null mongoClient")
			void testCreateTransactionHandlerWhenMongoClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionHandlerFactory.createTransactionHandler(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Cannot create a ClientSession from a null Mongo client.");
			}

			@Test
			@DisplayName("Valid mongoClient")
			void testCreateTransactionHandlerWhenMongoClientIsValidShouldReturnSession() {
				assertThat(transactionHandlerFactory.createTransactionHandler(mongoClient))
					.isInstanceOf(ClientSession.class);
			}
		}
	}
}
