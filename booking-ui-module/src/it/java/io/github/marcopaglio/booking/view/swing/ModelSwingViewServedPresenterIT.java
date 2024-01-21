package io.github.marcopaglio.booking.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;

public abstract class ModelSwingViewServedPresenterIT extends AssertJSwingJUnitTestCase {
	private static final long TIMEOUT = 5000;
	private static final String LASTNAME_FIELD = "lastName";
	private static final String FIRSTNAME_FIELD = "firstName";

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY);

	protected TransactionalBookingService transactionalBookingService;
	private ServedBookingPresenter servedBookingPresenter;

	protected FrameFixture window;

	private JTextComponentFixture nameFormTxt;
	private JTextComponentFixture surnameFormTxt;
	private JTextComponentFixture yearFormTxt;
	private JTextComponentFixture monthFormTxt;
	private JTextComponentFixture dayFormTxt;
	private JButtonFixture addReservationBtn;
	private JButtonFixture addClientBtn;
	private JButtonFixture renameBtn;
	private JButtonFixture rescheduleBtn;
	private JButtonFixture removeClientBtn;
	private JButtonFixture removeReservationBtn;
	private JListFixture clientList;
	private JListFixture reservationList;

	private Client client;

	// pause conditions
	private Condition untilClientListContainsClients = new Condition("Client list to contain clients") {
		@Override
		public boolean test() {
			return clientList.contents().length != 0;
		}
	};

	private Condition untilClientListContainsNothing = new Condition("Client list to contain nothing") {
		@Override
		public boolean test() {
			return clientList.contents().length == 0;
		}
	};

	private Condition untilNameFormsAreReset = new Condition("Name forms to be reset") {
		@Override
		public boolean test() {
			return nameFormTxt.text().isEmpty() && 
					surnameFormTxt.text().isEmpty();
		}
	};

	private Condition untilReservationListContainsReservations = new Condition("Reservation list to contain reservations") {
		@Override
		public boolean test() {
			return reservationList.contents().length != 0;
		}
	};

	private Condition untilReservationListContainsNothing = new Condition("Reservation list to contain nothing") {
		@Override
		public boolean test() {
			return reservationList.contents().length == 0;
		}
	};

	private Condition untilDateFormsAreReset = new Condition("Date forms to be reset") {
		@Override
		public boolean test() {
			return yearFormTxt.text().isEmpty() && 
					monthFormTxt.text().isEmpty() && 
					dayFormTxt.text().isEmpty();
		}
	};

	@Override
	protected void onSetUp() throws Exception {
		RestrictedClientValidator restrictedClientValidator = new RestrictedClientValidator();
		RestrictedReservationValidator restrictedReservationValidator = new RestrictedReservationValidator();
		
		// make sure we always start with a clean database
		cleanDatabase();
		
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			BookingSwingView bookingSwingView = new BookingSwingView();
			servedBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					transactionalBookingService, restrictedClientValidator,
					restrictedReservationValidator);
			
			bookingSwingView.setBookingPresenter(servedBookingPresenter);
			return bookingSwingView;
		}));
		window.show();
		
		// text fields
		nameFormTxt = window.textBox("nameFormTxt");
		surnameFormTxt = window.textBox("surnameFormTxt");
		yearFormTxt = window.textBox("yearFormTxt");
		monthFormTxt = window.textBox("monthFormTxt");
		dayFormTxt = window.textBox("dayFormTxt");
		
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
		
		// entities
		client = new Client(A_FIRSTNAME, A_LASTNAME);
	}

	@Test
	@DisplayName("Integration tests for 'AddClientBtn'")
	public void testAddClientBtnWhenClientIsNewShouldInsert() {
		nameFormTxt.enterText(A_FIRSTNAME);
		surnameFormTxt.enterText(A_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilClientListContainsClients, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}

	@Test
	@DisplayName("Integration tests for 'RenameBtn'")
	public void testRenameBtnWhenThereIsNoClientWithTheSameNewNamesShouldRename() {
		String anotherFirstName = "Maria";
		String anotherLastName = "De Lucia";
		
		addTestClientToDatabase(client);
		updateClientList();
		
		clientList.selectItem(0);
		nameFormTxt.enterText(anotherFirstName);
		surnameFormTxt.enterText(anotherLastName);
		renameBtn.click();
		
		pause(untilNameFormsAreReset, timeout(TIMEOUT));
		
		assertThat(transactionalBookingService.findClient(client.getId()))
			.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, anotherFirstName)
			.hasFieldOrPropertyWithValue(LASTNAME_FIELD, anotherLastName);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveClientBtn'")
	public void testRemoveClientBtnWhenClientExistsWithAnExistingReservationShouldRemove() {
		addTestClientToDatabase(client);
		updateClientList();
		Reservation reservation = new Reservation(client.getId(), A_LOCALDATE);
		addTestReservationToDatabase(reservation);
		updateReservationList();
		
		clientList.selectItem(0);
		removeClientBtn.click();
		
		pause(untilClientListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
		assertThat(readAllReservationsFromDatabase())
			.filteredOn(r -> Objects.equals(r.getClientId(), client.getId())).isEmpty();
	}

	@Test
	@DisplayName("Integration tests for 'AddReservationBtn'")
	public void testAddReservationBtnWhenReservationIsNewAndAssociatedClientExistsShouldInsert() {
		addTestClientToDatabase(client);
		updateClientList();
		
		clientList.selectItem(0);
		yearFormTxt.enterText(A_YEAR);
		monthFormTxt.enterText(A_MONTH);
		dayFormTxt.enterText(A_DAY);
		addReservationBtn.click();
		
		pause(untilReservationListContainsReservations, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase())
			.containsExactly(new Reservation(client.getId(), A_LOCALDATE));
	}

	@Test
	@DisplayName("Integration tests for 'RescheduleBtn'")
	public void testRescheduleBtnWhenThereIsNoReservationInTheSameNewDateShouldReschedule() {
		String anotherYear = "2023";
		String anotherMonth = "09";
		String anotherDay = "05";
		LocalDate anotherLocalDate = LocalDate.parse(anotherYear + "-" + anotherMonth + "-" + anotherDay);
		
		addTestClientToDatabase(client);
		updateClientList();
		Reservation reservation = new Reservation(client.getId(), A_LOCALDATE);
		addTestReservationToDatabase(reservation);
		updateReservationList();
		
		reservationList.selectItem(0);
		yearFormTxt.enterText(anotherYear);
		monthFormTxt.enterText(anotherMonth);
		dayFormTxt.enterText(anotherDay);
		rescheduleBtn.click();
		
		pause(untilDateFormsAreReset, timeout(TIMEOUT));
		
		assertThat(transactionalBookingService.findReservation(reservation.getId()))
			.extracting(Reservation::getDate).isEqualTo(anotherLocalDate);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveReservationBtn'")
	public void testRemoveReservationBtnWhenReservationExistsShouldRemove() {
		addTestClientToDatabase(client);
		updateClientList();
		Reservation reservation = new Reservation(client.getId(), A_LOCALDATE);
		addTestReservationToDatabase(reservation);
		updateReservationList();
		
		reservationList.selectItem(0);
		removeReservationBtn.click();
		
		pause(untilReservationListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
	}


	private void updateClientList() {
		GuiActionRunner.execute(() -> servedBookingPresenter.allClients());
	}

	private void updateReservationList() {
		GuiActionRunner.execute(() -> servedBookingPresenter.allReservations());
	}

	private void cleanDatabase() {
		for (Reservation reservation : transactionalBookingService.findAllReservations())
			transactionalBookingService.removeReservation(reservation.getId());
		for (Client client : transactionalBookingService.findAllClients())
			transactionalBookingService.removeClient(client.getId());
	}

	private void addTestClientToDatabase(Client client) {
		transactionalBookingService.insertNewClient(client);
	}

	private void addTestReservationToDatabase(Reservation reservation) {
		transactionalBookingService.insertNewReservation(reservation);
	}

	private List<Client> readAllClientsFromDatabase() {
		return transactionalBookingService.findAllClients();
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		return transactionalBookingService.findAllReservations();
	}
}