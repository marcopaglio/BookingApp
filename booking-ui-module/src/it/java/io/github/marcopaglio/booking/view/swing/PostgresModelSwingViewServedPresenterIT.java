package io.github.marcopaglio.booking.view.swing;

import org.assertj.swing.junit.runner.GUITestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests for BookingSwingView, ServedBookingPresenter and PostgreSQL")
@RunWith(GUITestRunner.class)
public class PostgresModelSwingViewServedPresenterIT extends ModelSwingViewServedPresenterIT {
	private static EntityManagerFactory emf;

	@BeforeClass
	public static void setupEmf() throws Exception {
		System.setProperty("db.host", System.getProperty("postgres.host", "localhost"));
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "ITandE2ETest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@Override
	protected void onSetUp() throws Exception {
		TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
		ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
		ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
		TransactionPostgresManager transactionPostgresManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionPostgresManager);
		
		super.onSetUp();
	}

	@AfterClass
	public static void closeEmf() throws Exception {
		emf.close();
	}
}