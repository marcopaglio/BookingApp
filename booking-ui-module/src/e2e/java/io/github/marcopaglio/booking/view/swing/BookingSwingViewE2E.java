package io.github.marcopaglio.booking.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public abstract class BookingSwingViewE2E extends AssertJSwingJUnitTestCase {
	private static final int TIMEOUT = 5000;
	private static final int DEFAULT_NUM_OF_CLIENTS = 1;
	private static final int DEFAULT_NUM_OF_RESERVATIONS = 1;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("b8a88ad6-739e-4df5-b3ea-82832a56843a");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";

	private static final String INVALID_FIRSTNAME = "Mari4";
	private static final String INVALID_LASTNAME = "De_Lucia";

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final String A_DATE = A_YEAR + "-" + A_MONTH + "-" + A_DAY;
	private static final UUID A_RESERVATION_UUID = UUID.fromString("6de5f28a-541a-4699-8377-e0eaa9d2b13e");
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final String ANOTHER_DATE = ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY;

	private static final String INVALID_YEAR = "2O23";

	// regex
	private static final String NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX =
			".*" + A_FIRSTNAME + ".*" + A_LASTNAME + ".*" + "|" +
			".*" + A_LASTNAME + ".*" + A_FIRSTNAME + ".*";
	private static final String DATE_REGEX = ".*" + A_DATE + ".*";

	protected FrameFixture window;

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

	// pause conditions
	private Condition untilClientListContainsDifferentNumberOfClientsThanTheDefaultOnes = new Condition(
			"Client list to contain different number of clients than the default ones") {
		@Override
		public boolean test() {
			return clientList.contents().length != DEFAULT_NUM_OF_CLIENTS;
		}
	};

	private Condition untilNameFormsAreReset = new Condition("Name forms to be reset") {
		@Override
		public boolean test() {
			return nameFormTxt.text().isEmpty() && 
					surnameFormTxt.text().isEmpty();
		}
	};

	private Condition untilReservationListContainsDifferentNumberOfReservationThanTheDefaultOnes = new Condition(
			"Reservation list to contain different number of reservations than the default ones") {
		@Override
		public boolean test() {
			return reservationList.contents().length != DEFAULT_NUM_OF_RESERVATIONS;
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

	private Condition untilFormErrorContainsAMessage = new Condition("Form error to contain a message") {
		@Override
		public boolean test() {
			return !formErrorMsgLbl.text().isBlank();
		}
	};

	private Condition untilOperationErrorContainsAMessage = new Condition("Operation error to contain a message") {
		@Override
		public boolean test() {
			return !operationErrorMsgLbl.text().isBlank();
		}
	};

	@Override
	protected void onSetUp() throws Exception {
		// get a reference of its JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "BookingApp".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
		
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
	}

	protected void addTestEntitiesToDatabase() {
		addTestClientToDatabase(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);
		addTestReservationToDatabase(A_CLIENT_UUID, A_DATE, A_RESERVATION_UUID);
	}


	@Test @GUITest
	@DisplayName("Application is started")
	public void testApplicationWhenIsStartedShouldShowAllDatabaseElements() {
		assertThat(clientList.contents())
			.anySatisfy(e -> assertThat(e).contains(A_FIRSTNAME, A_LASTNAME));
		
		assertThat(reservationList.contents())
			.anySatisfy(e -> assertThat(e).contains(A_DATE));
	}

	////////////// Add client
	@Test @GUITest
	@DisplayName("Adding client succeeds")
	public void testAddClientWhenIsSuccessfulShouldShowTheNewClient() {
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilClientListContainsDifferentNumberOfClientsThanTheDefaultOnes, timeout(TIMEOUT));
		
		assertThat(clientList.contents())
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
	}

	@Test @GUITest
	@DisplayName("Name is invalid")
	public void testAddClientWhenNameIsInvalidShouldShowFormError() {
		nameFormTxt.enterText(INVALID_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_FIRSTNAME);
	}

	@Test @GUITest
	@DisplayName("Surname is invalid")
	public void testAddClientWhenSurnameIsInvalidShouldShowFormError() {
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(INVALID_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Adding client fails")
	public void testAddClientWhenIsAFailureShouldShowAnOperationError() {
		nameFormTxt.enterText(A_FIRSTNAME);
		surnameFormTxt.enterText(A_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_FIRSTNAME, A_LASTNAME);
	}
	////////////// Add client


	////////////// Rename client
	@Test @GUITest
	@DisplayName("Renaming client succeeds")
	public void testRenameClientWhenIsSuccessfulShouldShowChanges() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		renameBtn.click();
		
		pause(untilNameFormsAreReset, timeout(TIMEOUT));
		
		assertThat(clientList.contents())
			.allSatisfy(e -> assertThat(e).doesNotContain(A_FIRSTNAME, A_LASTNAME))
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
	}

	@Test @GUITest
	@DisplayName("New name is invalid")
	public void testRenameClientWhenNewNameIsInvalidShouldShowFormError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(INVALID_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		renameBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_FIRSTNAME);
	}

	@Test @GUITest
	@DisplayName("New surname is invalid")
	public void testRenameClientWhenNewSurnameIsInvalidShouldShowFormError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(INVALID_LASTNAME);
		
		renameBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Renamed client would be homonymic")
	public void testRenameClientWhenThereIsHomonymyShouldShowAnOperationError() {
		UUID anotherClientUUID = UUID.fromString("c5014376-5a67-4c17-a1da-0b0b92af5711");
		addTestClientToDatabase(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME, anotherClientUUID);
		
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		renameBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Renaming client fails")
	public void testRenameClientWhenIsAFailureShouldShowAnOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		removeTestClientFromDatabase(A_FIRSTNAME, A_LASTNAME);
		
		renameBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_FIRSTNAME, A_LASTNAME);
	}
	////////////// Rename client


	////////////// Remove client
	@Test @GUITest
	@DisplayName("Removing client succeeds")
	public void testRemoveClientWhenIsSuccessfulShouldMakeDisappearTheClient() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		
		removeClientBtn.click();
		
		pause(untilClientListContainsDifferentNumberOfClientsThanTheDefaultOnes, timeout(TIMEOUT));
		
		assertThat(clientList.contents())
			.allSatisfy(e -> assertThat(e).doesNotContain(A_FIRSTNAME, A_LASTNAME));
	}

	@Test @GUITest
	@DisplayName("Removing client fails")
	public void testRemoveClientWhenIsAFailureShouldShowOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		
		removeTestClientFromDatabase(A_FIRSTNAME, A_LASTNAME);
		
		removeClientBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_FIRSTNAME, A_LASTNAME);
	}
	////////////// Remove client


	////////////// Add reservation
	@Test @GUITest
	@DisplayName("Adding reservation succeeds")
	public void testAddReservationWhenIsSuccessfulShouldShowTheNewReservation() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		addReservationBtn.click();
		
		pause(untilReservationListContainsDifferentNumberOfReservationThanTheDefaultOnes, timeout(TIMEOUT));
		
		assertThat(reservationList.contents())
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_DATE));
	}

	@Test @GUITest
	@DisplayName("Date is invalid")
	public void testAddReservationWhenDateIsInvalidShouldShowFormError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(INVALID_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		addReservationBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_YEAR);
	}

	@Test @GUITest
	@DisplayName("Selected client is wrong")
	public void testAddReservationWhenSelectedClientIsWrongShouldShowOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		removeTestClientFromDatabase(A_FIRSTNAME, A_LASTNAME);
		
		addReservationBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_FIRSTNAME, A_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Adding reservation fails")
	public void testAddReservationWhenIsAFailureShouldShowAnOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(A_YEAR);
		monthFormTxt.enterText(A_MONTH);
		dayFormTxt.enterText(A_DAY);
		
		addReservationBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_DATE);
	}
	////////////// Add reservation


	////////////// Reschedule reservation
	@Test @GUITest
	@DisplayName("Rescheduling reservation succeeds")
	public void testRescheduleReservationWhenIsSuccessfulShouldShowChanges() {
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		rescheduleBtn.click();
		
		pause(untilDateFormsAreReset, timeout(TIMEOUT));
		
		assertThat(reservationList.contents())
			.allSatisfy(e -> assertThat(e).doesNotContain(A_DATE))
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_DATE));
	}

	@Test @GUITest
	@DisplayName("New date is invalid")
	public void testRescheduleReservationWhenNewDateIsInvalidShouldShowFormError() {
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		yearFormTxt.enterText(INVALID_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		rescheduleBtn.click();
		
		pause(untilFormErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_YEAR);
	}

	@Test @GUITest
	@DisplayName("Rescheduled reservation would be simultaneous")
	public void testRescheduleReservationWhenThereIsSimultaneityShouldShowAnOperationError() {
		UUID anotherReservationUUID = UUID.fromString("17f600a7-af1e-41d1-8dd0-f4d00dbda750");
		addTestReservationToDatabase(A_CLIENT_UUID, ANOTHER_DATE, anotherReservationUUID);
		
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		rescheduleBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(ANOTHER_DATE);
	}

	@Test @GUITest
	@DisplayName("Rescheduling reservation fails")
	public void testRescheduleReservationWhenIsAFailureShouldShowAnOperationError() {
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		removeTestReservationFromDatabase(A_DATE);
		
		rescheduleBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_DATE);
	}
	////////////// Reschedule reservation


	////////////// Remove reservation
	@Test @GUITest
	@DisplayName("Removing reservation succeeds")
	public void testRemoveReservationWhenIsSuccessfulShouldMakeDisappearTheReservation() {
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		
		removeReservationBtn.click();
		
		pause(untilReservationListContainsDifferentNumberOfReservationThanTheDefaultOnes, timeout(TIMEOUT));
		
		assertThat(reservationList.contents())
			.allSatisfy(e -> assertThat(e).doesNotContain(A_DATE));
	}

	@Test @GUITest
	@DisplayName("Removing reservation fails")
	public void testRemoveReservationWhenIsAFailureShouldShowOperationError() {
		reservationList.selectItem(Pattern.compile(DATE_REGEX));
		
		removeTestReservationFromDatabase(A_DATE);
		
		removeReservationBtn.click();
		
		pause(untilOperationErrorContainsAMessage, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_DATE);
	}
	////////////// Remove reservation


	// database modifiers
	protected abstract void addTestClientToDatabase(String name, String surname, UUID id);

	protected abstract void removeTestClientFromDatabase(String name, String surname);

	protected abstract void addTestReservationToDatabase(UUID clientId, String date, UUID id);

	protected abstract void removeTestReservationFromDatabase(String date);
}