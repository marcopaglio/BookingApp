package io.github.marcopaglio.booking.transaction.handler.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Tests for TransactionPostgresHandler class")
@Testcontainers
class TransactionPostgresHandlerTest {

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.3")
		.withDatabaseName("TransactionPostgresHandlerTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private TransactionPostgresHandler transactionPostgresHandler;

	@BeforeAll
	static void setupServer() throws Exception {
		System.setProperty("db.host", postgreSQLContainer.getHost());
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", postgreSQLContainer.getDatabaseName());
		
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}

	@BeforeEach
	void setUp() throws Exception {
		em = emf.createEntityManager();
		
		transactionPostgresHandler = new TransactionPostgresHandler(em);
	}

	@AfterEach
	void closeEntityManager() throws Exception {
		em.close();
	}

	@AfterAll
	static void closeClient() throws Exception {
		emf.close();
	}

	@Nested
	@DisplayName("Tests for 'startTransaction'")
	class StartTransactionTest {

		@Test
		@DisplayName("No active transaction")
		void testStartTransactionWhenThereAreNoActiveTransactionShouldStart() {
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
			
			transactionPostgresHandler.startTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isTrue();
		}

		@Test
		@DisplayName("Active transaction")
		void testStartTransactionWhenThereIsAlreadyAnActiveTransactionShouldMaintainItActiveAndNotThrow() {
			startATransaction();
			
			assertThatNoException().isThrownBy(() -> transactionPostgresHandler.startTransaction());
			
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
			
			transactionPostgresHandler.commitTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testCommitTransactionWhenThereIsNoActiveTransactionShouldMaintainItCloseAndNotThrow() {
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
			
			assertThatNoException().isThrownBy(
					() -> transactionPostgresHandler.commitTransaction());
			
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
			
			transactionPostgresHandler.rollbackTransaction();
			
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testRollbackTransactionWhenThereIsNoActiveTransactionShouldMaintainItCloseAndNotThrow() {
			assertThat(hasHandlerAnActiveTransaction()).isFalse();
			
			assertThatNoException().isThrownBy(
					() -> transactionPostgresHandler.rollbackTransaction());
			
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
			
			transactionPostgresHandler.closeHandler();
			
			assertThat(isHandlerOpen()).isFalse();
		}

		@Test
		@DisplayName("Already closed handler")
		void testCloseHandlerWhenHandlerIsAlreadyClosedShouldMaintainItCloseAndNotThrow() {
			closeTheHandler();
			
			assertThatNoException().isThrownBy(() -> transactionPostgresHandler.closeHandler());
			
			assertThat(isHandlerOpen()).isFalse();
		}
	}

	private void startATransaction() {
		em.getTransaction().begin();
	}

	private boolean hasHandlerAnActiveTransaction() {
		return em.getTransaction().isActive();
	}

	private void closeTheHandler() {
		em.close();
	}

	private boolean isHandlerOpen() {
		return em.isOpen();
	}
}