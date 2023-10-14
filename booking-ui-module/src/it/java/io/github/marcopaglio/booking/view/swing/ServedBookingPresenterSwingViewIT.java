package io.github.marcopaglio.booking.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;

@DisplayName("Integration tests for BookingSwingView and ServedBookingPresenter")
@RunWith(GUITestRunner.class)
public class ServedBookingPresenterSwingViewIT extends AssertJSwingJUnitTestCase {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private FrameFixture window;

	private JTextComponentFixture nameFormTxt;
	private JTextComponentFixture surnameFormTxt;
	private JTextComponentFixture yearFormTxt;
	private JTextComponentFixture monthFormTxt;
	private JTextComponentFixture dayFormTxt;
	private JLabelFixture formErrorMsgLbl;
	private JLabelFixture operationErrorMsgLbl;
	private JButtonFixture addReservationBtn;
	private JButtonFixture addClientBtn;
	private JButtonFixture renameBtn;
	private JButtonFixture rescheduleBtn;
	private JButtonFixture removeClientBtn;
	private JButtonFixture removeReservationBtn;
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

	private Client client, another_client;

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
		
		// text fields
		nameFormTxt = window.textBox("nameFormTxt");
		surnameFormTxt = window.textBox("surnameFormTxt");
		yearFormTxt = window.textBox("yearFormTxt");
		monthFormTxt = window.textBox("monthFormTxt");
		dayFormTxt = window.textBox("dayFormTxt");
		
		// error labels
		formErrorMsgLbl = window.label("formErrorMsgLbl");
		operationErrorMsgLbl = window.label("operationErrorMsgLbl");
		
		// buttons
		addReservationBtn = window.button(JButtonMatcher.withText("Add Reservation"));
		addClientBtn = window.button(JButtonMatcher.withText("Add Client"));
		renameBtn = window.button(JButtonMatcher.withText("Rename"));
		rescheduleBtn = window.button(JButtonMatcher.withText("Reschedule"));
		removeClientBtn = window.button(JButtonMatcher.withText("Remove Client"));
		removeReservationBtn = window.button(JButtonMatcher.withText("Remove Reservation"));
		
		// lists
		clientList = window.list("clientList");
		reservationList = window.list("reservationList");
		
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
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
		List<Client> clients = Arrays.asList(client, another_client);
		
		when(bookingService.findAllClients()).thenReturn(clients);
		
		GuiActionRunner.execute(() -> servedBookingPresenter.allClients());
		
		verify(bookingService).findAllClients();
		assertThat(clientList.contents())
			.containsExactlyInAnyOrder(client.toString(), another_client.toString());
	}
}
