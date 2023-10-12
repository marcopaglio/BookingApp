package io.github.marcopaglio.booking.presenter.served;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests for ServedBookingPresenter and PostgreSQL")
class ServedPostgresBookingPresenterIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final UUID A_CLIENT_UUID = UUID.fromString("78bce42b-1d28-4c37-b0a2-3287d6a829ca");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("864f7928-049b-4c2a-bd87-9e52ca16afc5");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-09-05");

	private static EntityManagerFactory emf;

	private TransactionPostgresManager transactionPostgresManager;
	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionalBookingService transactionalBookingService;
	private RestrictedClientValidator restrictedClientValidator;
	private RestrictedReservationValidator restrictedReservationValidator;

	@Mock
	private BookingView view;

	private ServedBookingPresenter presenter;

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	private AutoCloseable closeable;

	@BeforeAll
	static void setupEmf() throws Exception {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "IntegrationTest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionPostgresManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionPostgresManager);
		restrictedClientValidator = new RestrictedClientValidator();
		restrictedReservationValidator = new RestrictedReservationValidator();
		
		presenter = new ServedBookingPresenter(view, transactionalBookingService,
				restrictedClientValidator, restrictedReservationValidator);
		
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@AfterAll
	static void closeEmf() throws Exception {
		emf.close();
	}

	@Nested
	@DisplayName("Integration tests for 'allClients'")
	class AllClientsIT {

		@Test
		@DisplayName("No clients in repository")
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallViewWithEmptyList() {
			presenter.allClients();
			
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Several clients in repository")
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallViewWithClientsAsList() {
			addTestClientToDatabase(client);
			addTestClientToDatabase(another_client);
			
			presenter.allClients();
			
			verify(view).showAllClients(Arrays.asList(client, another_client));
		}
	}

	@Nested 
	@DisplayName("Integration tests for 'allReservations'")
	class AllReservationsIT {

		@Test
		@DisplayName("No reservations in repository")
		void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldCallViewWithEmptyList() {
			presenter.allReservations();
			
			verify(view).showAllReservations(Collections.emptyList());
		}

		@Test
		@DisplayName("Several reservations in repository")
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallViewWithReservationsAsList() {
			addTestReservationToDatabase(reservation);
			addTestReservationToDatabase(another_reservation);
			
			presenter.allReservations();
			
			verify(view).showAllReservations(Arrays.asList(reservation, another_reservation));
		}
	}

	private void addTestClientToDatabase(Client client) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(client);
		em.getTransaction().commit();
		em.close();
	}

	private void addTestReservationToDatabase(Reservation reservation) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(reservation);
		em.getTransaction().commit();
		em.close();
	}
}