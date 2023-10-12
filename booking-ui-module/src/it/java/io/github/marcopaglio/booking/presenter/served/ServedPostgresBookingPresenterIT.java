package io.github.marcopaglio.booking.presenter.served;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	private static final UUID A_CLIENT_UUID = UUID.fromString("9fc24bdb-5c1d-477b-b197-e645caf51ec6");
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("8ad4de21-9a13-4307-97b2-13e0aa77b7a0");

	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("298f5682-0294-4d00-b337-b86fb32e5e74");
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);

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

	@Nested
	@DisplayName("Integration tests for 'deleteClient'")
	class DeleteClientIT {

		@Test
		@DisplayName("Client is in repository")
		void testDeleteClientWhenClientIsInRepositoryShouldRemoveAndNotifyView() {
			addTestClientToDatabase(client);
			addTestClientToDatabase(another_client);
			
			presenter.deleteClient(client);
			
			verify(view).clientRemoved(client);
			assertThat(readAllClientsFromDatabase())
				.doesNotContain(client)
				.containsExactly(another_client);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldShowErrorAndUpdateView() {
			client.setId(A_CLIENT_UUID);
			
			presenter.deleteClient(client);
			
			verify(view).showOperationError(client.toString() + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}
	}

	@Nested
	@DisplayName("Integration tests for 'deleteReservation'")
	class DeleteReservationIT {

		@Test
		@DisplayName("Reservation is in repository")
		void testDeleteReservationWhenReservationIsInRepositoryShouldRemoveAndNotifyView() {
			addTestReservationToDatabase(reservation);
			addTestReservationToDatabase(another_reservation);
			
			presenter.deleteReservation(reservation);
			
			verify(view).reservationRemoved(reservation);
			
			assertThat(readAllReservationsFromDatabase())
				.doesNotContain(reservation)
				.containsExactly(another_reservation);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowErrorAndUpdateView() {
			reservation.setId(A_RESERVATION_UUID);
			
			presenter.deleteReservation(reservation);
			
			verify(view).showOperationError(reservation.toString() + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addClient'")
	class AddClientIT {

		@Test
		@DisplayName("Client is new")
		void testAddClientWhenClientIsNewShouldInsertAndNotifyView() {
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			verify(view).clientAdded(client);
			
			assertThat(readAllClientsFromDatabase()).containsExactly(client);
		}

		@Test
		@DisplayName("Client is not new")
		void testAddClientWhenClientIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client);
			
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			verify(view).showOperationError("A client named " + A_FIRSTNAME
					+ " " + A_LASTNAME + " has already been made.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Arrays.asList(client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addReservation'")
	class AddReservationIT {

		@Test
		@DisplayName("Reservation is new")
		void testAddReservationWhenReservationIsNewShouldInsertAndNotifyView() {
			addTestClientToDatabase(client);
			
			presenter.addReservation(client, A_DATE);
			
			verify(view).reservationAdded(reservation);
			
			assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
		}

		@Test
		@DisplayName("Reservation is not new")
		void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			
			presenter.addReservation(client, A_DATE);
			
			verify(view).showOperationError(
					"A reservation on " + A_DATE + " has already been made.");
			verify(view).showAllReservations(Arrays.asList(reservation));
			verify(view).showAllClients(Arrays.asList(client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'renameClient'")
	class RenameClientIT {

		@Test
		@DisplayName("Renamed client is new")
		void testRenameClientWhenRenamedClientIsNewShouldRenameAndNotifyView() {
			addTestClientToDatabase(client);
			Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			renamedClient.setId(client.getId());
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			verify(view).clientRenamed(client, renamedClient);
			
			assertThat(readAllClientsFromDatabase())
				.contains(renamedClient)
				.doesNotContain(client);
		}

		@Test
		@DisplayName("Renamed client is not new")
		void testRenameClientWhenRenamedClientIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client);
			addTestClientToDatabase(another_client);
			Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			renamedClient.setId(client.getId());
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			verify(view).showOperationError("A client named " + ANOTHER_FIRSTNAME
					+ " " + ANOTHER_LASTNAME + " has already been made.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Arrays.asList(client, another_client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'rescheduleReservation'")
	class RescheduleReservationIT {

		@Test
		@DisplayName("Rescheduled reservation is new")
		void testRescheduleReservationWhenRescheduledReservationIsNewShouldRescheduleAndNotifyView() {
			addTestReservationToDatabase(reservation);
			Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
			rescheduledReservation.setId(reservation.getId());
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			verify(view).reservationRescheduled(reservation, rescheduledReservation);
			
			assertThat(readAllReservationsFromDatabase())
				.contains(rescheduledReservation)
				.doesNotContain(reservation);
		}

		@Test
		@DisplayName("Rescheduled reservation is not new")
		void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowErrorAndUpdateView() {
			addTestReservationToDatabase(reservation);
			addTestReservationToDatabase(another_reservation);
			Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
			rescheduledReservation.setId(reservation.getId());
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			verify(view).showOperationError(
					"A reservation on " + ANOTHER_DATE + " has already been made.");
			// updateAll
			verify(view).showAllReservations(Arrays.asList(reservation, another_reservation));
			verify(view).showAllClients(Collections.emptyList());
		}
	}

	private List<Client> readAllClientsFromDatabase() {
		EntityManager em = emf.createEntityManager();
		List<Client> clientsInDB = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
		em.close();
		return clientsInDB;
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		EntityManager em = emf.createEntityManager();
		List<Reservation> reservationsInDB = em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
		em.close();
		return reservationsInDB;
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