package io.github.marcopaglio.booking.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;

@DisplayName("Integration tests for BookingSwingView and ServedBookingPresenter")
@RunWith(GUITestRunner.class)
public class ServedBookingPresenterSwingViewIT extends AssertJSwingJUnitTestCase {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final UUID A_CLIENT_UUID = UUID.fromString("3fea780e-def5-49ce-9257-3f0a66e958d0");
	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("466aa13c-625d-4b3e-bb31-f742675c4551");

	private static final String A_CLIENT_DISPLAYED = "Client [" + A_FIRSTNAME + " " + A_LASTNAME + "]";
	private static final String A_RESERVATION_DISPLAYED = "Reservation [" + A_DATE + "]";

	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);

	private static final String ANOTHER_CLIENT_DISPLAYED = "Client [" + ANOTHER_FIRSTNAME + " " + ANOTHER_LASTNAME + "]";
	private static final String ANOTHER_RESERVATION_DISPLAYED = "Reservation [" + ANOTHER_DATE + "]";

	private FrameFixture window;

	private JLabelFixture formErrorMsgLbl;
	private JLabelFixture operationErrorMsgLbl;
	private JListFixture clientList;
	private JListFixture reservationList;

	@Mock
	private BookingService bookingService;

	@Mock
	private ReservationValidator reservationValidator;

	@Mock
	private ClientValidator clientValidator;

	private ServedBookingPresenter servedBookingPresenter;
	private BookingSwingView bookingSwingView;

	private Client client;
	private Reservation reservation;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		GuiActionRunner.execute(() -> {
			bookingSwingView = new BookingSwingView();
			servedBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					bookingService, clientValidator, reservationValidator);
			
			bookingSwingView.setBookingPresenter(servedBookingPresenter);
			return bookingSwingView;
		});
		window = new FrameFixture(robot(), bookingSwingView);
		window.show();
		
		// error labels
		formErrorMsgLbl = window.label("formErrorMsgLbl");
		operationErrorMsgLbl = window.label("operationErrorMsgLbl");
		
		// lists
		clientList = window.list("clientList");
		reservationList = window.list("reservationList");
		
		// entities
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		client.setId(A_CLIENT_UUID);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		reservation.setId(A_RESERVATION_UUID);
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	////////////// Integration tests for 'allClients'
		@Test @GUITest
		@DisplayName("No clients in repository")
		public void testAllClientsWhenThereAreNoClientsInRepositoryShouldShowNothingInClientList() {
			// default stubbing for bookingService.findAllClients()
			
			servedBookingPresenter.allClients();
			
			assertThat(clientList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Several clients in repository")
		public void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldShowClientsInList() {
			when(bookingService.findAllClients())
				.thenReturn(Arrays.asList(client, new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME)));
			
			servedBookingPresenter.allClients();
			
			assertThat(clientList.contents())
				.containsExactlyInAnyOrder(A_CLIENT_DISPLAYED, ANOTHER_CLIENT_DISPLAYED);
		}
	////////////// Integration tests for 'allClients'


	////////////// Integration tests for 'allReservations'
		@Test @GUITest
		@DisplayName("No reservations in repository")
		public void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldShowNothingInReservationList() {
			// default stubbing for bookingService.findAllReservations()
			
			servedBookingPresenter.allReservations();
			
			assertThat(reservationList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Several reservations in repository")
		public void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldShowReservationsInList() {
			when(bookingService.findAllReservations()).thenReturn(
					Arrays.asList(reservation, new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE)));
			
			servedBookingPresenter.allReservations();
			
			assertThat(reservationList.contents()).containsExactlyInAnyOrder(
					A_RESERVATION_DISPLAYED, ANOTHER_RESERVATION_DISPLAYED);
		}
	////////////// Integration tests for 'allReservations'


	////////////// Integration tests for 'deleteClient'
		@Test @GUITest
		@DisplayName("Client is in repository")
		public void testDeleteClientWhenClientIsInRepositoryShouldUpdateLists() {
			addClientInList(client);
			addReservationInList(reservation);
			
			// default stubbing for bookingService.removeClientNamed(firstName, lastName)
			
			servedBookingPresenter.deleteClient(client);
			
			assertThat(reservationList.contents()).doesNotContain(A_RESERVATION_DISPLAYED);
			assertThat(clientList.contents()).doesNotContain(A_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Client is not in repository")
		public void testDeleteClientWhenClientIsNotInRepositoryShouldShowOperationErrorAndUpdateLists() {
			addClientInList(client);
			
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
			
			servedBookingPresenter.deleteClient(client);
			
			operationErrorMsgLbl.requireText(
					"Client named " + A_FIRSTNAME + " " + A_LASTNAME + " no longer exists.");
			assertThat(clientList.contents()).doesNotContain(A_CLIENT_DISPLAYED);
		}
	////////////// Integration tests for 'deleteClient'


	////////////// Integration tests for 'deleteReservation'
		@Test @GUITest
		@DisplayName("Reservation is in repository")
		public void testDeleteReservationWhenReservationIsInRepositoryShouldUpdateReservationList() {
			addReservationInList(reservation);
			
			// default stubbing for bookingService.removeReservationOn(date)
			
			servedBookingPresenter.deleteReservation(reservation);
			
			assertThat(reservationList.contents()).doesNotContain(A_RESERVATION_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Reservation is not in repository")
		public void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowOperationErrorAndUpdateLists() {
			addReservationInList(reservation);
			
			doThrow(new InstanceNotFoundException())
				.when(bookingService).removeReservationOn(A_LOCALDATE);
			
			servedBookingPresenter.deleteReservation(reservation);
			
			operationErrorMsgLbl.requireText("Reservation on " + A_DATE + " no longer exists.");
			assertThat(reservationList.contents()).doesNotContain(A_RESERVATION_DISPLAYED);
		}
	////////////// Integration tests for 'deleteReservation'


	////////////// Integration tests for 'addClient'
		@Test @GUITest
		@DisplayName("Client is new")
		public void testAddClientWhenClientIsNewShouldShowItInList() {
			when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
			when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
			when(bookingService.insertNewClient(client)).thenReturn(client);
			
			servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			assertThat(clientList.contents()).containsExactly(A_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Client is not new")
		public void testAddClientWhenClientIsNotNewShouldShowOperationErrorAndNotAddAgainInList() {
			when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
			when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
			when(bookingService.insertNewClient(client))
				.thenThrow(new InstanceAlreadyExistsException());
			when(bookingService.findAllClients()).thenReturn(Arrays.asList(client));
			
			servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			operationErrorMsgLbl.requireText(
					"Client named " + A_FIRSTNAME + " " + A_LASTNAME + " already exists.");
			assertThat(clientList.contents()).containsOnlyOnce(A_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Name is not valid")
		public void testAddClientWhenNameIsNotValidShouldShowFormErrorAndNotAddInList() {
			when(clientValidator.validateFirstName(A_FIRSTNAME))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			formErrorMsgLbl.requireText("Client's name [" + A_FIRSTNAME + "] is not valid.");
			assertThat(clientList.contents()).doesNotContain(A_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Surname is not valid")
		public void testAddClientWhenSurnameIsNotValidShouldShowFormErrorAndNotAddInList() {
			when(clientValidator.validateLastName(A_LASTNAME))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			formErrorMsgLbl.requireText("Client's surname [" + A_LASTNAME + "] is not valid.");
			assertThat(clientList.contents()).doesNotContain(A_CLIENT_DISPLAYED);
		}
	////////////// Integration tests for 'addClient'


	////////////// Integration tests for 'addReservation'
		@Test @GUITest
		@DisplayName("Reservation is new")
		public void testAddReservationWhenReservationIsNewShouldShowItInList() {
			when(reservationValidator.validateClientId(A_CLIENT_UUID)).thenReturn(A_CLIENT_UUID);
			when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
			when(bookingService.insertNewReservation(reservation)).thenReturn(reservation);
			
			servedBookingPresenter.addReservation(client, A_DATE);
			
			assertThat(reservationList.contents()).containsExactly(A_RESERVATION_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Reservation is not new")
		public void testAddReservationWhenReservationIsNotNewShouldShowOperationErrorAndNotAddAgainInList() {
			when(reservationValidator.validateClientId(A_CLIENT_UUID)).thenReturn(A_CLIENT_UUID);
			when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
			when(bookingService.insertNewReservation(reservation))
				.thenThrow(new InstanceAlreadyExistsException());
			when(bookingService.findAllReservations()).thenReturn(Arrays.asList(reservation));
			
			servedBookingPresenter.addReservation(client, A_DATE);
			
			operationErrorMsgLbl.requireText("Reservation on " + A_DATE + " already exists.");
			assertThat(reservationList.contents()).containsOnlyOnce(A_RESERVATION_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("ClientId is not valid")
		public void testAddReservationWhenClientIdIsNotValidShouldShowFormErrorAndNotAddInList() {
			when(reservationValidator.validateClientId(A_CLIENT_UUID))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.addReservation(client, A_DATE);
			
			formErrorMsgLbl.requireText("Reservation's client ID [" + A_CLIENT_UUID + "] is not valid.");
			assertThat(reservationList.contents()).doesNotContain(A_RESERVATION_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Date is not valid")
		public void testAddReservationWhenDateIsNotValidShouldShowFormErrorAndNotAddInList() {
			when(reservationValidator.validateDate(A_DATE))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.addReservation(client, A_DATE);
			
			formErrorMsgLbl.requireText("Reservation's date [" + A_DATE + "] is not valid.");
			assertThat(reservationList.contents()).doesNotContain(A_RESERVATION_DISPLAYED);
		}
	////////////// Integration tests for 'addReservation'


	////////////// Integration tests for 'renameClient'
		@Test @GUITest
		@DisplayName("Renamed client is new")
		public void testRenameClientWhenRenamedClientIsNewShouldUpdateClientInList() {
			addClientInList(client);
			
			when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME)).thenReturn(ANOTHER_FIRSTNAME);
			when(clientValidator.validateLastName(ANOTHER_LASTNAME)).thenReturn(ANOTHER_LASTNAME);
			when(bookingService.renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
				.thenReturn(new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
			
			servedBookingPresenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			assertThat(clientList.contents())
				.doesNotContain(A_CLIENT_DISPLAYED)
				.containsExactly(ANOTHER_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Renamed client is not new")
		public void testRenameClientWhenRenamedClientIsNotNewShouldShowOperationErrorAndNotRename() {
			Client another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			addClientInList(client);
			addClientInList(another_client);
			
			when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME)).thenReturn(ANOTHER_FIRSTNAME);
			when(clientValidator.validateLastName(ANOTHER_LASTNAME)).thenReturn(ANOTHER_LASTNAME);
			when(bookingService.renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
				.thenThrow(new InstanceAlreadyExistsException());
			when(bookingService.findAllClients()).thenReturn(Arrays.asList(client, another_client));
			
			servedBookingPresenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			operationErrorMsgLbl.requireText(
					"Client named " + ANOTHER_FIRSTNAME + " " + ANOTHER_LASTNAME + " already exists.");
			assertThat(clientList.contents()).containsOnlyOnceElementsOf(
					Arrays.asList(A_CLIENT_DISPLAYED, ANOTHER_CLIENT_DISPLAYED));
		}

		@Test @GUITest
		@DisplayName("New name is not valid")
		public void testRenameClientWhenNewNameIsNotValidShouldShowFormErrorAndNotRename() {
			when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			formErrorMsgLbl.requireText("Client's name [" + ANOTHER_FIRSTNAME + "] is not valid.");
			assertThat(clientList.contents()).doesNotContain(ANOTHER_CLIENT_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("New surname is not valid")
		public void testRenameClientWhenNewSurnameIsNotValidShouldShowFormErrorAndNotRename() {
			when(clientValidator.validateLastName(ANOTHER_LASTNAME))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			formErrorMsgLbl.requireText("Client's surname [" + ANOTHER_LASTNAME + "] is not valid.");
			assertThat(clientList.contents()).doesNotContain(ANOTHER_CLIENT_DISPLAYED);
		}
	////////////// Integration tests for 'renameClient'


	////////////// Integration tests for 'rescheduleReservation'
		@Test @GUITest
		@DisplayName("Rescheduled reservation is new")
		public void testRescheduleReservationWhenRescheduledReservationIsNewShouldUpdateReservationInList() {
			addReservationInList(reservation);
			
			when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
			when(bookingService.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
				.thenReturn(new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE));
			
			servedBookingPresenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			assertThat(reservationList.contents())
				.doesNotContain(A_RESERVATION_DISPLAYED)
				.containsExactly(ANOTHER_RESERVATION_DISPLAYED);
		}

		@Test @GUITest
		@DisplayName("Rescheduled reservation is not new")
		public void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowOperationErrorAndNotReschedule() {
			Reservation another_reservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
			
			addReservationInList(reservation);
			addReservationInList(another_reservation);
			
			when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
			when(bookingService.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
				.thenThrow(new InstanceAlreadyExistsException());
			when(bookingService.findAllReservations()).thenReturn(
					Arrays.asList(reservation, another_reservation));
			
			servedBookingPresenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			operationErrorMsgLbl.requireText("Reservation on " + ANOTHER_DATE + " already exists.");
			assertThat(reservationList.contents()).containsOnlyOnceElementsOf(
					Arrays.asList(A_RESERVATION_DISPLAYED, ANOTHER_RESERVATION_DISPLAYED));
		}

		@Test @GUITest
		@DisplayName("New date is not valid")
		public void testRescheduleReservationWhenNewDateIsNotValidShouldShowFormErrorAndNotReschedule() {
			when(reservationValidator.validateDate(ANOTHER_DATE))
				.thenThrow(new IllegalArgumentException());
			
			servedBookingPresenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			formErrorMsgLbl.requireText("Reservation's date [" + ANOTHER_DATE + "] is not valid.");
			assertThat(reservationList.contents()).doesNotContain(ANOTHER_RESERVATION_DISPLAYED);
		}
	////////////// Integration tests for 'rescheduleReservation'

	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
	}
}