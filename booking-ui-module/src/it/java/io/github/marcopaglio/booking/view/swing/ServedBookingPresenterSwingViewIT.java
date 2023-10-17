package io.github.marcopaglio.booking.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);

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
		
		GuiActionRunner.execute(() -> servedBookingPresenter.allClients());
		
		verify(bookingService).findAllClients();
		assertThat(clientList.contents()).isEmpty();
	}

	@Test @GUITest
	@DisplayName("Several clients in repository")
	public void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldShowClientsInClientList() {
		Client anotherClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		
		when(bookingService.findAllClients())
			.thenReturn(Arrays.asList(client, anotherClient));
		
		GuiActionRunner.execute(() -> servedBookingPresenter.allClients());
		
		verify(bookingService).findAllClients();
		assertThat(clientList.contents())
			.containsExactlyInAnyOrder(client.toString(), anotherClient.toString());
	}
	////////////// Integration tests for 'allClients'


	////////////// Integration tests for 'allReservations'
	@Test @GUITest
	@DisplayName("No reservations in repository")
	public void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldShowNothingInReservationList() {
		// default stubbing for bookingService.findAllReservations()
		
		GuiActionRunner.execute(() -> servedBookingPresenter.allReservations());
		
		verify(bookingService).findAllReservations();
		assertThat(reservationList.contents()).isEmpty();
	}

	@Test @GUITest
	@DisplayName("Several reservations in repository")
	public void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldShowReservationsInReservationList() {
		Reservation anotherReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
		
		when(bookingService.findAllReservations())
			.thenReturn(Arrays.asList(reservation, anotherReservation));
		
		GuiActionRunner.execute(() -> servedBookingPresenter.allReservations());
		
		verify(bookingService).findAllReservations();
		assertThat(reservationList.contents()).containsExactlyInAnyOrder(
				reservation.toString(), anotherReservation.toString());
	}
	////////////// Integration tests for 'allReservations'


	////////////// Integration tests for 'deleteClient'
	@Test @GUITest
	@DisplayName("Client is in repository")
	public void testDeleteClientWhenClientIsInRepositoryShouldUpdateLists() {
		addClientInList(client);
		addReservationInList(reservation);
		
		GuiActionRunner.execute(() -> servedBookingPresenter.deleteClient(client));
		
		verify(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
		assertThat(reservationList.contents())
			.doesNotContain(reservation.toString())
			.isEmpty();
		assertThat(clientList.contents())
			.doesNotContain(client.toString())
			.isEmpty();
	}

	@Test @GUITest
	@DisplayName("Client is not in repository")
	public void testDeleteClientWhenClientIsNotInRepositoryShouldShowOperationErrorAndUpdateLists() {
		addClientInList(client);
		
		doThrow(new InstanceNotFoundException())
			.when(bookingService).removeClientNamed(A_FIRSTNAME, A_LASTNAME);
		
		GuiActionRunner.execute(() -> servedBookingPresenter.deleteClient(client));
		
		operationErrorMsgLbl.requireText(client.toString() + " no longer exists.");
		assertThat(clientList.contents()).isEmpty();
	}
	////////////// Integration tests for 'deleteClient'


	////////////// Integration tests for 'deleteReservation'
	@Test @GUITest
	@DisplayName("Reservation is in repository")
	public void testDeleteReservationWhenReservationIsInRepositoryShouldDelegateToServiceAndUpdateReservationList() {
		addReservationInList(reservation);
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.deleteReservation(reservation));
		
		verify(bookingService).removeReservationOn(A_LOCALDATE);
		assertThat(reservationList.contents())
			.doesNotContain(reservation.toString())
			.isEmpty();
	}

	@Test @GUITest
	@DisplayName("Reservation is not in repository")
	public void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowOperationErrorAndUpdateLists() {
		addReservationInList(reservation);
		
		doThrow(new InstanceNotFoundException())
			.when(bookingService).removeReservationOn(A_LOCALDATE);
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.deleteReservation(reservation));
		
		operationErrorMsgLbl.requireText(reservation.toString() + " no longer exists.");
		assertThat(reservationList.contents()).isEmpty();
	}
	////////////// Integration tests for 'deleteReservation'


	////////////// Integration tests for 'addClient'
	@Test @GUITest
	@DisplayName("Client is new")
	public void testAddClientWhenClientIsNewShouldValidateItAndDelegateToServiceAndShowItInClientList() {
		when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
		when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
		when(bookingService.insertNewClient(client)).thenReturn(client);
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
		
		verify(clientValidator).validateFirstName(A_FIRSTNAME);
		verify(clientValidator).validateLastName(A_LASTNAME);
		verify(bookingService).insertNewClient(client);
		assertThat(clientList.contents()).containsExactly(client.toString());
	}

	@Test @GUITest
	@DisplayName("Client is not new")
	public void testAddClientWhenClientIsNotNewShouldShowOperationErrorAndUpdateLists() {
		when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
		when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
		when(bookingService.insertNewClient(client))
			.thenThrow(new InstanceAlreadyExistsException());
		when(bookingService.findAllClients()).thenReturn(Arrays.asList(client));
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
		
		operationErrorMsgLbl.requireText("A client named " + A_FIRSTNAME
				+ " " + A_LASTNAME + " has already been made.");
		assertThat(clientList.contents()).containsExactly(client.toString());
	}

	@Test @GUITest
	@DisplayName("Name is not valid")
	public void testAddClientWhenNameIsNotValidShouldShowFormError() {
		when(clientValidator.validateFirstName(A_FIRSTNAME))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
		
		formErrorMsgLbl.requireText("Client's name is not valid.");
	}

	@Test @GUITest
	@DisplayName("Surname is not valid")
	public void testAddClientWhenSurnameIsNotValidShouldShowFormError() {
		when(clientValidator.validateLastName(A_LASTNAME))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addClient(A_FIRSTNAME, A_LASTNAME));
		
		formErrorMsgLbl.requireText("Client's surname is not valid.");
	}
	////////////// Integration tests for 'addClient'


	////////////// Integration tests for 'addReservation'
	@Test @GUITest
	@DisplayName("Reservation is new")
	public void testAddReservationWhenReservationIsNewShouldValidateItAndDelegateToServiceAndShowItInReservationList() {
		when(reservationValidator.validateClientId(A_CLIENT_UUID)).thenReturn(A_CLIENT_UUID);
		when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
		when(bookingService.insertNewReservation(reservation)).thenReturn(reservation);
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addReservation(client, A_DATE));
		
		verify(reservationValidator).validateClientId(A_CLIENT_UUID);
		verify(reservationValidator).validateDate(A_DATE);
		verify(bookingService).insertNewReservation(reservation);
		assertThat(reservationList.contents()).containsExactly(reservation.toString());
	}

	@Test @GUITest
	@DisplayName("Reservation is not new")
	public void testAddReservationWhenReservationIsNotNewShouldShowOperationErrorAndUpdateLists() {
		when(reservationValidator.validateClientId(A_CLIENT_UUID)).thenReturn(A_CLIENT_UUID);
		when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
		when(bookingService.insertNewReservation(reservation))
			.thenThrow(new InstanceAlreadyExistsException());
		when(bookingService.findAllReservations())
			.thenReturn(Arrays.asList(reservation));
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addReservation(client, A_DATE));
		
		operationErrorMsgLbl.requireText(
				"A reservation on " + A_DATE + " has already been made.");
		assertThat(reservationList.contents()).containsExactly(reservation.toString());
	}

	@Test @GUITest
	@DisplayName("ClientId is not valid")
	public void testAddReservationWhenClientIdIsNotValidShouldShowFormError() {
		when(reservationValidator.validateClientId(A_CLIENT_UUID))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addReservation(client, A_DATE));
		
		formErrorMsgLbl.requireText(
				"Client's identifier associated with reservation is not valid.");
	}

	@Test @GUITest
	@DisplayName("Date is not valid")
	public void testAddReservationWhenDateIsNotValidShouldShowFormError() {
		when(reservationValidator.validateDate(A_DATE))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(
				() -> servedBookingPresenter.addReservation(client, A_DATE));
		
		formErrorMsgLbl.requireText("Reservation's date is not valid.");
	}
	////////////// Integration tests for 'addReservation'


	////////////// Integration tests for 'renameClient'
	@Test @GUITest
	@DisplayName("Renamed client is new")
	public void testRenameClientWhenRenamedClientIsNewShouldValidateItAndDelegateToServiceAndUpdateClientList() {
		addClientInList(client);
		Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		
		when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME)).thenReturn(ANOTHER_FIRSTNAME);
		when(clientValidator.validateLastName(ANOTHER_LASTNAME)).thenReturn(ANOTHER_LASTNAME);
		when(bookingService.renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
			.thenReturn(renamedClient);
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
		
		verify(clientValidator).validateFirstName(ANOTHER_FIRSTNAME);
		verify(clientValidator).validateLastName(ANOTHER_LASTNAME);
		verify(bookingService).renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		assertThat(clientList.contents())
			.doesNotContain(client.toString())
			.containsOnlyOnce(renamedClient.toString());
	}

	@Test @GUITest
	@DisplayName("Renamed client is not new")
	public void testRenameClientWhenRenamedClientIsNotNewShouldShowOperationErrorAndUpdateLists() {
		addClientInList(client);
		Client hiddenClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		
		when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME)).thenReturn(ANOTHER_FIRSTNAME);
		when(clientValidator.validateLastName(ANOTHER_LASTNAME)).thenReturn(ANOTHER_LASTNAME);
		when(bookingService.renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
			.thenThrow(new InstanceAlreadyExistsException());
		when(bookingService.findAllClients())
			.thenReturn(Arrays.asList(client, hiddenClient));
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
		
		operationErrorMsgLbl.requireText("A client named " + ANOTHER_FIRSTNAME
				+ " " + ANOTHER_LASTNAME + " has already been made.");
		assertThat(clientList.contents())
			.containsExactlyInAnyOrder(client.toString(), hiddenClient.toString());
	}

	@Test @GUITest
	@DisplayName("New name is not valid")
	public void testRenameClientWhenNewNameIsNotValidShouldShowFormError() {
		when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
		
		formErrorMsgLbl.requireText("Client's name is not valid.");
	}

	@Test @GUITest
	@DisplayName("New surname is not valid")
	public void testRenameClientWhenNewSurnameIsNotValidShouldShowFormError() {
		when(clientValidator.validateLastName(ANOTHER_LASTNAME))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
		
		formErrorMsgLbl.requireText("Client's surname is not valid.");
	}
	////////////// Integration tests for 'renameClient'


	////////////// Integration tests for 'rescheduleReservation'
	@Test @GUITest
	@DisplayName("Rescheduled reservation is new")
	public void testRescheduleReservationWhenRescheduledReservationIsNewShouldValidateItAndDelegateToServiceAndUpdateReservationList() {
		addReservationInList(reservation);
		Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
		
		when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
		when(bookingService.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
			.thenReturn(rescheduledReservation);
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.rescheduleReservation(reservation, ANOTHER_DATE));
		
		verify(reservationValidator).validateDate(ANOTHER_DATE);
		verify(bookingService).rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE);
		assertThat(reservationList.contents())
			.doesNotContain(reservation.toString())
			.containsOnlyOnce(rescheduledReservation.toString());
	}

	@Test @GUITest
	@DisplayName("Rescheduled reservation is not new")
	public void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowOperationErrorAndUpdateLists() {
		addReservationInList(reservation);
		Reservation hiddenReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);

		
		when(reservationValidator.validateDate(ANOTHER_DATE)).thenReturn(ANOTHER_LOCALDATE);
		when(bookingService.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
			.thenThrow(new InstanceAlreadyExistsException());
		when(bookingService.findAllReservations())
			.thenReturn(Arrays.asList(reservation, hiddenReservation));
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.rescheduleReservation(reservation, ANOTHER_DATE));
		
		operationErrorMsgLbl.requireText(
				"A reservation on " + ANOTHER_LOCALDATE + " has already been made.");
		assertThat(reservationList.contents())
			.containsExactlyInAnyOrder(reservation.toString(), hiddenReservation.toString());
	}

	@Test @GUITest
	@DisplayName("New date is not valid")
	public void testRescheduleReservationWhenNewDateIsNotValidShouldShowFormError() {
		when(reservationValidator.validateDate(ANOTHER_DATE))
			.thenThrow(new IllegalArgumentException());
		
		GuiActionRunner.execute(() -> servedBookingPresenter
				.rescheduleReservation(reservation, ANOTHER_DATE));
		
		formErrorMsgLbl.requireText("Reservation's date is not valid.");
	}
	////////////// Integration tests for 'rescheduleReservation'


	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
	}
}