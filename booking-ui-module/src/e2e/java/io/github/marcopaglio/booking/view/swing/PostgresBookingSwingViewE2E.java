package io.github.marcopaglio.booking.view.swing;

import static io.github.marcopaglio.booking.model.BaseEntity.ID_POSTGRESQL;
import static io.github.marcopaglio.booking.model.Client.FIRSTNAME_DB;
import static io.github.marcopaglio.booking.model.Client.LASTNAME_DB;
import static io.github.marcopaglio.booking.model.Reservation.CLIENTID_DB;
import static io.github.marcopaglio.booking.model.Reservation.DATE_DB;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.awt.EventQueue;
import java.awt.Frame;
import java.util.Map;
import java.util.UUID;

import org.assertj.swing.junit.runner.GUITestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import io.github.marcopaglio.booking.presenter.BookingPresenter;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("End-to-end tests for BookingSwingApp using PostgreSQL")
@RunWith(GUITestRunner.class)
public class PostgresBookingSwingViewE2E extends BookingSwingViewE2E {
	private static String postgresHost = System.getProperty("postgres.host", "localhost");
	private static int postgresPort = Integer.parseInt(System.getProperty("postgres.port", "5432"));
	private static String postgresName = System.getProperty("postgres.name", "ITandE2ETest_db");
	private static String postgresUser = System.getProperty("postgres.user", "postgres-it-e2e");
	private static String postgresPswd = System.getProperty("postgres.pswd", "postgres-it-e2e");

	private static EntityManagerFactory emf;

	@BeforeClass
	public static void setupEmf() throws Exception {
		// ATTENTION: number of max connection for PostgreSQL database is settled in
		// docker-maven-plugin (pom.xml) and is strictly related to the number of current tests.
		// Increasing the number of e2e tests requires more connections.
		String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
				postgresHost, postgresPort, postgresName);
		emf = Persistence.createEntityManagerFactory("postgres-e2e", Map.of(
				"jakarta.persistence.jdbc.url", jdbcUrl,
				"jakarta.persistence.jdbc.user", postgresUser,
				"jakarta.persistence.jdbc.password", postgresPswd));
	}

	@Override
	protected void onSetUp() throws Exception {
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		// add entities to the database
		addTestClientToDatabase(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);
		addTestReservationToDatabase(A_CLIENT_UUID, A_DATE, A_RESERVATION_UUID);
		
		// start the Swing application
		application(PostgresBookingSwingApp.class)
			.withArgs(
					postgresHost,
					String.valueOf(postgresPort),
					postgresName,
					postgresUser,
					postgresPswd
			).start();
		
		super.onSetUp();
	}

	@AfterClass
	public static void closeEmf() throws Exception {
		emf.close();
	}

	@Override
	protected void addTestClientToDatabase(String name, String surname, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + CLIENT_TABLE_DB + " " +
							"(" + ID_POSTGRESQL + ", "+ FIRSTNAME_DB + ", " + LASTNAME_DB + ") " +
							"VALUES ('" + id + "', '" + name + "', '" + surname + "') ")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	@Override
	protected void removeTestClientFromDatabase(String name, String surname) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("DELETE FROM " + CLIENT_TABLE_DB + " " +
							"WHERE " + FIRSTNAME_DB + "='" + name +"' " +
							"AND " + LASTNAME_DB + "='" + surname +"' ")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	@Override
	protected void addTestReservationToDatabase(UUID clientId, String date, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + RESERVATION_TABLE_DB + " " +
							"(" + ID_POSTGRESQL + ", "+ CLIENTID_DB + ", " + DATE_DB + ") " +
							"VALUES ('" + id + "', '" + clientId + "', '" + date + "') ")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	@Override
	protected void removeTestReservationFromDatabase(String date) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("DELETE FROM " + RESERVATION_TABLE_DB + " " +
							"WHERE " + DATE_DB + "='" + date +"' ")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}
}

class PostgresBookingSwingApp {
	private static final int STARTUP_FAILURE_STATUS = 255;

	private static EntityManagerFactory entityManagerAppFactory;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				String postgresHost = "localhost";
				int postgresPort = 5432;
				String postgresName = "BookingApp_PostgreSQL";
				String postgresUser = "postgres-user";
				String postgresPswd = "postgres-pswd";
				if (args.length > 0)
					postgresHost = args[0];
				if (args.length > 1)
					postgresPort = Integer.parseInt(args[1]);
				if (args.length > 2)
					postgresName = args[2];
				if (args.length > 3)
					postgresUser = args[3];
				if (args.length > 4)
					postgresPswd = args[4];
				
				TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
				ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
				ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
				
				openDatabaseConnection(postgresHost, postgresPort, postgresName, postgresUser, postgresPswd);
				TransactionManager transactionManager = createTransactionManager(transactionHandlerFactory, clientRepositoryFactory,
						reservationRepositoryFactory);
				
				BookingService bookingService = new TransactionalBookingService(transactionManager);
				ClientValidator clientValidator = new RestrictedClientValidator();
				ReservationValidator reservationValidator = new RestrictedReservationValidator();
				
				BookingSwingView bookingSwingView = new BookingSwingView();
				BookingPresenter bookingPresenter = new ServedBookingPresenter(bookingSwingView,
						bookingService, clientValidator, reservationValidator);
				bookingSwingView.setBookingPresenter(bookingPresenter);
				bookingSwingView.setVisible(true);
				bookingPresenter.allClients();
				bookingPresenter.allReservations();
			} catch(Exception e) {
				closeWindows();
				System.exit(STARTUP_FAILURE_STATUS);
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				closeDatabaseConnection();
			}
		});
	}

	private static void openDatabaseConnection(String postgresHost, int postgresPort, String postgresName,
			String postgresUser, String postgresPswd) {
		entityManagerAppFactory = Persistence.createEntityManagerFactory("postgres-e2e", Map.of(
				"jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://%s:%d/%s", postgresHost, postgresPort, postgresName),
				"jakarta.persistence.jdbc.user", postgresUser,
				"jakarta.persistence.jdbc.password", postgresPswd));
	}

	private static TransactionPostgresManager createTransactionManager(
			TransactionHandlerFactory transactionHandlerFactory, ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		return new TransactionPostgresManager(entityManagerAppFactory,
				transactionHandlerFactory, clientRepositoryFactory, reservationRepositoryFactory);
	}

	private static void closeWindows() {
		for (Frame f: Frame.getFrames()) {
			if (f.isDisplayable()) {
				f.setVisible(false);
				f.dispose();
			}
		}
	}

	private static void closeDatabaseConnection() {
		if (entityManagerAppFactory != null && entityManagerAppFactory.isOpen())
			entityManagerAppFactory.close();
	}
}