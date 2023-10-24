package io.github.marcopaglio.booking.presenter.served;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests of race conditions for ServedBookingPresenter and PostgreSQL")
class ServedPostgresBookingPresenterRaceConditionIT {
	private static final int NUM_OF_THREADS = 10;

	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_CLIENT_UUID = UUID.fromString("03ee257d-f06d-47e9-8ef0-78b18ee03fe9");
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);

	private static EntityManagerFactory emf;

	private TransactionPostgresManager transactionPostgresManager;
	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionalBookingService transactionalBookingService;

	@Mock
	private BookingView view;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	private Client client;
	private Reservation reservation;

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
		
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@AfterAll
	static void closeEmf() throws Exception {
		emf.close();
	}

	@Test
	@DisplayName("Concurrent requests of 'addClient'")
	void testAddClientWhenConcurrentRequestsOccurShouldAddOnceAndNotThrowShowingErrors() {
		when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
		when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.addClient(A_FIRSTNAME, A_LASTNAME)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllClientsFromDatabase()).containsOnlyOnce(client);
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				eq("A client named " + A_FIRSTNAME + " " + A_LASTNAME + " has already been made."),
				eq("Something went wrong while adding " + new Client(A_FIRSTNAME, A_LASTNAME).toString() + ".")));
	}

	@Test
	@DisplayName("Concurrent requests of 'addReservation'")
	void testAddReservationWhenConcurrentRequestsOccurShouldAddOnceAndNotThrowShowingErrors() {
		addTestClientToDatabase(client);
		UUID client_id = client.getId();
		
		when(reservationValidator.validateClientId(client_id)).thenReturn(client_id);
		when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.addReservation(client, A_DATE)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllReservationsFromDatabase()).containsOnlyOnce(reservation);
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				eq("A reservation on " + A_DATE + " has already been made."),
				eq("Something went wrong while adding " + new Reservation(client_id, A_LOCALDATE).toString() + ".")));
	}

	@Test
	@DisplayName("Concurrent requests of 'deleteClient'")
	void testDeleteClientWhenConcurrentRequestsOccurShouldDeleteOnceAndNotThrowShowingErrors() {
		addTestClientToDatabase(client);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.deleteClient(client)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(contains(client.toString()));
	}

	@Test
	@DisplayName("Concurrent requests of 'deleteReservation'")
	void testDeleteReservationWhenConcurrentRequestsOccurShouldDeleteOnceAndNotThrowShowingErrors() {
		addTestReservationToDatabase(reservation);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.deleteReservation(reservation)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(contains(reservation.toString()));
	}

	@Test
	@DisplayName("Concurrent requests of 'renameClient'")
	void testRenameClientWhenConcurrentRequestsOccurShouldRenameOnceAndNotThrowShowingErrors() {
		List<Client> clients = new ArrayList<>();
		IntStream.range(0, NUM_OF_THREADS).forEach(i -> {
			Client a_client = new Client(A_FIRSTNAME + i, A_LASTNAME + i);
			addTestClientToDatabase(a_client);
			clients.add(i, a_client);
		});
		
		when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME)).thenReturn(ANOTHER_FIRSTNAME);
		when(clientValidator.validateLastName(ANOTHER_LASTNAME)).thenReturn(ANOTHER_LASTNAME);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.renameClient(clients.get(i), ANOTHER_FIRSTNAME, ANOTHER_LASTNAME)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllClientsFromDatabase())
			.containsOnlyOnce(new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(anyString());
	}

	@Test
	@DisplayName("Concurrent requests of 'rescheduleReservation'")
	void testRescheduleReservationWhenConcurrentRequestsOccurShouldRescheduleOnceAndNotThrowShowingErrors() {
		addTestClientToDatabase(client);
		List<Reservation> reservations = new ArrayList<>();
		IntStream.range(0, NUM_OF_THREADS).forEach(i -> {
			Reservation a_reservation = new Reservation(A_CLIENT_UUID, LocalDate.of(i, 4, 24));
			addTestReservationToDatabase(a_reservation);
			reservations.add(i, a_reservation);
		});
		
		when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.rescheduleReservation(reservations.get(i), ANOTHER_DATE)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllReservationsFromDatabase())
			.doesNotContain(reservation)
			.containsOnlyOnce(new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE));
		
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(anyString());
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