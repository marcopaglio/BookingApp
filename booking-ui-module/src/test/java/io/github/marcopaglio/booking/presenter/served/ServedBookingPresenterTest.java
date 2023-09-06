package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import io.github.marcopaglio.booking.view.View;

@DisplayName("Tests for ServedBookingPresenter class")
@ExtendWith(MockitoExtension.class)
class ServedBookingPresenterTest {
	final static private String A_FIRSTNAME = "Mario";
	final static private String A_LASTNAME = "Rossi";
	final static private Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);
	
	final static private UUID A_CLIENT_UUID = UUID.fromString("0617d050-9cde-49e5-8fca-d448a7115ccd");
	final static private String A_DATE = "2023-04-24";
	final static private LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	final static private Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, A_LOCALDATE);

	@Mock
	private BookingService bookingService;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	@Mock
	private View view;

	@InjectMocks
	private ServedBookingPresenter servedBookingPresenter;

	@Nested
	@DisplayName("Null inputs on methods")
	class NullInputsTest {

		@Test
		@DisplayName("Null client on 'deleteClient'")
		void testDeleteClientWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> servedBookingPresenter.deleteClient(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to delete cannot be null.");
		
			verifyNoInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Null reservation on 'deleteReservation'")
		void testDeleteReservationWhenDateIsNullShouldThrow() {
			assertThatThrownBy(() -> servedBookingPresenter.deleteReservation(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to delete cannot be null.");
			
			verifyNoInteractions(bookingService, view);
		}

		@DisplayName("Null client on 'addReservation'")
		@Test
		void testAddReservationWhenClientIsNullShouldThrow() {
			assertThatThrownBy(
					() -> servedBookingPresenter.addReservation(A_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation's client to add cannot be null.");
			
			verifyNoInteractions(bookingService, view);
		}
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
		@DisplayName("Database request fails on 'allClients'")
		void testAllClientsWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
			doThrow(new DatabaseException()).when(bookingService).findAllClients();
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showClientError("An error occurred while updating clients.");
			
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
		void testAllReservationsWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
			doThrow(new DatabaseException()).when(bookingService).findAllReservations();
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showReservationError("An error occurred while updating reservations.");
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteClient'")
	class DeleteClientTest {

		@Test
		@DisplayName("Client is in repository")
		void testDeleteClientWhenClientIsInRepositoryShouldDelegateToServiceAndNotifyView() {
			servedBookingPresenter.deleteClient(A_CLIENT);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).clientRemoved(A_CLIENT);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldNotifyAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).showClientError(
					"Client [" + A_FIRSTNAME + " " + A_LASTNAME + "] has already been deleted.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteClientWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
			doThrow(new DatabaseException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).showClientError(
					"An error occurred while deleting Client [" + A_FIRSTNAME + " " + A_LASTNAME + "].");
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteReservation'")
	class DeleteReservationTest {

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
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldNotifyAndUpdateView() {
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).showReservationError(
					"Reservation [date=" + A_LOCALDATE + "] has already been deleted.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Database request fails")
		void testDeleteReservationWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
			doThrow(new DatabaseException()).when(bookingService).removeReservationOn(A_LOCALDATE);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).showReservationError(
					"An error occurred while deleting Reservation [date=" + A_LOCALDATE + "].");
			
			verifyNoMoreInteractions(bookingService, view);
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
			void testAddClientWhenClientIsNewShouldCreateItDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewClient(A_CLIENT)).thenReturn(A_CLIENT);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(clientValidator, bookingService, view);
				
				inOrder.verify(clientValidator).validateFirstName(A_FIRSTNAME);
				inOrder.verify(clientValidator).validateLastName(A_LASTNAME);
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).clientAdded(A_CLIENT);
				
				verifyNoMoreInteractions(clientValidator, bookingService, view);
			}

			@Test
			@DisplayName("Client is not new")
			void testAddClientWhenClientIsNotNewShouldUpdateView() {
				when(bookingService.insertNewClient(A_CLIENT))
					.thenThrow(new InstanceAlreadyExistsException());
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(clientValidator, bookingService, view);
				
				inOrder.verify(clientValidator).validateFirstName(A_FIRSTNAME);
				inOrder.verify(clientValidator).validateLastName(A_LASTNAME);
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(clientValidator, bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddClientWhenDatabaseRequestFailsShouldShowError() {
				doThrow(new DatabaseException()).when(bookingService).insertNewClient(A_CLIENT);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).showClientError(
						"An error occurred while adding Client [" + A_FIRSTNAME + " " + A_LASTNAME + "].");
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Name is not valid")
			@NullSource
			@ValueSource(strings = {" ", "Mari0", "Mario!"})
			void testAddClientWhenNameIsNotValidShouldShowError(String invalidName) {
				when(clientValidator.validateFirstName(invalidName))
					.thenThrow(illegalArgumentException);
				
				servedBookingPresenter.addClient(invalidName, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(clientValidator, view);
				
				inOrder.verify(clientValidator).validateFirstName(invalidName);
				inOrder.verify(view).showFormError("Client's name or surname is not valid.");
				
				verifyNoMoreInteractions(clientValidator, view);
				verifyNoInteractions(bookingService);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Surname is not valid")
			@NullSource
			@ValueSource(strings = {" ", "Ro55i", "Rossi@"})
			void testAddClientWhenSurnameIsNotValidShouldShowError(String invalidSurname) {
				when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
				when(clientValidator.validateLastName(invalidSurname))
					.thenThrow(illegalArgumentException);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, invalidSurname);
				
				InOrder inOrder = Mockito.inOrder(clientValidator, view);
				
				inOrder.verify(clientValidator).validateFirstName(A_FIRSTNAME);
				inOrder.verify(clientValidator).validateLastName(invalidSurname);
				inOrder.verify(view).showFormError("Client's name or surname is not valid.");
				
				verifyNoMoreInteractions(clientValidator, view);
				verifyNoInteractions(bookingService);
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'addReservation'")
	class AddReservationTest {
		private Client spiedClient = spy(A_CLIENT);

		@BeforeEach
		void stubbingClient() throws Exception {
			when(spiedClient.getId()).thenReturn(A_CLIENT_UUID);
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
			void testAddReservationWhenReservationIsNewShouldCreateDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenReturn(A_RESERVATION);
				
				servedBookingPresenter.addReservation(A_DATE, spiedClient);
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				inOrder.verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				inOrder.verify(reservationValidator).validateDate(A_DATE);
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).reservationAdded(A_RESERVATION);
				
				verifyNoMoreInteractions(reservationValidator, bookingService, view);
			}

			@Test
			@DisplayName("Reservation is not new")
			void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, spiedClient));
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				inOrder.verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				inOrder.verify(reservationValidator).validateDate(A_DATE);
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"Reservation [date=" + A_LOCALDATE + "] has already been booked.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(reservationValidator, bookingService, view);
			}

			@Test
			@DisplayName("Reservation's client is not in database")
			void testAddReservationWhenClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, spiedClient));
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				inOrder.verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				inOrder.verify(reservationValidator).validateDate(A_DATE);
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"Reservation [date=" + A_LOCALDATE + "]'s client has been deleted.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(reservationValidator, bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddReservationWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
				doThrow(new DatabaseException())
					.when(bookingService).insertNewReservation(A_RESERVATION);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, spiedClient));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"An error occurred while adding Reservation [date=" + A_LOCALDATE + "].");
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@DisplayName("ClientId is not valid")
			void testAddReservationWhenClientIdIsNotValidShouldShowErrorAndThrow() {
				when(reservationValidator.validateClientId(A_CLIENT_UUID))
					.thenThrow(illegalArgumentException);
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, spiedClient))
					.isEqualTo(illegalArgumentException);
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(view).showFormError("Date of reservation is not valid.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Date is not valid")
			@NullSource
			@ValueSource(strings = {"2O23-O4-24", "24-04-2023", "2022-13-12", "2022-08-32"})
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndThrow(String invalidDate) {
				when(reservationValidator.validateDate(invalidDate))
					.thenThrow(illegalArgumentException);
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addReservation(invalidDate, spiedClient))
					.isEqualTo(illegalArgumentException);
				
				verify(reservationValidator).validateDate(invalidDate);
				verify(view).showFormError("Date of reservation is not valid.");
			}
		}
	}
}