package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;

public abstract class ServedBookingPresenterIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final UUID A_CLIENT_UUID = UUID.fromString("03ee257d-f06d-47e9-8ef0-78b18ee03fe9");
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("a2014dc9-7f77-4aa2-a3ce-0559736a7670");
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);

	@Mock
	private BookingView view;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	protected TransactionalBookingService transactionalBookingService;
	private ServedBookingPresenter presenter;

	private Client client;
	private Reservation reservation;

	private AutoCloseable closeable;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		presenter = new ServedBookingPresenter(view, transactionalBookingService,
				clientValidator, reservationValidator);
		
		// make sure we always start with a clean database
		cleanDatabase();
		
		// entities
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
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
			Client another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
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
			addTestClientToDatabase(client);
			Reservation reservation = new Reservation(client.getId(), A_LOCALDATE);
			addTestReservationToDatabase(reservation);
			Reservation another_reservation = new Reservation(client.getId(), ANOTHER_LOCALDATE);
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
		void testDeleteClientWhenClientIsInRepositoryShouldRemove() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			
			presenter.deleteClient(client);
			
			assertThat(readAllClientsFromDatabase()).doesNotContain(client);
			assertThat(readAllReservationsFromDatabase())
				.filteredOn(r -> Objects.equals(r.getClientId(), client.getId()))
				.isEmpty();
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldNotChangeAnything() {
			List<Client> initialClientsInDB = readAllClientsFromDatabase();
			client.setId(A_CLIENT_UUID);
			
			presenter.deleteClient(client);
			
			assertThat(readAllClientsFromDatabase()).isEqualTo(initialClientsInDB);
		}
	}

	@Nested
	@DisplayName("Integration tests for 'deleteReservation'")
	class DeleteReservationIT {

		@Test
		@DisplayName("Reservation is in repository")
		void testDeleteReservationWhenReservationIsInRepositoryShouldRemove() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			
			presenter.deleteReservation(reservation);
			
			assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldNotChangeAnything() {
			List<Reservation> initialReservationsInDB = readAllReservationsFromDatabase();
			reservation.setId(A_RESERVATION_UUID);
			
			presenter.deleteReservation(reservation);
			
			assertThat(readAllReservationsFromDatabase()).isEqualTo(initialReservationsInDB);
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addClient'")
	class AddClientIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
			when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
		}

		@Test
		@DisplayName("Client is new")
		void testAddClientWhenClientIsNewShouldInsert() {
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			assertThat(readAllClientsFromDatabase()).containsExactly(client);
		}

		@Test
		@DisplayName("Client is not new")
		void testAddClientWhenClientIsNotNewShouldNotInsert() {
			addTestClientToDatabase(client);
			
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			assertThat(readAllClientsFromDatabase()).containsOnlyOnce(client);
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addReservation'")
	class AddReservationIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(reservationValidator.validateClientId(isA(UUID.class))).thenAnswer(i -> i.getArguments()[0]);
			when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
		}

		@Test
		@DisplayName("Reservation is new")
		void testAddReservationWhenReservationIsNewShouldInsert() {
			addTestClientToDatabase(client);
			
			presenter.addReservation(client, A_DATE);
			
			assertThat(readAllReservationsFromDatabase())
				.containsExactly(new Reservation(client.getId(), A_LOCALDATE));
		}

		@Test
		@DisplayName("Reservation is not new")
		void testAddReservationWhenReservationIsNotNewShouldNotInsertAgain() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			
			presenter.addReservation(client, A_DATE);
			
			assertThat(readAllReservationsFromDatabase()).containsOnlyOnce(reservation);
		}
	}

	@Nested
	@DisplayName("Integration tests for 'renameClient'")
	class RenameClientIT {
		private static final String LASTNAME_FIELD = "lastName";
		private static final String FIRSTNAME_FIELD = "firstName";

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME))
				.thenReturn(ANOTHER_FIRSTNAME);
			when(clientValidator.validateLastName(ANOTHER_LASTNAME))
				.thenReturn(ANOTHER_LASTNAME);
		}

		@Test
		@DisplayName("Renamed client is new")
		void testRenameClientWhenRenamedClientIsNewShouldRename() {
			addTestClientToDatabase(client);
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			assertThat(transactionalBookingService.findClient(client.getId()))
				.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
				.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME);
		}

		@Test
		@DisplayName("Renamed client is not new")
		void testRenameClientWhenRenamedClientIsNotNewShouldNotRename() {
			addTestClientToDatabase(client);
			addTestClientToDatabase(new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			assertThat(transactionalBookingService.findClient(client.getId()))
				.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, A_FIRSTNAME)
				.hasFieldOrPropertyWithValue(LASTNAME_FIELD, A_LASTNAME);
		}
	}

	@Nested
	@DisplayName("Integration tests for 'rescheduleReservation'")
	class RescheduleReservationIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
		}

		@Test
		@DisplayName("Rescheduled reservation is new")
		void testRescheduleReservationWhenRescheduledReservationIsNewShouldReschedule() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			assertThat(transactionalBookingService.findReservation(reservation.getId()))
				.extracting(Reservation::getDate).isEqualTo(ANOTHER_LOCALDATE);
		}

		@Test
		@DisplayName("Rescheduled reservation is not new")
		void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldNotReschedule() {
			addTestClientToDatabase(client);
			reservation.setClientId(client.getId());
			addTestReservationToDatabase(reservation);
			Reservation another_reservation = new Reservation(client.getId(), ANOTHER_LOCALDATE);
			addTestReservationToDatabase(another_reservation);
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			assertThat(transactionalBookingService.findReservation(reservation.getId()))
				.extracting(Reservation::getDate).isEqualTo(A_LOCALDATE);
		}
	}


	private void cleanDatabase() {
		for (Reservation reservation : transactionalBookingService.findAllReservations())
			transactionalBookingService.removeReservation(reservation.getId());
		for (Client client : transactionalBookingService.findAllClients())
			transactionalBookingService.removeClient(client.getId());
	}

	public void addTestClientToDatabase(Client client) {
		transactionalBookingService.insertNewClient(client);
	}

	public void addTestReservationToDatabase(Reservation reservation) {
		transactionalBookingService.insertNewReservation(reservation);
	}

	private List<Client> readAllClientsFromDatabase() {
		return transactionalBookingService.findAllClients();
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		return transactionalBookingService.findAllReservations();
	}
}