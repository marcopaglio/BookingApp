package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;

public abstract class ServedBookingPresenterRaceConditionIT {
	private static final int NUM_OF_THREADS = 10;

	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";

	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);

	private static final String CLIENT_STRING = "Client named " + A_FIRSTNAME + " " + A_LASTNAME;
	final static private String RESERVATION_STRING = "Reservation on " + A_DATE;

	protected TransactionalBookingService transactionalBookingService;

	@Mock
	private BookingView view;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	private Client client;

	private AutoCloseable closeable;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		// make sure we always start with a clean database
		cleanDatabase();
		
		client = new Client(A_FIRSTNAME, A_LASTNAME);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
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
				eq(CLIENT_STRING + " already exists."),
				eq("Something went wrong while adding " + CLIENT_STRING + ".")));
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
		
		assertThat(readAllReservationsFromDatabase())
			.containsOnlyOnce(new Reservation(client.getId(), A_LOCALDATE));
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				eq(RESERVATION_STRING + " already exists."),
				eq("Something went wrong while adding " + RESERVATION_STRING + ".")));
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
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				eq(CLIENT_STRING + " no longer exists."),
				eq("Something went wrong while deleting " + CLIENT_STRING + ".")));
	}

	@Test
	@DisplayName("Concurrent requests of 'deleteReservation'")
	void testDeleteReservationWhenConcurrentRequestsOccurShouldDeleteOnceAndNotThrowShowingErrors() {
		addTestClientToDatabase(client);
		Reservation reservation = new Reservation(client.getId(), A_LOCALDATE);
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
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				eq(RESERVATION_STRING + " no longer exists."),
				eq("Something went wrong while deleting " + RESERVATION_STRING + ".")));
	}

	@Test
	@DisplayName("Concurrent requests of 'renameClient'")
	void testRenameClientWhenConcurrentRequestsOccurShouldRenameOnceAndNotThrowShowingErrors() {
		String another_lastName = "De Lucia";
		String another_firstName = "Maria";
		
		List<Client> clients = new ArrayList<>();
		IntStream.range(0, NUM_OF_THREADS).forEach(i -> {
			Client a_client = new Client(A_FIRSTNAME + i, A_LASTNAME + i);
			addTestClientToDatabase(a_client);
			clients.add(i, a_client);
		});
		
		when(clientValidator.validateFirstName(another_firstName)).thenReturn(another_firstName);
		when(clientValidator.validateLastName(another_lastName)).thenReturn(another_lastName);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.renameClient(clients.get(i), another_firstName, another_lastName)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllClientsFromDatabase())
			.containsOnlyOnce(new Client(another_firstName, another_lastName));
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				contains(" already exists."), contains("Something went wrong while renaming ")));
	}

	@Test
	@DisplayName("Concurrent requests of 'rescheduleReservation'")
	void testRescheduleReservationWhenConcurrentRequestsOccurShouldRescheduleOnceAndNotThrowShowingErrors() {
		String another_date = "2023-09-05";
		LocalDate another_localDate = LocalDate.parse(another_date);
		
		addTestClientToDatabase(client);
		List<Reservation> reservations = new ArrayList<>();
		IntStream.range(0, NUM_OF_THREADS).forEach(i -> {
			Reservation a_reservation = new Reservation(client.getId(), LocalDate.of(i, 4, 24));
			addTestReservationToDatabase(a_reservation);
			reservations.add(i, a_reservation);
		});
		
		when(reservationValidator.validateDate(another_date)).thenReturn(another_localDate);
		
		List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
				.mapToObj(i -> new Thread(() ->
						new ServedBookingPresenter(view, transactionalBookingService,
								clientValidator, reservationValidator)
							.rescheduleReservation(reservations.get(i), another_date)))
				.peek(t -> t.start())
				.collect(Collectors.toList());
		
		await().atMost(10, SECONDS)
			.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		
		assertThat(readAllReservationsFromDatabase())
			.containsOnlyOnce(new Reservation(client.getId(), another_localDate));
		verify(view, times(NUM_OF_THREADS-1)).showOperationError(AdditionalMatchers.or(
				contains(" already exists."), contains("Something went wrong while rescheduling ")));
	}


	private void cleanDatabase() {
		for (Reservation reservation : transactionalBookingService.findAllReservations())
			transactionalBookingService.removeReservation(reservation.getId());
		for (Client client : transactionalBookingService.findAllClients())
			transactionalBookingService.removeClient(client.getId());
	}

	private void addTestClientToDatabase(Client client) {
		transactionalBookingService.insertNewClient(client);
	}

	private void addTestReservationToDatabase(Reservation reservation) {
		transactionalBookingService.insertNewReservation(reservation);
	}

	private List<Client> readAllClientsFromDatabase() {
		return transactionalBookingService.findAllClients();
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		return transactionalBookingService.findAllReservations();
	}
}