package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.view.View;

@DisplayName("Tests for ServedBookingPresenter class")
@ExtendWith(MockitoExtension.class)
class ServedBookingPresenterTest {

	@Mock
	private BookingService bookingService;

	@Mock
	private View view;

	@InjectMocks
	private ServedBookingPresenter servedBookingPresenter;

	@DisplayName("Tests for 'allClients'")
	@Nested
	class AllClientsTest {
		private final Client a_client = new Client("Mario", "Rossi", new ArrayList<>());

		@DisplayName("No clients in repository")
		@Test
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallTheViewWithEmptyList() {
			// default stubbing for bookingService.findAllClients()
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@DisplayName("Single client in repository")
		@Test
		void testAllClientsWhenThereIsASingleClientInRepositoryShouldCallTheViewWithTheClientAsList() {
			List<Client> clients = Arrays.asList(a_client);
			when(bookingService.findAllClients()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(clients);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@DisplayName("Several clients in repository")
		@Test
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallTheViewWithClientsAsList() {
			Client another_client = new Client("Maria", "De Lucia", new ArrayList<>());
			List<Client> clients = Arrays.asList(a_client, another_client);
			when(bookingService.findAllClients()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allClients());
			
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(clients);
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@DisplayName("Tests for 'allReservations'")
	@Nested 
	class AllReservationsTest {
		private final Client a_client = new Client("Mario", "Rossi", new ArrayList<>());
		private final Reservation a_reservation = new Reservation(a_client, "2023-04-24");

		@DisplayName("No reservations in repository")
		@Test
		void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldCallTheViewWithEmptyList() {
			// default stubbing for bookingService.findAllReservations()
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(new ArrayList<>());
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@DisplayName("Single reservation in repository")
		@Test
		void testAllReservationsWhenThereIsASingleReservationInRepositoryShouldCallTheViewWithTheReservationAsList() {
			List<Reservation> reservations = Arrays.asList(a_reservation);
			when(bookingService.findAllReservations()).thenReturn(reservations);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(reservations);
			
			verifyNoMoreInteractions(bookingService, view);
		}

		@DisplayName("Several reservations in repository")
		@Test
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallTheViewWithReservationsAsList() {
			Reservation another_reservation = new Reservation(a_client, "2023-09-05");
			List<Reservation> reservations = Arrays.asList(a_reservation, another_reservation);
			when(bookingService.findAllReservations()).thenReturn(reservations);
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(() -> servedBookingPresenter.allReservations());
			
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(reservations);
			
			verifyNoMoreInteractions(bookingService, view);
		}
	}

	@DisplayName("Tests for 'deleteReservation'")
	@Nested
	class DeleteReservationTest {

		@DisplayName("Null reservation")
		@Test
		void testDeleteReservationWhenDateIsNullShouldThrow() {
			assertThatThrownBy(() -> servedBookingPresenter.deleteReservation(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to delete cannot be null.");
			
			verifyNoInteractions(bookingService, view);
		}

		@DisplayName("Reservation is in repository")
		@Test
		void testDeleteReservationWhenReservationIsInRepositoryShouldDelegateToServiceAndNotifyTheView() {
			String date = "2023-04-24";
			LocalDate localDate = LocalDate.parse(date);
			Client a_client = new Client("Mario", "Rossi", new ArrayList<>());
			Reservation a_reservation = new Reservation(a_client, date);
			//Reservation a_reservation = new Reservation(UUID.randomUUID(), localDate);
			
			// default stubbing for bookingService.removeReservationOn(date)
			// default stubbing for view.reservationRemoved(reservation)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(a_reservation));
			
			inOrder.verify(bookingService).removeReservationOn(localDate);
			inOrder.verify(view).reservationRemoved(a_reservation);
			
			verifyNoMoreInteractions(bookingService, view);
		}
	
		@DisplayName("Reservation is not in repository")
		@Test
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldNotifyTheView() {
			String date = "2023-04-24";
			LocalDate localDate = LocalDate.parse(date);
			Client a_client = new Client("Mario", "Rossi", new ArrayList<>());
			Reservation a_reservation = new Reservation(a_client, date);
			//Reservation a_reservation = new Reservation(UUID.randomUUID(), localDate);
			
			doThrow(new NoSuchElementException()).when(bookingService).removeReservationOn(localDate);
			// default stubbing for view.showReservationError(reservation, message)
			// default stubbing for view.reservationRemoved(reservation)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteReservation(a_reservation));
			
			inOrder.verify(bookingService).removeReservationOn(localDate);
			inOrder.verify(view).showReservationError(a_reservation, " has already been eliminated.");
			inOrder.verify(view).reservationRemoved(a_reservation);
			
			verifyNoMoreInteractions(bookingService, view);
		}
		// TODO: test eccezioni lanciate da view.showReservationError e view.reservationRemoved
	}

	@DisplayName("Tests for 'deleteClient'")
	@Nested
	class DeleteClientTest {

		@DisplayName("Null client")
		@Test
		void testDeleteClientWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> servedBookingPresenter.deleteClient(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to delete cannot be null.");
		
			verifyNoInteractions(bookingService, view);
		}

		@DisplayName("Client is in repository")
		@Test
		void testDeleteClientWhenClientIsInRepositoryShouldDelegateToServiceAndNotifyTheView() {
			String a_firstName = "Mario";
			String a_lastName = "Rossi";
			Client a_client = new Client(a_firstName, a_lastName, new ArrayList<>());
			
			// default stubbing for bookingService.removeClientNamed(firstName, lastName)
			// default stubbing for view.clientRemoved(client)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(a_client));
			
			inOrder.verify(bookingService).removeClientNamed(a_firstName, a_lastName);
			inOrder.verify(view).clientRemoved(a_client);
		}

		@DisplayName("Client is not in repository")
		@Test
		void testDeleteClientWhenClientIsNotInRepositoryShouldNotifyTheView() {
			String a_firstName = "Mario";
			String a_lastName = "Rossi";
			Client a_client = new Client(a_firstName, a_lastName, new ArrayList<>());
			
			doThrow(new NoSuchElementException())
				.when(bookingService).removeClientNamed(a_firstName, a_lastName);
			// default stubbing for view.showClientError(client, message)
			// default stubbing for view.clientRemoved(client)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.deleteClient(a_client));
			
			inOrder.verify(bookingService).removeClientNamed(a_firstName, a_lastName);
			inOrder.verify(view).showClientError(a_client, " has already been eliminated.");
			inOrder.verify(view).clientRemoved(a_client);
		}
		// TODO: test eccezioni lanciate da view.showClientError e view.clientRemoved
	}

	@DisplayName("Tests for 'addReservation'")
	@Nested
	class AddReservationTest {

		@DisplayName("Null inputs")
		@Nested
		class NullInputsTest {

			@DisplayName("Null date")
			@Test
			void testAddReservationWhenDateIsNullShouldThrow() {
				String a_firstName = "Mario";
				String a_lastName = "Rossi";
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addReservation(null, a_firstName, a_lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Date of reservation to add cannot be null.");
		
				verifyNoInteractions(bookingService, view);
			}

			@DisplayName("Null names")
			@Test
			void testAddReservationWhenNameIsNullShouldThrow() {
				String date = "2023-04-24";
				String a_firstName = "Mario";
				String a_lastName = "Rossi";
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addReservation(date, null, a_lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's name of reservation to add cannot be null.");
				
				assertThatThrownBy(
						() -> servedBookingPresenter.addReservation(date, a_firstName, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's surname of reservation to add cannot be null.");
				
				verifyNoInteractions(bookingService, view);
			}
		}

		@DisplayName("Client is not new and reservation is new and parameters are valid")
		@Test
		void testAddReservationWhenClientIsNotNewAndParametersAreValidShouldDelegateAndNotify() {
			String date = "2023-04-24";
			String a_firstName = "Mario";
			String a_lastName = "Rossi";
			Client a_client = new Client(a_firstName, a_lastName, new ArrayList<>());
			Reservation a_reservation = new Reservation(a_client, date);
			
			when(bookingService.findClientNamed(a_firstName, a_lastName)).thenReturn(a_client);
			when(bookingService.insertNewReservation(a_reservation)).thenReturn(a_reservation);
			// default stubbing for view.reservationAdded(reservation)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.addReservation(date, a_firstName, a_lastName));
			
			inOrder.verify(bookingService).findClientNamed(a_firstName, a_lastName);
			inOrder.verify(bookingService).insertNewReservation(a_reservation);
			inOrder.verify(view).reservationAdded(a_reservation);
		}

		@DisplayName("Reservation and client are new and parameters are valid")
		@Test
		void testAddReservationWhenReservationAndClientAreNewAndParametersAreValidShouldCreateDelegateAndNotify() {
			String date = "2023-04-24";
			String a_firstName = "Mario";
			String a_lastName = "Rossi";
			Client a_client = new Client(a_firstName, a_lastName, new ArrayList<>());
			Reservation a_reservation = new Reservation(a_client, date);
			
			when(bookingService.findClientNamed(a_firstName, a_lastName))
				.thenThrow(new NoSuchElementException());
			when(bookingService.insertNewClient(a_client)).thenReturn(a_client);
			// default stubbing for view.clientAdded(client)
			when(bookingService.insertNewReservation(a_reservation)).thenReturn(a_reservation);
			// default stubbing for view.reservationAdded(reservation)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.addReservation(date, a_firstName, a_lastName));
			
			inOrder.verify(bookingService).findClientNamed(a_firstName, a_lastName);
			inOrder.verify(bookingService).insertNewClient(a_client);
			inOrder.verify(view).clientAdded(a_client);
			inOrder.verify(bookingService).insertNewReservation(a_reservation);
			inOrder.verify(view).reservationAdded(a_reservation);
		}

		@DisplayName("Reservation is not new")
		@Test
		void testNewReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
			String date = "2023-04-24";
			String a_firstName = "Mario";
			String a_lastName = "Rossi";
			Client a_client = new Client(a_firstName, a_lastName, new ArrayList<>());
			Reservation a_reservation = new Reservation(a_client, date);
			
			when(bookingService.findClientNamed(a_firstName, a_lastName)).thenReturn(a_client);
			when(bookingService.insertNewReservation(a_reservation))
				.thenThrow(new InstanceAlreadyExistsException());
			// default stubbing for view.showReservationError(reservation, message)
			// default stubbing for bookingService.findAllClients()
			// default stubbing for view.showAllClients(clients)
			// default stubbing for bookingService.findAllReservations()
			// default stubbing for view.showAllReservations(reservations)
			
			InOrder inOrder = Mockito.inOrder(bookingService, view);
			
			assertThatNoException().isThrownBy(
					() -> servedBookingPresenter.addReservation(date, a_firstName, a_lastName));
			
			inOrder.verify(bookingService).findClientNamed(a_firstName, a_lastName);
			inOrder.verify(bookingService).insertNewReservation(a_reservation);
			inOrder.verify(view).showReservationError(a_reservation, " has already been booked.");
			inOrder.verify(bookingService).findAllClients();
			inOrder.verify(view).showAllClients(ArgumentMatchers.<List<Client>>any());
			inOrder.verify(bookingService).findAllReservations();
			inOrder.verify(view).showAllReservations(ArgumentMatchers.<List<Reservation>>any());
		}

		// TODO: tests with validation

	}
}
