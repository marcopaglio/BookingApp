package io.github.marcopaglio.booking.presenter.served;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests of race conditions for ServedBookingPresenter and PostgreSQL")
class ServedPostgresBookingPresenterRaceConditionIT extends ServedBookingPresenterRaceConditionIT {
	private static EntityManagerFactory emf;

	@BeforeAll
	static void setupEmf() throws Exception {
		System.setProperty("db.host", System.getProperty("postgres.host", "localhost"));
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "ITandE2ETest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@BeforeEach
	void setUp() throws Exception {
		TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
		ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
		ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
		TransactionPostgresManager transactionPostgresManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionPostgresManager);
		
		super.setUp();
	}

	@AfterAll
	static void closeEmf() throws Exception {
		emf.close();
	}
}