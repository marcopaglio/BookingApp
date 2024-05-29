package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	final static private UUID A_CLIENT_UUID = UUID.fromString("0617d050-9cde-49e5-8fca-d448a7115ccd");

	final static private String A_DATE = "2023-04-24";
	final static private LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	final static private UUID A_RESERVATION_UUID = UUID.fromString("3069144c-5c3d-4ee2-9458-ac67dd763fff");

	final static private String CLIENT_STRING = "Client named " + A_FIRSTNAME + " " + A_LASTNAME;
	final static private String RESERVATION_STRING = "Reservation on " + A_DATE;

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

	private Client client;
	private Reservation reservation;

	@BeforeEach
	void setEntities() throws Exception {
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		client.setId(A_CLIENT_UUID);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		reservation.setId(A_RESERVATION_UUID);
	}

	@Nested
	@DisplayName("Tests for 'allClients'")
	class AllClientsTest {

		@Test
		@DisplayName("No clients in repository")
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallViewWithEmptyList() {
			// default stubbing for bookingService.findAllClients()
			
			servedBookingPresenter.allClients();
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(Collections.emptyList());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several clients in repository")
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallViewWithClientsAsList() {
			List<Client> clients = Arrays.asList(client, new Client("Maria", "De Lucia"));
			
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
			inOrder.verify(view).showAllReservations(Collections.emptyList());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several reservations in repository")
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallViewWithReservationsAsList() {
			List<Reservation> reservations = Arrays.asList(
					reservation, new Reservation(A_CLIENT_UUID, LocalDate.parse("2023-09-05")));
			
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
			// default stubbing for bookingService.removeClientNamed(firstName, lastName)
			// default stubbing for bookingService.findAllReservation()
			
			servedBookingPresenter.deleteClient(client);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(Collections.emptyList());
			inOrder.verify(view).clientRemoved(client);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldShowErrorAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for bookingService.findAllClients()
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(client));
			
			verify(view).showOperationError(CLIENT_STRING + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
			doThrow(new DatabaseException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for bookingService.findAllClients()
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(client));
			
			verify(view).showOperationError(
					"Something went wrong while deleting " + CLIENT_STRING + ".");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Concurrent requests occur")
		void testDeleteClientWhenConcurrentRequestsOccurShouldDeleteOnce() {
			List<Client> clients = new ArrayList<>();
			IntStream.range(0, NUM_OF_THREADS).forEach(i -> clients.add(i, client));
			
			doAnswer(invocation -> {
				if (clients.size() == NUM_OF_THREADS) {
					// wait for simulating database operations
					await().timeout(Duration.of(1, MILLIS));
					clients.remove(client);
					return null;
				}
				else throw new InstanceNotFoundException();
			}).when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
					.mapToObj(i -> new Thread(() ->
						servedBookingPresenter.deleteClient(client)))
					.peek(t -> t.start())
					.toList();
			
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
			// default stubbing for bookingService.removeReservationOn(date)
			
			servedBookingPresenter.deleteReservation(reservation);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).reservationRemoved(reservation);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowErrorAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for bookingService.findAllClients()
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(reservation));
			
			verify(view).showOperationError(RESERVATION_STRING + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
			doThrow(new DatabaseException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for bookingService.findAllClients()
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(reservation));
			
			verify(view).showOperationError(
					"Something went wrong while deleting " + RESERVATION_STRING + ".");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Concurrent requests occur")
		void testDeleteReservationWhenConcurrentRequestsOccurShouldDeleteOnce() {
			List<Reservation> reservations = new ArrayList<>();
			IntStream.range(0, NUM_OF_THREADS).forEach(i -> reservations.add(i, reservation));
			
			doAnswer(invocation -> {
				if (reservations.size() == NUM_OF_THREADS) {
					// wait for simulating database operations
					await().timeout(Duration.of(1, MILLIS));
					reservations.remove(reservation);
					return null;
				}
				else throw new InstanceNotFoundException();
			}).when(bookingService).removeReservationOn(A_LOCALDATE);
			
			List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
					.mapToObj(i -> new Thread(() ->
						servedBookingPresenter.deleteReservation(reservation)))
					.peek(t -> t.start())
					.toList();
			
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
			void testAddClientWhenClientIsNewShouldValidateItAndDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewClient(client)).thenReturn(client);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(clientValidator).validateFirstName(A_FIRSTNAME);
				verify(clientValidator).validateLastName(A_LASTNAME);
				inOrder.verify(bookingService).insertNewClient(client);
				inOrder.verify(view).clientAdded(client);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Client is not new")
			void testAddClientWhenClientIsNotNewShouldShowErrorAndUpdateView() {
				List<Client> clientsInDB = Arrays.asList(client);
				
				when(bookingService.insertNewClient(client))
					.thenThrow(new InstanceAlreadyExistsException());
				// default stubbing for bookingService.findAllReservations()
				when(bookingService.findAllClients()).thenReturn(clientsInDB);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				verify(view).showOperationError(CLIENT_STRING + " already exists.");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(clientsInDB);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewClient(client)).thenThrow(new DatabaseException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
				
				verify(view).showOperationError(
						"Something went wrong while adding " + CLIENT_STRING + ".");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testAddClientWhenConcurrentRequestsOccurShouldAddOnce() {
				List<Client> clients = new ArrayList<>();
				
				doAnswer(invocation -> {
					if (!clients.contains(client)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						clients.add(client);
						return client;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).insertNewClient(client);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME)))
						.peek(t -> t.start())
						.toList();
				
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
				verify(view).showFormError("Client's name [" + A_FIRSTNAME + "] is not valid.");
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
				verify(view).showFormError("Client's surname [" + A_LASTNAME + "] is not valid.");
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
			void testAddReservationWhenReservationIsNewShouldValidateItAndDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewReservation(reservation)).thenReturn(reservation);
				
				servedBookingPresenter.addReservation(client, A_DATE);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(reservationValidator).validateDate(A_DATE);
				inOrder.verify(bookingService).insertNewReservation(reservation);
				inOrder.verify(view).reservationAdded(reservation);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Reservation is not new")
			void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
				List<Reservation> reservationsInDB = Arrays.asList(reservation);
				List<Client> clientsInDB = Arrays.asList(client);
				
				when(bookingService.insertNewReservation(reservation))
					.thenThrow(new InstanceAlreadyExistsException());
				when(bookingService.findAllReservations()).thenReturn(reservationsInDB);
				when(bookingService.findAllClients()).thenReturn(clientsInDB);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(client, A_DATE));
				
				verify(view).showOperationError(RESERVATION_STRING + " already exists.");
				verify(view).showAllReservations(reservationsInDB);
				verify(view).showAllClients(clientsInDB);
			}

			@Test
			@DisplayName("Reservation's client is not in database")
			void testAddReservationWhenAssociatedClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(reservation))
					.thenThrow(new InstanceNotFoundException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(client, A_DATE));
				
				verify(view).showOperationError(CLIENT_STRING + " no longer exists.");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Database request fails")
			void testAddReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(reservation))
					.thenThrow(new DatabaseException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(client, A_DATE));
				
				verify(view).showOperationError(
						"Something went wrong while adding " + RESERVATION_STRING + ".");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testAddReservationWhenConcurrentRequestsOccurShouldAddOnce() {
				List<Reservation> reservations = new ArrayList<>();
				
				doAnswer(invocation -> {
					if (!reservations.contains(reservation)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						reservations.add(reservation);
						return reservation;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).insertNewReservation(reservation);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.addReservation(client, A_DATE)))
						.peek(t -> t.start())
						.toList();
				
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
						() -> servedBookingPresenter.addReservation(client, A_DATE));
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(view).showFormError("Reservation's client ID [" + A_CLIENT_UUID + "] is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}

			@Test
			@DisplayName("Date is not valid")
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndNotInsert() {
				when(reservationValidator.validateDate(A_DATE))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(client, A_DATE));
				
				verify(reservationValidator).validateDate(A_DATE);
				verify(view).showFormError("Reservation's date [" + A_DATE + "] is not valid.");
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
			void testRenameClientWhenRenamedClientIsNewShouldValidateItAndDelegateToServiceAndNotifyView() {
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
			@DisplayName("Validated name has not changed")
			void testRenameClientWhenValidatedNameHasNotChangedShouldRename() {
				client.setFirstName(validatedFirstName);
				
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenReturn(renamedClient);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				verify(view).clientRenamed(client, renamedClient);
			}

			@Test
			@DisplayName("Validated surname has not changed")
			void testRenameClientWhenValidatedSurnameHasNotChangedShouldRename() {
				client.setLastName(validatedLastName);
				
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenReturn(renamedClient);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				verify(bookingService)
					.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				verify(view).clientRenamed(client, renamedClient);
			}

			@Test
			@DisplayName("Both validated names have not changed")
			void testRenameClientWhenBothValidatedNamesHaveNotChangedShouldShowErrorAndNotRename() {
				client.setFirstName(validatedFirstName);
				client.setLastName(validatedLastName);
				
				servedBookingPresenter.renameClient(client, newFirstName, newLastName);
				
				verify(view).showFormError("Insert new names for the client to be renamed.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}

			@Test
			@DisplayName("Renamed client is not new")
			void testRenameClientWhenRenamedClientIsNotNewShouldShowErrorAndUpdateView() {
				List<Client> clientsInDB = Arrays.asList(
						client, new Client(validatedFirstName, validatedLastName));
				
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new InstanceAlreadyExistsException());
				// default stubbing for bookingService.findAllReservations()
				when(bookingService.findAllClients()).thenReturn(clientsInDB);
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(client, newFirstName, newLastName));
				
				verify(view).showOperationError("Client named " + validatedFirstName
						+ " " + validatedLastName + " already exists.");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(clientsInDB);
			}

			@Test
			@DisplayName("Client is not in database")
			void testRenameClientWhenClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new InstanceNotFoundException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(client, newFirstName, newLastName));
				
				verify(view).showOperationError(CLIENT_STRING + " no longer exists.");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Database request fails")
			void testRenameClientWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName))
					.thenThrow(new DatabaseException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(client, newFirstName, newLastName));
				
				verify(view).showOperationError(
						"Something went wrong while renaming " + CLIENT_STRING + ".");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testRenameClientWhenConcurrentRequestsOccurShouldRenameOnce() {
				List<Client> clients = new ArrayList<>();
				IntStream.range(0, NUM_OF_THREADS).forEach(i -> clients.add(i, client));
				
				doAnswer(invocation -> {
					if (!clients.contains(renamedClient)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						clients.remove(client);
						clients.add(renamedClient);
						return renamedClient;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).renameClient(A_CLIENT_UUID, validatedFirstName, validatedLastName);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.renameClient(client, newFirstName, newLastName)))
						.peek(t -> t.start())
						.toList();
				
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
						.renameClient(client, newFirstName, newLastName));
				
				verify(clientValidator).validateFirstName(newFirstName);
				verify(view).showFormError("Client's name [" + newFirstName + "] is not valid.");
				verify(bookingService, never())
					.renameClient(same(A_CLIENT_UUID), anyString(), anyString());
			}

			@Test
			@DisplayName("New surname is not valid")
			void testRenameClientWhenNewSurnameIsNotValidShouldShowErrorAndNotRename() {
				when(clientValidator.validateLastName(newLastName))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.renameClient(client, newFirstName, newLastName));
				
				verify(clientValidator).validateLastName(newLastName);
				verify(view).showFormError("Client's surname [" + newLastName + "] is not valid.");
				verify(bookingService, never())
					.renameClient(same(A_CLIENT_UUID), anyString(), anyString());
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
			void testRescheduleReservationWhenRescheduledReservationIsNewShouldValidateItAndDelegateToServiceAndNotifyView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenReturn(rescheduledReservation);
				
				servedBookingPresenter.rescheduleReservation(reservation, newDate);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				verify(reservationValidator).validateDate(newDate);
				inOrder.verify(bookingService)
					.rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				inOrder.verify(view).reservationRescheduled(reservation, rescheduledReservation);
				
				verifyNoMoreInteractions(bookingService, view);
			}

			@Test
			@DisplayName("Rescheduled reservation is not new")
			void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowErrorAndUpdateView() {
				List<Reservation> reservationsInDB = Arrays.asList(
						reservation, new Reservation(A_RESERVATION_UUID, validatedDate));
				List<Client> clientsInDB = Arrays.asList(client);
				
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new InstanceAlreadyExistsException());
				when(bookingService.findAllReservations()).thenReturn(reservationsInDB);
				when(bookingService.findAllClients()).thenReturn(clientsInDB);
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(reservation, newDate));
				
				verify(view).showOperationError(
						"Reservation on " + validatedDate + " already exists.");
				verify(view).showAllReservations(reservationsInDB);
				verify(view).showAllClients(clientsInDB);
			}

			@Test
			@DisplayName("Reservation is not in database")
			void testRescheduleReservationWhenReservationIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new InstanceNotFoundException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(reservation, newDate));
				
				verify(view).showOperationError(RESERVATION_STRING + " no longer exists.");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Database request fails")
			void testRescheduleReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.rescheduleReservation(A_RESERVATION_UUID, validatedDate))
					.thenThrow(new DatabaseException());
				// default stubbing for bookingService.findAllReservations()
				// default stubbing for bookingService.findAllClients()
				
				assertThatNoException().isThrownBy(() -> servedBookingPresenter
						.rescheduleReservation(reservation, newDate));
				
				verify(view).showOperationError(
						"Something went wrong while rescheduling " + RESERVATION_STRING + ".");
				verify(view).showAllReservations(Collections.emptyList());
				verify(view).showAllClients(Collections.emptyList());
			}

			@Test
			@DisplayName("Validated date has not changed")
			void testRescheduleReservationWhenValidatedDateHasNotChangedShouldShowErrorAndNotReschedule() {
				reservation.setDate(validatedDate);
				
				servedBookingPresenter.rescheduleReservation(reservation, newDate);
				
				verify(reservationValidator).validateDate(newDate);
				verify(view).showFormError("Insert a new date for the reservation to be rescheduled.");
				verify(bookingService, never())
					.rescheduleReservation(any(UUID.class), any(LocalDate.class));
			}

			@Test
			@DisplayName("Concurrent requests occur")
			void testRescheduleReservationWhenConcurrentRequestsOccurShouldRescheduleOnce() {
				List<Reservation> reservations = new ArrayList<>();
				IntStream.range(0, NUM_OF_THREADS).forEach(i -> reservations.add(i, reservation));
				
				doAnswer(invocation -> {
					if (!reservations.contains(rescheduledReservation)) {
						// wait for simulating database operations
						await().timeout(Duration.of(1, MILLIS));
						reservations.remove(reservation);
						reservations.add(rescheduledReservation);
						return rescheduledReservation;
					}
					else throw new InstanceAlreadyExistsException();
				}).when(bookingService).rescheduleReservation(A_RESERVATION_UUID, validatedDate);
				
				List<Thread> threads = IntStream.range(0, NUM_OF_THREADS)
						.mapToObj(i -> new Thread(() ->
							servedBookingPresenter.rescheduleReservation(reservation, newDate)))
						.peek(t -> t.start())
						.toList();
				
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
					.rescheduleReservation(reservation, newDate));
			
			verify(reservationValidator).validateDate(newDate);
			verify(view).showFormError("Reservation's date [" + newDate + "] is not valid.");
			verify(bookingService, never())
				.rescheduleReservation(same(A_RESERVATION_UUID), any(LocalDate.class));
		}
	}
}