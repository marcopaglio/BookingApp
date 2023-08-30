package io.github.marcopaglio.booking.transaction.handler.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
		.withDatabaseName("TransactionPostgresHandlerTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private TransactionPostgresHandler transactionPostgresHandler;

	@BeforeAll
	static void setupServer() throws Exception {
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
			assertThat(em.getTransaction().isActive()).isFalse();
			
			transactionPostgresHandler.startTransaction();
			
			assertThat(em.getTransaction().isActive()).isTrue();
		}

		@Test
		@DisplayName("Active transaction")
		void testStartTransactionWhenThereIsAlreadyAnActiveTransactionShouldThrow() {
			startATransaction();
			
			assertThatThrownBy(() -> transactionPostgresHandler.startTransaction())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Transaction is already in progress.");
		}
	}

	@Nested
	@DisplayName("Tests for 'commitTransaction'")
	class CommitTransactionTest {

		@Test
		@DisplayName("Active transaction")
		void testCommitTransactionWhenThereIAnActiveTransactionShouldClose() {
			startATransaction();
			
			transactionPostgresHandler.commitTransaction();
			
			assertThat(em.getTransaction().isActive()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testCommitTransactionWhenThereIsNoActiveTransactionShouldThrow() {
			assertThat(em.getTransaction().isActive()).isFalse();
			
			assertThatThrownBy(() -> transactionPostgresHandler.commitTransaction())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("There is no transaction started.");
		}
	}

	@Nested
	@DisplayName("Tests for 'rollbackTransaction'")
	class RollbackTransactionTest {

		@Test
		@DisplayName("Active transaction")
		void testRollbackTransactionWhenThereIAnActiveTransactionShouldClose() {
			startATransaction();
			
			transactionPostgresHandler.rollbackTransaction();
			
			assertThat(em.getTransaction().isActive()).isFalse();
		}

		@Test
		@DisplayName("No active transaction")
		void testRollbackTransactionWhenThereIsNoActiveTransactionShouldThrow() {
			assertThat(em.getTransaction().isActive()).isFalse();
			
			assertThatThrownBy(() -> transactionPostgresHandler.rollbackTransaction())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("There is no transaction started.");
		}
	}

	@Nested
	@DisplayName("Tests for 'hasActiveTransaction'")
	class HasActiveTransactionTest {

		@Test
		@DisplayName("Active transaction")
		void testHasActiveTransactionWhenThereIAnActiveTransactionShouldReturnTrue() {
			startATransaction();
			
			assertThat(transactionPostgresHandler.hasActiveTransaction()).isTrue();
		}

		@Test
		@DisplayName("No active transaction")
		void testHasActiveTransactionWhenThereIsNoActiveTransactionShouldReturnFalse() {
			assertThat(transactionPostgresHandler.hasActiveTransaction()).isFalse();
		}
	}

	private void startATransaction() {
		em.getTransaction().begin();
	}
}
