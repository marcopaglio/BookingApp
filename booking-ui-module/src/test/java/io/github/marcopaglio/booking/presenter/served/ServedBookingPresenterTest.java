package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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
import org.mockito.MockedStatic;
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
			inOrder.verify(view).showClientError(A_CLIENT.toString() + " has already been deleted.");
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
					"An error occurred while deleting " + A_CLIENT.toString() + ".");
			
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
					A_RESERVATION.toString() + " has already been deleted.");
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
					"An error occurred while deleting " + A_RESERVATION.toString() + ".");
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'addClient'")
	class AddClientTest {
		private MockedStatic<ClientValidator> mockedClientValidator =
				Mockito.mockStatic(ClientValidator.class);

		@AfterEach
		void close() {
			mockedClientValidator.close();
		}

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {

			@BeforeEach
			void stubbingValidator() {
				mockedClientValidator.when(
						() -> ClientValidator.newValidatedClient(A_FIRSTNAME, A_LASTNAME))
					.thenReturn(A_CLIENT);
			}

			@Test
			@DisplayName("Client is new")
			void testAddClientWhenClientIsNewShouldCreateDelegateToServiceNotifyViewAndReturn() {
				when(bookingService.insertNewClient(A_CLIENT)).thenReturn(A_CLIENT);
				
				assertThat(servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(A_CLIENT);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view, ClientValidator.class);
				
				inOrder.verify(mockedClientValidator,
						() -> ClientValidator.newValidatedClient(A_FIRSTNAME, A_LASTNAME));
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).clientAdded(A_CLIENT);
				
				verifyNoMoreInteractions(bookingService, view, ClientValidator.class);
			}

			@Test
			@DisplayName("Client is not new")
			void testAddClientWhenClientIsNotNewShouldReturnTheExistingOneAndUpdateView() {
				when(bookingService.insertNewClient(A_CLIENT))
					.thenThrow(new InstanceAlreadyExistsException());
				when(bookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME)).thenReturn(A_CLIENT);
				
				assertThat(servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(A_CLIENT);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view, ClientValidator.class);
				
				inOrder.verify(mockedClientValidator,
						() -> ClientValidator.newValidatedClient(A_FIRSTNAME, A_LASTNAME));
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(bookingService).findClientNamed(A_FIRSTNAME, A_LASTNAME);
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view, ClientValidator.class);
			}

			@Test
			@DisplayName("Client deleted before being found")
			void testAddClientWhenClientIsDeletedBeforeBeingFoundShouldRetryToInsert() {
				when(bookingService.insertNewClient(A_CLIENT))
					.thenThrow(new InstanceAlreadyExistsException())
					.thenReturn(A_CLIENT);
				when(bookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME))
					.thenThrow(new InstanceNotFoundException());
				
				servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
				
				mockedClientValidator.verify(
						() -> ClientValidator.newValidatedClient(A_FIRSTNAME, A_LASTNAME));
				verify(bookingService, times(2)).insertNewClient(A_CLIENT);
				verify(bookingService).findClientNamed(A_FIRSTNAME, A_LASTNAME);
				verify(view).clientAdded(A_CLIENT);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddClientWhenDatabaseRequestFailsShouldShowErrorAndThrow() {
				DatabaseException databaseException = new DatabaseException();
				
				doThrow(databaseException).when(bookingService).insertNewClient(A_CLIENT);
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(databaseException);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewClient(A_CLIENT);
				inOrder.verify(view).showClientError(
						"An error occurred while adding " + A_CLIENT.toString() + ".");
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@Nested
		@DisplayName("Validation is failure")
		class ValidationFailureTest {
			private IllegalArgumentException illegalArgumentException =
					new IllegalArgumentException();

			@BeforeEach
			void stubbingValidator() {
				mockedClientValidator.when(
						() -> ClientValidator.newValidatedClient(
								Mockito.<String>any(),
								Mockito.<String>any()))
					.thenThrow(illegalArgumentException);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Name is not valid")
			@NullSource
			@ValueSource(strings = {" ", "Mari0", "Mario!"})
			void testAddClientWhenNameIsNotValidShouldShowErrorAndThrow(String invalidName) {
				assertThatThrownBy(
						() -> servedBookingPresenter.addClient(invalidName, A_LASTNAME))
					.isEqualTo(illegalArgumentException);
				
				InOrder inOrder = Mockito.inOrder(view, ClientValidator.class);
				
				inOrder.verify(mockedClientValidator,
						() -> ClientValidator.newValidatedClient(invalidName, A_LASTNAME));
				inOrder.verify(view).showFormError("Client's name or surname is not valid.");
				
				verifyNoMoreInteractions(view, ClientValidator.class);
				verifyNoInteractions(bookingService);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Surname is not valid")
			@NullSource
			@ValueSource(strings = {" ", "Ro55i", "Rossi@"})
			void testAddClientWhenSurnameIsNotValidShouldShowErrorAndThrow(String invalidSurname) {
				assertThatThrownBy(
						() -> servedBookingPresenter.addClient(A_FIRSTNAME, invalidSurname))
					.isEqualTo(illegalArgumentException);
				
				InOrder inOrder = Mockito.inOrder(view, ClientValidator.class);
				
				inOrder.verify(mockedClientValidator,
						() -> ClientValidator.newValidatedClient(A_FIRSTNAME, invalidSurname));
				inOrder.verify(view).showFormError("Client's name or surname is not valid.");
				
				verifyNoMoreInteractions(view, ClientValidator.class);
				verifyNoInteractions(bookingService);
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'addReservation'")
	class AddReservationTest {
		private MockedStatic<ReservationValidator> mockedReservationValidator =
				Mockito.mockStatic(ReservationValidator.class);

		@AfterEach
		void close() {
			mockedReservationValidator.close();
		}

		@Nested
		@DisplayName("Validation is successful")
		class ValidationSuccessfulTest {

			@BeforeEach
			void stubbingValidator() {
				mockedReservationValidator.when(
						() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE))
					.thenReturn(A_RESERVATION);
			}

			@Test
			@DisplayName("Reservation is new")
			void testAddReservationWhenReservationIsNewShouldCreateDelegateToServiceAndNotifyView() {
				servedBookingPresenter.addReservation(A_DATE, A_CLIENT);
				
				InOrder inOrder = Mockito.inOrder(bookingService, view, ReservationValidator.class);
				
				inOrder.verify(mockedReservationValidator,
						() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).reservationAdded(A_RESERVATION);
				
				verifyNoMoreInteractions(bookingService, view, ReservationValidator.class);
			}

			@Test
			@DisplayName("Reservation is not new")
			void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceAlreadyExistsException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, A_CLIENT));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view, ReservationValidator.class);
				
				inOrder.verify(mockedReservationValidator,
						() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						A_RESERVATION.toString() + " has already been booked.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view, ReservationValidator.class);
			}

			@Test
			@DisplayName("Reservation's client is not in database")
			void testAddReservationWhenClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
				when(bookingService.insertNewReservation(A_RESERVATION))
					.thenThrow(new InstanceNotFoundException());
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, A_CLIENT));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view, ReservationValidator.class);
				
				inOrder.verify(mockedReservationValidator,
						() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						A_RESERVATION.toString() + "'s client has been deleted.");
				// updateAll
				inOrder.verify(bookingService).findAllReservations();
				inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
				inOrder.verify(bookingService).findAllClients();
				inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
				
				verifyNoMoreInteractions(bookingService, view, ReservationValidator.class);
			}

			@Test
			@DisplayName("Database request fails")
			void testAddReservationWhenDatabaseRequestFailsShouldNotThrowAndShowError() {
				doThrow(new DatabaseException())
					.when(bookingService).insertNewReservation(A_RESERVATION);
				
				assertThatNoException().isThrownBy(
						() -> servedBookingPresenter.addReservation(A_DATE, A_CLIENT));
				
				InOrder inOrder = Mockito.inOrder(bookingService, view);
				
				inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
				inOrder.verify(view).showReservationError(
						"An error occurred while adding " + A_RESERVATION.toString() + ".");
				
				verifyNoMoreInteractions(bookingService, view);
			}
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Date is not valid")
		@NullSource
		@ValueSource(strings = {"2O23-O4-24", "24-04-2023", "2022-13-12", "2022-08-32"})
		void testAddReservationWhenDateIsNotValidShouldShowErrorAndThrow(String invalidDate) {
			IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
			
			mockedReservationValidator.when(
					() -> ReservationValidator.newValidatedReservation(A_CLIENT, invalidDate))
				.thenThrow(illegalArgumentException);
			
			assertThatThrownBy(
					() -> servedBookingPresenter.addReservation(invalidDate, A_CLIENT))
				.isEqualTo(illegalArgumentException);
			
			InOrder inOrder = Mockito.inOrder(view, ReservationValidator.class);
			
			inOrder.verify(mockedReservationValidator,
					() -> ReservationValidator.newValidatedReservation(A_CLIENT, invalidDate));
			inOrder.verify(view).showFormError("Date of reservation is not valid.");
			
			verifyNoMoreInteractions(view, ReservationValidator.class);
			verifyNoInteractions(bookingService);
		}
	}
}