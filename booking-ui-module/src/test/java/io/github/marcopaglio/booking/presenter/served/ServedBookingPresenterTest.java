package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcopaglio.booking.exception.DatabaseException;
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;

@DisplayName("Tests for ServedBookingPresenter class")
@ExtendWith(MockitoExtension.class)
class ServedBookingPresenterTest {
	private static final int NUM_OF_THREADS = 10;

	final static private String A_FIRSTNAME = "Mario";
	final static private String A_LASTNAME = "Rossi";
	final static private Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);
	final static private UUID A_CLIENT_UUID = UUID.fromString("0617d050-9cde-49e5-8fca-d448a7115ccd");

	final static private String A_DATE = "2023-04-24";
	final static private LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	final static private Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
	final static private UUID A_RESERVATION_UUID = UUID.fromString("3069144c-5c3d-4ee2-9458-ac67dd763fff");

	@Mock
	private BookingService bookingService;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	@Mock
	private BookingView view;

	@InjectMocks
	private ServedBookingPresenter servedBookingPresenter;

	@BeforeAll
	static void setId() throws Exception {
		A_CLIENT.setId(A_CLIENT_UUID);
		A_RESERVATION.setId(A_RESERVATION_UUID);
	}

	@Nested
	@DisplayName("Tests for 'allClients'")
	class AllClientsTest {

		@Test
		@DisplayName("No clients in repository")
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallViewWithEmptyList() {
			// default stubbing for bookingService.findAllClients()

			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several clients in repository")
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallViewWithClientsAsList() {
			List<Client> clients = Arrays.asList(A_CLIENT, new Client("Maria", "De Lucia"));
			
			when(bookingService.findAllClients()).thenReturn(clients);
			
			servedBookingPresenter.allClients();
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(clients);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testAllClientsWhenDatabaseRequestFailsShouldShowErrorAndNotThrow() {
			doThrow(new DatabaseException()).when(bookingService).findAllClients();
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showOperationError("Something went wrong while updating clients.");
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested 
	@DisplayName("Tests for 'allReservations'")
	class AllReservationsTest {

		@Test
		@DisplayName("No reservations in repository")
		void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldCallViewWithEmptyList() {
			// default stubbing for bookingService.findAllReservations()
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several reservations in repository")
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallViewWithReservationsAsList() {
			List<Reservation> reservations = Arrays.asList(
					A_RESERVATION, new Reservation(A_CLIENT_UUID, LocalDate.parse("2023-09-05")));
			
			when(bookingService.findAllReservations()).thenReturn(reservations);
			
			servedBookingPresenter.allReservations();
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(reservations);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testAllReservationsWhenDatabaseRequestFailsShouldShowErrorAndNotThrow() {
			doThrow(new DatabaseException()).when(bookingService).findAllReservations();
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showOperationError("Something went wrong while updating reservations.");
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteClient'")
	class DeleteClientTest {

		@Test
		@DisplayName("Client is null")
		void testDeleteClientWhenClientIsNullShouldShowError() {
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.deleteClient(null));
		
			verify(view).showFormError("Select a client to delete.");
			verifyNoInteractions(bookingService);
		}

		@Test
		@DisplayName("Client is in repository")
		void testDeleteClientWhenClientIsInRepositoryShouldDelegateToServiceAndNotifyView() {
			servedBookingPresenter.deleteClient(A_CLIENT);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(view).clientRemoved(A_CLIENT);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldShowErrorAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).showOperationError(A_CLIENT.toString() + " no longer exists.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
			doThrow(new DatabaseException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).showOperationError(
					"Something went wrong while deleting " + A_CLIENT.toString() + ".");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Concurrent requests occur")
		void testDeleteClientWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
			List<Client> clients = new ArrayList<>();
			IntStream.range(0, NUM_OF_THREADS).forEach(i -> clients.add(i, A_CLIENT));
			
			doAnswer(invocation -> {
				if (clients.size() == NUM_OF_THREADS) {
					// wait for simulating database operations
					await().timeout(Duration.of(1, MILLIS));
					clients.remove(A_CLIENT);
					return null;
				}
				else throw new InstanceNotFoundException();
			}).when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
					.mapToObj(i -> new Thread(() ->
						servedBookingPresenter.deleteClient(A_CLIENT)))
					.peek(t -> t.start())
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
			
			// should be removed a single element from the list
			assertThat(clients).hasSize(NUM_OF_THREADS-1);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteReservation'")
	class DeleteReservationTest {

		@Test
		@DisplayName("Reservation is null")
		void testDeleteReservationWhenReservationIsNullShouldShowError() {
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(null));
		
			verify(view).showFormError("Select a reservation to delete.");
			verifyNoInteractions(bookingService);
		}

		@Test
		@DisplayName("Reservation is in repository")
		void testDeleteReservationWhenReservationIsInRepositoryShouldDelegateToServiceAndNotifyView() {
			servedBookingPresenter.deleteReservation(A_RESERVATION);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).reservationRemoved(A_RESERVATION);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowErrorAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).showOperationError(
					A_RESERVATION.toString() + " no longer exists.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
			doThrow(new DatabaseException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).showOperationError(
					"Something went wrong while deleting " + A_RESERVATION.toString() + ".");
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Concurrent requests occur")
		void testDeleteReservationWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
			List<Reservation> reservations = new ArrayList<>();
			IntStream.range(0, NUM_OF_THREADS).forEach(i -> reservations.add(i, A_RESERVATION));
			
			doAnswer(invocation -> {
				if (reservations.size() == NUM_OF_THREADS) {
					// wait for simulating database operations
					await().timeout(Duration.of(1, MILLIS));
					reservations.remove(A_RESERVATION);
					return null;
				}
				else throw new InstanceNotFoundException();
			}).when(bookingService).removeReservationOn(A_LOCALDATE);
			
			List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
					.mapToObj(i -> new Thread(() ->
						servedBookingPresenter.deleteReservation(A_RESERVATION)))
					.peek(t -> t.start())
					.collect(Collectors.toList());
			
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
			
			// should be removed a single element from the list
			assertThat(reservations).hasSize(NUM_OF_THREADS-1);
		}
	}

	@Nested
	@DisplayName("Tests for 'addClient'")
	class AddClientTest {

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {

			@BeforeEach
			void stubbingValidator() throws Exception {
				when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
				when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
			}

			@Test
			@DisplayName("Client is new")
			void testAddClientWhenClientIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewClient(A_CLIENT)).thenReturn(A_CLIENT);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(clientValidator).validateFirstName(A_FIRSTNAME);
				verify(clientValidator).validateLastName(A_LASTNAME);
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).clientAdded(A_CLIENT);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Client is not new")
			void testAddClientWhenClientIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewClient(A_CLIENT))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).showOperationError("A client named " + A_FIRSTNAME
						+ " " + A_LASTNAME + " has already been made.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewClient(A_CLIENT)).thenThrow(new DatabaseException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).showOperationError(
						"Something went wrong while adding " + A_CLIENT.toString() + ".");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testAddClientWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
				List<Client> clients = new ArrayList<>();
				
				doAnswer(invocation -> {
					if (!clients.contains(A_CLIENT)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						clients.add(A_CLIENT);
						return A_CLIENT;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).insertNewClient(A_CLIENT);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME)))
						.peek(t -> t.start())
						.collect(Collectors.toList());
				
				await().atMost(10, SECONDS)
					.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
				
				// there should be a single element in the list
				assertThat(clients).hasSize(1);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@Test
			@DisplayName("Name is not valid")
			void testAddClientWhenNameIsNotValidShouldShowErrorAndNotInsert() {
				when(clientValidator.validateFirstName(A_FIRSTNAME))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				verify(clientValidator).validateFirstName(A_FIRSTNAME);
				verify(view).showFormError("Client's name is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}

			@Test
			@DisplayName("Surname is not valid")
			void testAddClientWhenSurnameIsNotValidShouldShowErrorAndNotInsert() {
				when(clientValidator.validateLastName(A_LASTNAME))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				verify(clientValidator).validateLastName(A_LASTNAME);
				verify(view).showFormError("Client's surname is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'addReservation'")
	class AddReservationTest {

		@DisplayName("Client is null")
		@Test
		void testAddReservationWhenClientIsNullShouldShowError() {
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.addReservation(null, A_DATE));
			
			verify(view).showFormError("Select a client to add the reservation to.");
			verifyNoInteractions(bookingService);
		}

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {

			@BeforeEach
			void stubbingValidator() throws Exception {
				when(reservationValidator.validateClientId(A_CLIENT_UUID))
					.thenReturn(A_CLIENT_UUID);
				when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
			}

			@Test
			@DisplayName("Reservation is new")
			void testAddReservationWhenReservationIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewReservation(A_RESERVATION)).thenReturn(A_RESERVATION);
				
				servedBookingPresenter.addReservation(A_CLIENT, A_DATE);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(reservationValidator).validateDate(A_DATE);
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).reservationAdded(A_RESERVATION);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Reservation is not new")
			void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_CLIENT, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showOperationError(
						"A reservation on " + A_DATE + " has already been made.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Reservation's client is not in database")
			void testAddReservationWhenAssociatedClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_CLIENT, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showOperationError(
						A_RESERVATION.toString() + "'s client [id=" + A_CLIENT_UUID + "] no longer exists.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new DatabaseException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_CLIENT, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showOperationError(
						"Something went wrong while adding " + A_RESERVATION.toString() + ".");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testAddReservationWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
				List<Reservation> reservations = new ArrayList<>();
				
				doAnswer(invocation -> {
					if (!reservations.contains(A_RESERVATION)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						reservations.add(A_RESERVATION);
						return A_RESERVATION;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).insertNewReservation(A_RESERVATION);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.addReservation(A_CLIENT, A_DATE)))
						.peek(t -> t.start())
						.collect(Collectors.toList());
				
				await().atMost(10, SECONDS)
					.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
				
				// there should be a single element in the list
				assertThat(reservations).hasSize(1);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@Test
			@DisplayName("ClientId is not valid")
			void testAddReservationWhenClientIdIsNotValidShouldShowErrorAndNotInsert() {
				when(reservationValidator.validateClientId(A_CLIENT_UUID))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_CLIENT, A_DATE));
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(view).showFormError("Client's identifier associated with reservation is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}

			@Test
			@DisplayName("Date is not valid")
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndNotInsert() {
				when(reservationValidator.validateDate(A_DATE))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_CLIENT, A_DATE));
				
				verify(reservationValidator).validateDate(A_DATE);
				verify(view).showFormError("Reservation's date is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'renameClient'")
	class RenameClientTest {
		private String newFirstName = "Maria ";
		private String newLastName = "De  Lucia";

		@DisplayName("Client is null")
		@Test
		void testRenameClientWhenClientIsNullShouldShowError() {
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.renameClient(null, newFirstName, newLastName));
			
			verify(view).showFormError("Select a client to rename.");
			verifyNoInteractions(bookingService);
		}

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {
			private String validatedFirstName = "Maria";
			private String validatedLastName = "De Lucia";
			private Client renamedClient = new Client(validatedFirstName, validatedLastName);

			@BeforeEach
			void stubbingValidator() throws Exception {
				when(clientValidator.validateFirstName(newFirstName)).thenReturn(validatedFirstName);
				when(clientValidator.validateLastName(newLastName)).thenReturn(validatedLastName);
			}

			@Test
			@DisplayName("Renamed client is new")
			void testRenameClientWhenRenamedClientIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenReturn(renamedClient);
				
				servedBookingPresenter.renameClient(A_CLIENT, newFirstName, newLastName);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(clientValidator).validateLastName(newLastName);
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).clientRenamed(A_CLIENT, renamedClient);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Validated name has not changed")
			void testRenameClientWhenValidatedNameHasNotChangedShouldRename() {
				Client client = new Client(validatedFirstName, A_LASTNAME);
				client.setId(A_CLIENT_UUID);
				
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenReturn(renamedClient);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(clientValidator).validateLastName(newLastName);
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).clientRenamed(client, renamedClient);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Validated surname has not changed")
			void testRenameClientWhenValidatedSurnameHasNotChangedShouldRename() {
				Client client = new Client(A_FIRSTNAME, validatedLastName);
				client.setId(A_CLIENT_UUID);
				
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenReturn(renamedClient);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(clientValidator).validateLastName(newLastName);
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).clientRenamed(client, renamedClient);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Both validated names have not changed")
			void testRenameClientWhenBothValidatedNamesHaveNotChangedShouldShowErrorAndNotRename() {
				Client client = new Client(validatedFirstName, validatedLastName);
				client.setId(A_CLIENT_UUID);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(clientValidator).validateLastName(newLastName);
				verify(view).showFormError("Insert new names for the client to be renamed.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}

			@Test
			@DisplayName("Renamed client is not new")
			void testRenameClientWhenRenamedClientIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(A_CLIENT, newFirstName, newLastName));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).showOperationError("A client named " + validatedFirstName
						+ " " + validatedLastName + " has already been made.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Client is not in database")
			void testRenameClientWhenClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(A_CLIENT, newFirstName, newLastName));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).showOperationError(A_CLIENT.toString() + " no longer exists.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testRenameClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new DatabaseException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(A_CLIENT, newFirstName, newLastName));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				inOrder.verify(view).showOperationError(
						"Something went wrong while renaming " + A_CLIENT.toString() + ".");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testRenameClientWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
				List<Client> clients = new ArrayList<>();
				IntStream.range(0, NUM_OF_THREADS).forEach(i -> clients.add(i, A_CLIENT));
				
				doAnswer(invocation -> {
					if (!clients.contains(renamedClient)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						clients.remove(A_CLIENT);
						clients.add(renamedClient);
						return renamedClient;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.renameClient(A_CLIENT, newFirstName, newLastName)))
						.peek(t -> t.start())
						.collect(Collectors.toList());
				
				await().atMost(10, SECONDS)
					.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
				
				// should be renamed a single element of the list
				assertThat(clients)
					.filteredOn(c -> Objects.equals(c, renamedClient)).hasSize(1);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@Test
			@DisplayName("New name is not valid")
			void testRenameClientWhenNewNameIsNotValidShouldShowErrorAndNotRename() {
				when(clientValidator.validateFirstName(newFirstName))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(A_CLIENT, newFirstName, newLastName));
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(view).showFormError("Client's name is not valid.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}

			@Test
			@DisplayName("New surname is not valid")
			void testRenameClientWhenNewSurnameIsNotValidShouldShowErrorAndNotRename() {
				when(clientValidator.validateLastName(newLastName))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(A_CLIENT, newFirstName, newLastName));
				
				verify(clientValidator).validateLastName(newLastName);
				verify(view).showFormError("Client's surname is not valid.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'rescheduleReservation'")
	class RescheduleReservationTest {
		private String newDate = "2023-09-05";

		@DisplayName("Reservation is null")
		@Test
		void testRescheduleReservationWhenReservationIsNullShouldShowError() {
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.rescheduleReservation(null, newDate));
			
			verify(view).showFormError("Select a reservation to reschedule.");
			verifyNoInteractions(bookingService);
		}

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {
			private LocalDate validatedDate = LocalDate.parse(newDate);
			private Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, validatedDate);

			@BeforeEach
			void stubbingValidator() throws Exception {
				when(reservationValidator.validateDate(newDate)).thenReturn(validatedDate);
			}

			@Test
			@DisplayName("Rescheduled reservation is new")
			void testRescheduleReservationWhenRescheduledReservationIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenReturn(rescheduledReservation);
				
				servedBookingPresenter.rescheduleReservation(A_RESERVATION, newDate);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(reservationValidator).validateDate(newDate);
				inOrder.verify(bookingService)
					.rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				inOrder.verify(view).reservationRescheduled(A_RESERVATION, rescheduledReservation);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Rescheduled reservation is not new")
			void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(A_RESERVATION, newDate));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				inOrder.verify(view).showOperationError(
						"A reservation on " + validatedDate + " has already been made.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Reservation is not in database")
			void testRescheduleReservationWhenReservationIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(A_RESERVATION, newDate));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				inOrder.verify(view).showOperationError(
						A_RESERVATION.toString() + " no longer exists.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testRescheduleReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new DatabaseException());
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(A_RESERVATION, newDate));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService)
					.rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				inOrder.verify(view).showOperationError(
						"Something went wrong while rescheduling " + A_RESERVATION.toString() + ".");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Validated date has not changed")
			void testRescheduleReservationWhenValidatedDateHasNotChangedShouldShowErrorAndNotReschedule() {
				Reservation reservation = new Reservation(A_CLIENT_UUID, validatedDate);
				
				servedBookingPresenter.rescheduleReservation(reservation, newDate);
				
				verify(reservationValidator).validateDate(newDate);
				verify(view).showFormError("Insert a new date for the reservation to be rescheduled.");
				verify(bookingService, never())
					.rescheduleReservation(any(UUID.class), any(LocalDate.class));
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testRescheduleReservationWhenConcurrentRequestsOccurShouldAvoidRaceConditions() {
				List<Reservation> reservations = new ArrayList<>();
				IntStream.range(0, NUM_OF_THREADS).forEach(i -> reservations.add(i, A_RESERVATION));
				
				doAnswer(invocation -> {
					if (!reservations.contains(rescheduledReservation)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						reservations.remove(A_RESERVATION);
						reservations.add(rescheduledReservation);
						return rescheduledReservation;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.rescheduleReservation(A_RESERVATION, newDate)))
						.peek(t -> t.start())
						.collect(Collectors.toList());
				
				await().atMost(10, SECONDS)
					.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
				
				// should be rescheduled a single element of the list
				assertThat(reservations)
					.filteredOn(r -> Objects.equals(r, rescheduledReservation)).hasSize(1);
			}
		}

		@Test
		@DisplayName("New date is not valid")
		void testRescheduleReservationWhenNewDateIsNotValidShouldShowErrorAndNotReschedule() {
			when(reservationValidator.validateDate(newDate))
				.thenThrow(new IllegalArgumentException());
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter
					.rescheduleReservation(A_RESERVATION, newDate));
			
			verify(reservationValidator).validateDate(newDate);
			verify(view).showFormError("Reservation's date is not valid.");
			verify(bookingService, never())
				.rescheduleReservation(any(UUID.class), any(LocalDate.class));
		}
	}
}