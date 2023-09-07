package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

		@DisplayName("Client is null")
		@Test
		void testAddReservationWhenClientIsNullShouldNotInsertAndNotShowError() {
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.addReservation(null, A_DATE));
			
			verify(view).showFormError("Select a client to add the reservation to.");
			verify(bookingService, never()).insertNewReservation(any(Reservation.class));
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
			void testAddClientWhenClientIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewClient(A_CLIENT)).thenReturn(A_CLIENT);
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				InOrder inOrder = Mockito.inOrder(clientValidator, bookingService, view);
				
				verify(clientValidator).validateFirstName(A_FIRSTNAME);
				verify(clientValidator).validateLastName(A_LASTNAME);
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).clientAdded(A_CLIENT);
				
				verifyNoMoreInteractions(clientValidator, bookingService, view);
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
				inOrder.verify(view).showClientError(
						"Client [" + A_FIRSTNAME + " " + A_LASTNAME + "] already exists.");
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
				inOrder.verify(view).showClientError("Something went wrong while adding Client ["
						+ A_FIRSTNAME + " " + A_LASTNAME + "].");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@Test
			@DisplayName("Name is not valid")
			void testAddClientWhenNameIsNotValidShouldNotInsertAndShowError() {
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
			void testAddClientWhenSurnameIsNotValidShouldNotInsertAndShowError() {
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
			void testAddReservationWhenReservationIsNewShouldValidateItDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewReservation(A_RESERVATION)).thenReturn(A_RESERVATION);
				
				servedBookingPresenter.addReservation(spiedClient, A_DATE);
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(reservationValidator).validateDate(A_DATE);
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
						() -> servedBookingPresenter.addReservation(spiedClient, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"Reservation [date=" + A_LOCALDATE + "] is already booked.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(reservationValidator, bookingService, view);
			}

			@Test
			@DisplayName("Reservation's client is not in database")
			void testAddReservationWhenAssociatedClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(spiedClient, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(reservationValidator, bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"Reservation [date=" + A_LOCALDATE + "]'s client no longer exists.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(reservationValidator, bookingService, view);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddReservationWhenDatabaseRequestFailsShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new DatabaseException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(spiedClient, A_DATE));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"Something went wrong while adding Reservation [date=" + A_LOCALDATE + "].");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@Test
			@DisplayName("ClientId is not valid")
			void testAddReservationWhenClientIdIsNotValidShouldNotInsertAndShowError() {
				when(reservationValidator.validateClientId(A_CLIENT_UUID))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(spiedClient, A_DATE));
				
				verify(reservationValidator).validateClientId(A_CLIENT_UUID);
				verify(view).showFormError("Client's identifier associated with reservation is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}

			@Test
			@DisplayName("Date is not valid")
			@ValueSource(strings = {"2O23-O4-24", "24-04-2023", "2022-13-12", "2022-08-32"})
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndThrow() {
				when(reservationValidator.validateDate(A_DATE))
					.thenThrow(illegalArgumentException);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(spiedClient, A_DATE));
				
				verify(reservationValidator).validateDate(A_DATE);
				verify(view).showFormError("Date of reservation is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}
		}
	}
}