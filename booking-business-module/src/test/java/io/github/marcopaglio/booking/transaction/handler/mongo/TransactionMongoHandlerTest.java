package io.github.marcopaglio.booking.transaction.handler.mongo;

import static io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager.TXN_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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

@DisplayName("Tests for TransactionMongoHandler class")
@Testcontainers
class TransactionMongoHandlerTest {

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;
	private ClientSession session;

	private TransactionMongoHandler transactionMongoHandler;

	@BeforeAll
	public static void setupServer() {
		mongoClient = MongoClients.create(mongo.getConnectionString());
	}

	@BeforeEach
	void setUp() throws Exception {
		session = mongoClient.startSession();
		
		transactionMongoHandler = new TransactionMongoHandler(session, TXN_OPTIONS);
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
	@DisplayName("Tests for 'startTransaction'")
	class StartTransactionTest {

		@Test
		@DisplayName("No active transaction")
		void testStartTransactionWhenThereAreNoActiveTransactionShouldStart() {
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
			
			transactionMongoHandler.startTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isTrue();
		}

		@Test
		@DisplayName("Active transaction")
		void testStartTransactionWhenThereIsAlreadyAnActiveTransactionShouldMaintainItActiveAndNotThrow() {
			startATransaction();
			
			assertThatNoException().isThrownBy(() -> transactionMongoHandler.startTransaction());
			
			assertThat(hasHandlerAnActiveTransaction()).isTrue();
		}

		@Test
		@DisplayName("Null transaction options")
		void testStartTransactionWhenTransactionOptionsAreNullShouldStart() {
			transactionMongoHandler = new TransactionMongoHandler(session, null);
			
			transactionMongoHandler.startTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isTrue();
		}
	}

	@Nested
	@DisplayName("Tests for 'commitTransaction'")
	class CommitTransactionTest {

		@Test
		@DisplayName("Active transaction")
		void testCommitTransactionWhenThereIsAnActiveTransactionShouldClose() {
			startATransaction();
			
			transactionMongoHandler.commitTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testCommitTransactionWhenThereIsNoActiveTransactionShouldMaintainItCloseAndNotThrow() {
			assertThat(session.hasActiveTransaction()).isFalse();
			
			assertThatNoException().isThrownBy(() -> transactionMongoHandler.commitTransaction());
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}
	}

	@Nested
	@DisplayName("Tests for 'rollbackTransaction'")
	class RollbackTransactionTest {

		@Test
		@DisplayName("Active transaction")
		void testRollbackTransactionWhenThereIsAnActiveTransactionShouldClose() {
			startATransaction();
			
			transactionMongoHandler.rollbackTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testRollbackTransactionWhenThereIsNoActiveTransactionShouldMaintainItCloseAndNotThrow() {
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
			
			assertThatNoException().isThrownBy(
					() -> transactionMongoHandler.rollbackTransaction());
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}
	}

	@Nested
	@DisplayName("Tests for 'closeHandler'")
	class CloseHandlerTest {

		@Test
		@DisplayName("Open handler")
		void testCloseHandlerWhenHandlerIsOpenShouldClose() {
			assertThat(isHandlerOpen()).isTrue();
			
			transactionMongoHandler.closeHandler();
			
			assertThat(isHandlerOpen()).isFalse();
		}

		@Test
		@DisplayName("Already closed handler")
		void testCloseHandlerWhenHandlerIsAlreadyClosedShouldMaintainItCloseAndNotThrow() {
			closeTheHandler();
			
			assertThatNoException().isThrownBy(() -> transactionMongoHandler.closeHandler());
			
			assertThat(isHandlerOpen()).isFalse();
		}
	}

	private void startATransaction() {
		session.startTransaction();
	}

	private boolean hasHandlerAnActiveTransaction() {
		return session.hasActiveTransaction();
	}

	private void closeTheHandler() {
		session.close();
	}

	private boolean isHandlerOpen() {
		try {
			// currently there isn't a method to check if session is open
			// this is a workaround
			session.getServerSession();
			return true;
		} catch(IllegalStateException e) {
			return false;
		}
	}
}