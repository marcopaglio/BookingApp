package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
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
	final static private UUID A_CLIENT_UUID = UUID.fromString("0617d050-9cde-49e5-8fca-d448a7115ccd");
	final static private Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);
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
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallTheViewWithEmptyList() {
			// default stubbing for bookingService.findAllClients()
			// default stubbing for view.showAllClients(clients)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Single client in repository")
		void testAllClientsWhenThereIsASingleClientInRepositoryShouldCallTheViewWithTheClientAsList() {
			List<Client> clients = Arrays.asList(A_CLIENT);
			
			when(bookingService.findAllClients()).thenReturn(clients);
			// default stubbing for view.showAllClients(clients)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(clients);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several clients in repository")
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallTheViewWithClientsAsList() {
			UUID anotherUuid = UUID.fromString("ac9c9315-6e61-43bd-a4f8-6ac3319de01c");
			Client anotherClient = new Client("Maria", "De Lucia", anotherUuid);
			List<Client> clients = Arrays.asList(A_CLIENT, anotherClient);
			
			when(bookingService.findAllClients()).thenReturn(clients);
			// default stubbing for view.showAllClients(clients)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(clients);
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested 
	@DisplayName("Tests for 'allReservations'")
	class AllReservationsTest {

		@Test
		@DisplayName("No reservations in repository")
		void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldCallTheViewWithEmptyList() {
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for view.showAllReservations(reservations)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Single reservation in repository")
		void testAllReservationsWhenThereIsASingleReservationInRepositoryShouldCallTheViewWithTheReservationAsList() {
			List<Reservation> reservations = Arrays.asList(A_RESERVATION);
			
			when(bookingService.findAllReservations()).thenReturn(reservations);
			// default stubbing for view.showAllReservations(reservations)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(reservations);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Several reservations in repository")
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallTheViewWithReservationsAsList() {
			Reservation anotherReservation =
					new Reservation(A_CLIENT_UUID, LocalDate.parse("2023-09-05"));
			List<Reservation> reservations = Arrays.asList(A_RESERVATION, anotherReservation);
			
			when(bookingService.findAllReservations()).thenReturn(reservations);
			// default stubbing for view.showAllReservations(reservations)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(reservations);
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteClient'")
	class DeleteClientTest {

		@Test
		@DisplayName("Client is in repository")
		void testDeleteClientWhenClientIsInRepositoryShouldDelegateToServiceAndNotifyTheView() {
			// default stubbing for bookingService.removeClientNamed(firstName, lastName)
			// default stubbing for view.clientRemoved(client)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).clientRemoved(A_CLIENT);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldNotifyTheView() {
			doThrow(new NoSuchElementException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			// default stubbing for view.showClientError(client, message)
			// default stubbing for bookingService.findAllReservation()
			// default stubbing for view.showAllReservations(reservations)
			// default stubbing for bookingService.findAllClients()
			// default stubbing for view.showAllClients(clients)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(A_CLIENT));
			
			inOrder.verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			inOrder.verify(view).showClientError(A_CLIENT, " has already been deleted.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests for 'deleteReservation'")
	class DeleteReservationTest {

		@Test
		@DisplayName("Reservation is in repository")
		void testDeleteReservationWhenReservationIsInRepositoryShouldDelegateToServiceAndNotifyTheView() {
			// default stubbing for bookingService.removeReservationOn(date)
			// default stubbing for view.reservationRemoved(reservation)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).reservationRemoved(A_RESERVATION);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldNotifyTheView() {
			doThrow(new NoSuchElementException()).when(bookingService).removeReservationOn(A_LOCALDATE);
			// default stubbing for view.showReservationError(reservation, message)
			// default stubbing for bookingService.findAllReservation()
			// default stubbing for view.showAllReservations(reservations)
			// default stubbing for bookingService.findAllClients()
			// default stubbing for view.showAllClients(clients)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(A_RESERVATION));
			
			inOrder.verify(bookingService).removeReservationOn(A_LOCALDATE);
			inOrder.verify(view).showReservationError(A_RESERVATION, " has already been deleted.");
			// updateAll
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@Nested
	@DisplayName("Tests using spied presenter")
	class SpiedPresenterTest {
		private ServedBookingPresenter spiedServedBookingPresenter;

		@BeforeEach
		void setUp() {
			spiedServedBookingPresenter = spy(servedBookingPresenter);
		}

		@Nested
		@DisplayName("Tests using ClientValidator")
		class ClientValidatorTest {
			private MockedStatic<ClientValidator> mockedClientValidator =
					Mockito.mockStatic(ClientValidator.class);

			@AfterEach
			void close() {
				mockedClientValidator.close();
			}

			@Nested
			@DisplayName("Tests for 'addClient'")
			class AddClientTest {

				@Test
				@DisplayName("Client is new")
				void testAddClientWhenClientIsNewShouldCreateDelegateNotifyAndReturn() {
					mockedClientValidator.when(
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)))
						.thenReturn(A_CLIENT);
					when(bookingService.insertNewClient(A_CLIENT)).thenReturn(A_CLIENT);
					// default stubbing for view.clientAdded(client)
					
					InOrder inOrder = Mockito.inOrder(bookingService, view, ClientValidator.class);
					
					assertThat(servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(A_CLIENT);
					
					inOrder.verify(mockedClientValidator,
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)));
					inOrder.verify(bookingService).insertNewClient(A_CLIENT);
					inOrder.verify(view).clientAdded(A_CLIENT);
					
					verifyNoMoreInteractions(bookingService, view, ClientValidator.class);
				}

				@Test
				@DisplayName("Client is not new")
				void testAddClientWhenClientIsNotNewShouldReturnTheExistingOne() {
					mockedClientValidator.when(
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)))
						.thenReturn(A_CLIENT);
					when(bookingService.insertNewClient(A_CLIENT)).thenThrow(new InstanceAlreadyExistsException());
					when(bookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME)).thenReturn(A_CLIENT);
					
					InOrder inOrder = Mockito.inOrder(bookingService, spiedServedBookingPresenter, ClientValidator.class);
					
					assertThat(spiedServedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(A_CLIENT);
					
					inOrder.verify(mockedClientValidator,
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)));
					inOrder.verify(bookingService).insertNewClient(A_CLIENT);
					inOrder.verify(bookingService).findClientNamed(A_FIRSTNAME, A_LASTNAME);
					// updateAll
					inOrder.verify(spiedServedBookingPresenter).allReservations();
					inOrder.verify(spiedServedBookingPresenter).allClients();
				}

				@Test
				@DisplayName("Client deleted before being found")
				void testAddClientWhenClientIsDeletedBeforeBeingFoundShouldRetryToInsert() {
					mockedClientValidator.when(
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)))
						.thenReturn(A_CLIENT);
					when(bookingService.insertNewClient(A_CLIENT))
						.thenThrow(new InstanceAlreadyExistsException())
						.thenReturn(A_CLIENT);
					when(bookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME))
						.thenThrow(new NoSuchElementException());
					
					InOrder inOrder = Mockito.inOrder(bookingService, view, ClientValidator.class);
					
					assertThat(servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(A_CLIENT);
					
					inOrder.verify(mockedClientValidator,
							() -> ClientValidator.newValidatedClient(
									same(A_FIRSTNAME), same(A_LASTNAME), isA(UUID.class)));
					inOrder.verify(bookingService).insertNewClient(A_CLIENT);
					inOrder.verify(bookingService).findClientNamed(A_FIRSTNAME, A_LASTNAME);
					// times(2)?
					inOrder.verify(bookingService).insertNewClient(A_CLIENT);
					inOrder.verify(view).clientAdded(A_CLIENT);
					
					verifyNoMoreInteractions(bookingService, view, ClientValidator.class);
				}

				@Nested
				@DisplayName("Invalid inputs")
				class InvalidInputsTest {

					@ParameterizedTest(name = "{index}: ''{0}''")
					@DisplayName("Name is not valid")
					@NullSource
					@ValueSource(strings = {" ", "Mari0", "Mario!"})
					void testAddClientWhenNameIsNotValidShouldShowErrorAndThrow(String invalidName) {
						mockedClientValidator.when(
								() -> ClientValidator.newValidatedClient(
										same(invalidName), same(A_LASTNAME), isA(UUID.class)))
							.thenThrow(new IllegalArgumentException());
						
						InOrder inOrder = Mockito.inOrder(view, ClientValidator.class);
						
						assertThatThrownBy(
								() -> servedBookingPresenter.addClient(invalidName, A_LASTNAME))
							.isInstanceOf(IllegalArgumentException.class);
						
						inOrder.verify(mockedClientValidator,
								() -> ClientValidator.newValidatedClient(
										same(invalidName), same(A_LASTNAME), isA(UUID.class)));
						inOrder.verify(view).showFormError("Client's name or surname is not valid.");
						
						//verify(bookingService, never()).insertNewClient(any());
						//verify(view, never()).clientAdded(any());
						verifyNoMoreInteractions(view, ClientValidator.class);
						verifyNoInteractions(bookingService);
					}

					@ParameterizedTest(name = "{index}: ''{0}''")
					@DisplayName("Surname is not valid")
					@NullSource
					@ValueSource(strings = {" ", "Ro55i", "Rossi@"})
					void testAddClientWhenSurnameIsNotValidShouldShowErrorAndThrow(String invalidSurname) {
						mockedClientValidator.when(
								() -> ClientValidator.newValidatedClient(
										same(A_FIRSTNAME), same(invalidSurname), isA(UUID.class)))
							.thenThrow(new IllegalArgumentException());
						
						InOrder inOrder = Mockito.inOrder(view, ClientValidator.class);
						
						assertThatThrownBy(
								() -> servedBookingPresenter.addClient(A_FIRSTNAME, invalidSurname))
							.isInstanceOf(IllegalArgumentException.class);
						
						inOrder.verify(mockedClientValidator,
								() -> ClientValidator.newValidatedClient(
										same(A_FIRSTNAME), same(invalidSurname), isA(UUID.class)));
						inOrder.verify(view).showFormError("Client's name or surname is not valid.");
						
						//verify(bookingService, never()).insertNewClient(any());
						//verify(view, never()).clientAdded(any());
						verifyNoMoreInteractions(view, ClientValidator.class);
						verifyNoInteractions(bookingService);
					}
				}
			}
		}

		@Nested
		@DisplayName("Tests using ReservationValidator")
		class ReservationValidatorTest {
			private MockedStatic<ReservationValidator> mockedReservationValidator =
					Mockito.mockStatic(ReservationValidator.class);

			@AfterEach
			void close() {
				mockedReservationValidator.close();
			}

			@Nested
			@DisplayName("Tests for 'addReservation'")
			class AddReservationTest {

				@Test
				@DisplayName("Reservation is new")
				void testAddReservationWhenReservationIsNewShouldCreateDelegateAndNotify() {
					mockedReservationValidator.when(
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE))
						.thenReturn(A_RESERVATION);
					// default stubbing for bookingService.insertNewReservation(reservation)
					// default stubbing for view.reservationAdded(reservation)
					
					InOrder inOrder = Mockito.inOrder(bookingService, view, ReservationValidator.class);
					
					assertThatNoException().isThrownBy(
							() -> servedBookingPresenter.addReservation(A_DATE, A_CLIENT));
					
					inOrder.verify(mockedReservationValidator,
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
					inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
					inOrder.verify(view).reservationAdded(A_RESERVATION);
					
					verifyNoMoreInteractions(bookingService, view, ReservationValidator.class);
				}

				@Test
				@DisplayName("Reservation is not new")
				void testNewReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
					mockedReservationValidator.when(
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE))
						.thenReturn(A_RESERVATION);
					when(bookingService.insertNewReservation(A_RESERVATION))
						.thenThrow(new InstanceAlreadyExistsException());
					// default stubbing for view.showReservationError(reservation, message)
					// default stubbing for bookingService.findAllReservations()
					// default stubbing for view.showAllReservations(reservations)
					// default stubbing for bookingService.findAllClients()
					// default stubbing for view.showAllClients(clients)
					
					InOrder inOrder = Mockito.inOrder(bookingService, view, spiedServedBookingPresenter, ReservationValidator.class);
					
					assertThatNoException().isThrownBy(
							() -> spiedServedBookingPresenter.addReservation(A_DATE, A_CLIENT));
					
					inOrder.verify(mockedReservationValidator,
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
					inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
					inOrder.verify(view).showReservationError(A_RESERVATION, " has already been booked.");
					// updateAll
					inOrder.verify(spiedServedBookingPresenter).allReservations();
					inOrder.verify(spiedServedBookingPresenter).allClients();
				}

				@Test
				@DisplayName("Reservation's client is not in database")
				void testNewReservationWhenClientIsNotInDatabaseShouldShowErrorAndUpdateView() {
					mockedReservationValidator.when(
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE))
						.thenReturn(A_RESERVATION);
					when(bookingService.insertNewReservation(A_RESERVATION))
						.thenThrow(new NoSuchElementException());
					// default stubbing for view.showReservationError(reservation, message)
					// default stubbing for bookingService.findAllClients()
					// default stubbing for view.showAllClients(clients)
					// default stubbing for bookingService.findAllReservations()
					// default stubbing for view.showAllReservations(reservations)
					
					InOrder inOrder = Mockito.inOrder(bookingService, view, spiedServedBookingPresenter, ReservationValidator.class);
					
					assertThatNoException().isThrownBy(
							() -> spiedServedBookingPresenter.addReservation(A_DATE, A_CLIENT));
					
					inOrder.verify(mockedReservationValidator,
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, A_DATE));
					inOrder.verify(bookingService).insertNewReservation(A_RESERVATION);
					inOrder.verify(view).showReservationError(A_RESERVATION, "'s client has been deleted.");
					// updateAll
					inOrder.verify(spiedServedBookingPresenter).allReservations();
					inOrder.verify(spiedServedBookingPresenter).allClients();
				}

				@ParameterizedTest(name = "{index}: ''{0}''")
				@DisplayName("Date is not valid")
				@NullSource
				@ValueSource(strings = {"2O23-O4-24", "24-04-2023", "2022-13-12", "2022-08-32"})
				void testAddReservationWhenDateIsNotValidShouldShowErrorAndThrow(String invalidDate) {
					mockedReservationValidator.when(
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, invalidDate))
						.thenThrow(new IllegalArgumentException());
					
					InOrder inOrder = Mockito.inOrder(view, ReservationValidator.class);
					
					assertThatThrownBy(
							() -> servedBookingPresenter.addReservation(invalidDate, A_CLIENT))
						.isInstanceOf(IllegalArgumentException.class);
					
					inOrder.verify(mockedReservationValidator,
							() -> ReservationValidator.newValidatedReservation(A_CLIENT, invalidDate));
					inOrder.verify(view).showFormError("Date of reservation is not valid.");
					
					//verify(bookingService, never()).insertNewReservation(any());
					//verify(view, never()).reservationAdded(any());
					verifyNoMoreInteractions(view);
					verifyNoInteractions(bookingService);
				}
			}
		}
	}
}