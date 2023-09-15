package io.github.marcopaglio.booking.view.swing;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.BookingPresenter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.swing.JButton;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

@DisplayName("Tests for BookingSwingView class")
@RunWith(GUITestRunner.class)
public class BookingSwingViewTest extends AssertJSwingJUnitTestCase {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final Client ANOTHER_CLIENT = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final Reservation A_RESERVATION = new Reservation(
			UUID.fromString("9686dee5-1675-48ee-b5c3-81f164a9cf04"),
			LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY));
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final Reservation ANOTHER_RESERVATION = new Reservation(
			UUID.fromString("54c5e7b8-0810-4026-b0b2-20cd814008b3"),
			LocalDate.parse(ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY));

	private FrameFixture window;

	private JTextComponentFixture nameFormTxt;
	private JTextComponentFixture surnameFormTxt;
	private JTextComponentFixture yearFormTxt;
	private JTextComponentFixture monthFormTxt;
	private JTextComponentFixture dayFormTxt;
	private JLabelFixture formErrorMsgLbl;
	private JLabelFixture clientErrorMsgLbl;
	private JLabelFixture reservationErrorMsgLbl;
	private JButtonFixture addReservationBtn;
	private JButtonFixture addClientBtn;
	private JButtonFixture renameBtn;
	private JButtonFixture rescheduleBtn;
	private JButtonFixture removeClientBtn;
	private JButtonFixture removeReservationBtn;
	private JListFixture clientList;
	private JListFixture reservationList;

	private AutoCloseable closeable;

	@Mock
	private BookingPresenter bookingPresenter;

	private BookingSwingView bookingSwingView;

	@Override
	protected void onSetUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		GuiActionRunner.execute(() -> {
			bookingSwingView = new BookingSwingView();
			bookingSwingView.setBookingPresenter(bookingPresenter);
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
		clientErrorMsgLbl = window.label("clientErrorMsgLbl");
		reservationErrorMsgLbl = window.label("reservationErrorMsgLbl");
		
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

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}


	////////////// Tests on controls
	@Test @GUITest
	@DisplayName("Initial states")
	public void testControlsInitialStates() {
		// First row
		window.label(JLabelMatcher.withText("First Name"));
		nameFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("Names must not contain numbers (e.g. 0-9) or any type of symbol"
					+ " or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");

		window.label(JLabelMatcher.withText("Last Name"));
		surnameFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("Surnames must not contain numbers (e.g. 0-9) or any type of symbol"
					+ " or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		
		window.label(JLabelMatcher.withText("Date"));
		yearFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("yyyy");
		window.label(JLabelMatcher.withName("dash2Lbl").andText("-"));
		monthFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("mm");
		window.label(JLabelMatcher.withName("dash1Lbl").andText("-"));
		dayFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("dd");
		
		// Second row
		addClientBtn.requireDisabled().requireVisible();
		renameBtn.requireDisabled().requireVisible();
		addReservationBtn.requireDisabled().requireVisible();
		rescheduleBtn.requireDisabled().requireVisible();
		
		// Third row
		formErrorMsgLbl.requireText(" ");
		
		// Fourth row
		window.scrollPane("clientScrollPane");
		clientList.requireNoSelection();
		
		window.scrollPane("reservationScrollPane");
		reservationList.requireNoSelection();
		
		// Fifth row
		removeClientBtn.requireDisabled().requireVisible();
		removeReservationBtn.requireDisabled().requireVisible();
		
		// Sixth row
		clientErrorMsgLbl.requireText(" ");
		reservationErrorMsgLbl.requireText(" ");
	}

		////////////// Add Client Button
		@Test @GUITest
		@DisplayName("Name is not empty and text is typed in surname")
		public void testAddClientBtnWhenNameIsNotEmptyAndTextIsTypedInSurnameShouldBeEnabled() {
			nameFormTxt.setText(A_FIRSTNAME);
			surnameFormTxt.enterText(A_LASTNAME);
			
			addClientBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Name is not empty and spaces are typed in surname")
		public void testAddClientBtnWhenNameIsNotemptyAndSpacesAreTypedInSurnameShouldBeDisabled() {
			nameFormTxt.setText(A_FIRSTNAME);
			surnameFormTxt.enterText("  ");
			
			addClientBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name is empty and text is typed in surname")
		public void testAddClientBtnWhenNameIsEmptyAndTextIsTypedInSurnameShouldBeDisabled() {
			nameFormTxt.setText(" ");
			surnameFormTxt.enterText(A_LASTNAME);
			
			addClientBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname is not empty and text is typed in name")
		public void testAddClientBtnWhenSurnameIsNotEmptyAndTextIsTypedInNameShouldBeEnabled() {
			surnameFormTxt.setText(A_LASTNAME);
			nameFormTxt.enterText(A_FIRSTNAME);
	
			addClientBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Surname is not empty and spaces are typed in name")
		public void testAddClientBtnWhenSurnameIsNotEmptyAndSpacesAreTypedInNameShouldBeDisabled() {
			surnameFormTxt.setText(A_LASTNAME);
			nameFormTxt.enterText("   ");
	
			addClientBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname is empty and text is typed in name")
		public void testAddClientBtnWhenSurnameIsEmptyAndTextIsTypedInNameShouldBeDisabled() {
			surnameFormTxt.setText(" ");
			nameFormTxt.enterText(A_FIRSTNAME);
			
			addClientBtn.requireDisabled();
		}
		////////////// Add Client Button

		////////////// Rename Client Button
		@Test @GUITest
		@DisplayName("Client selected, name not empty and text is typed in surname")
		public void testRenameClientBtnWhenAClientIsSelectedNameIsNotEmptyAndTextIsTypedInSurnameShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			nameFormTxt.setText(A_FIRSTNAME);
			
			surnameFormTxt.enterText(ANOTHER_LASTNAME);
			
			renameBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, name not empty and spaces are typed in surname")
		public void testRenameClientBtnWhenAClientIsSelectedNameIsNotEmptyAndSpacesAreTypedInSurnameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			nameFormTxt.setText(A_FIRSTNAME);
			
			surnameFormTxt.enterText("  ");
			
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or name is empty and text is typed in surname")
		public void testRenameClientBtnWhenClientIsNotSelectedOrNameIsEmptyAndTextIsTypedInSurnameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// name is empty
			clientList.selectItem(0);
			nameFormTxt.setText(" ");
			surnameFormTxt.enterText(ANOTHER_LASTNAME);
			renameBtn.requireDisabled();
			
			// clear surname
			surnameFormTxt.setText("");
			
			// client is not selected
			clientList.clearSelection();
			nameFormTxt.setText(A_FIRSTNAME);
			surnameFormTxt.enterText(ANOTHER_LASTNAME);
			renameBtn.requireDisabled();
			
			// clear surname
			surnameFormTxt.setText("");
			
			// name is empty and client is not selected
			clientList.clearSelection();
			nameFormTxt.setText(" ");
			surnameFormTxt.enterText(ANOTHER_LASTNAME);
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, surname not empty and text is typed in name")
		public void testRenameClientBtnWhenAClientIsSelectedSurnameIsNotEmptyAndTextIsTypedInNameShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			surnameFormTxt.setText(A_LASTNAME);
			
			nameFormTxt.enterText(ANOTHER_FIRSTNAME);
			
			renameBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, surname not empty and spaces are typed in name")
		public void testRenameClientBtnWhenAClientIsSelectedSurnameIsNotEmptyAndSpacesAreTypedInNameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			surnameFormTxt.setText(A_LASTNAME);
			
			nameFormTxt.enterText("  ");
			
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or surname is empty and text is typed in name")
		public void testRenameClientBtnWhenClientIsNotSelectedOrSurnameIsEmptyAndTextIsTypedInNameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// name is empty
			clientList.selectItem(0);
			surnameFormTxt.setText(" ");
			nameFormTxt.enterText(ANOTHER_FIRSTNAME);
			renameBtn.requireDisabled();
			
			// clear surname
			nameFormTxt.setText("");
			
			// client is not selected
			clientList.clearSelection();
			surnameFormTxt.setText(A_LASTNAME);
			nameFormTxt.enterText(ANOTHER_FIRSTNAME);
			renameBtn.requireDisabled();
			
			// clear surname
			nameFormTxt.setText("");
			
			// name is empty and client is not selected
			clientList.clearSelection();
			surnameFormTxt.setText(" ");
			nameFormTxt.enterText(ANOTHER_FIRSTNAME);
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name and surname not empty and client is selected")
		public void testRenameClientBtnWhenNameAndSurnameAreNotEmptyAndAClientIsSelectedShouldBeEnabled() {
			addClientInList(A_CLIENT);
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			
			clientList.selectItem(0);
			
			renameBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Name and surname not empty and client not selected")
		public void testRenameClientBtnWhenNameAndSurnameAreNotEmptyAndAClientIsNotSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			
			clientList.clearSelection();
			
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name or surname are empty and client is selected")
		public void testRenameClientBtnWhenNameOrSurnameAreEmptyAndAClientIsSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only name is empty
			nameFormTxt.setText("");
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			clientList.selectItem(0);
			renameBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// only surname is empty
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(" ");
			clientList.selectItem(0);
			renameBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// both name and surname are empty
			nameFormTxt.setText(" ");
			surnameFormTxt.setText("");
			clientList.selectItem(0);
			renameBtn.requireDisabled();
		}
		////////////// Rename Client Button

		////////////// Remove Client Button
		@Test @GUITest
		@DisplayName("A client is selected")
		public void testRemoveClientBtnWhenAClientIsSelectedShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			
			removeClientBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("No clients are selected")
		public void testRemoveClientBtnWhenNoClientsAreSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.clearSelection();
			
			removeClientBtn.requireDisabled();
		}
		////////////// Remove Client Button

		////////////// Add Reservation Button
		@Test @GUITest
		@DisplayName("Client is selected, year and month are not empty and text is typed in day")
		public void testAddReservationBtnWhenAClientIsSelectedYearAndMonthAreNotEmptyAndTextIsTypedInDayShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			
			dayFormTxt.enterText(A_DAY);
			
			addReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client is selected, year and month are not empty and spaces are typed in day")
		public void testAddReservationBtnWhenAClientIsSelectedYearAndMonthAreNotEmptyAndSpacesAreTypedInDayShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			
			dayFormTxt.enterText(" ");
			
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or year or month are empty and text is typed in day")
		public void testAddReservationBtnWhenClientIsNotSelectedOrYearOrMonthAreEmptyAndTextIsTypedInDayShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only year is empty
			clientList.selectItem(0);
			yearFormTxt.setText(" ");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// only month is empty
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText("  ");
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected
			clientList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// both year and month are empty
			clientList.selectItem(0);
			yearFormTxt.setText("  ");
			monthFormTxt.setText(" ");
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and year is empty
			clientList.clearSelection();
			yearFormTxt.setText("   ");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and month is empty
			clientList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText("   ");
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and year and month are empty
			clientList.clearSelection();
			yearFormTxt.setText("");
			monthFormTxt.setText("");
			dayFormTxt.enterText(A_DAY);
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is selected, year and day are not empty and text is typed in month")
		public void testAddReservationBtnWhenAClientIsSelectedYearAndDayAreNotEmptyAndTextIsTypedInMonthShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			
			monthFormTxt.enterText(A_MONTH);
			
			addReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client is selected, year and day are not empty and spaces are typed in month")
		public void testAddReservationBtnWhenAClientIsSelectedYearAndDayAreNotEmptyAndSpacesAreTypedInMonthShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			
			monthFormTxt.enterText("  ");
			
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or year or day are empty and text is typed in month")
		public void testAddReservationBtnWhenClientIsNotSelectedOrYearOrDayAreEmptyAndTextIsTypedInMonthShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only year is empty
			clientList.selectItem(0);
			yearFormTxt.setText(" ");
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// only day is empty
			clientList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText("  ");
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected
			clientList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// both year and day are empty
			clientList.selectItem(0);
			yearFormTxt.setText("  ");
			dayFormTxt.setText(" ");
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and year is empty
			clientList.clearSelection();
			yearFormTxt.setText("   ");
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and day is empty
			clientList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText("   ");
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and year and day are empty
			clientList.clearSelection();
			yearFormTxt.setText("");
			dayFormTxt.setText("");
			monthFormTxt.enterText(A_MONTH);
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is selected, month and day are not empty and text is typed in year")
		public void testAddReservationBtnWhenAClientIsSelectedMonthAndDayAreNotEmptyAndTextIsTypedInYearShouldBeEnabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			yearFormTxt.enterText(A_YEAR);
			
			addReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client is selected, month and day are not empty and spaces are typed in year")
		public void testAddReservationBtnWhenAClientIsSelectedMonthAndDayAreNotEmptyAndSpacesAreTypedInYearShouldBeDisabled() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			yearFormTxt.enterText("   ");
			
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or month or day are empty and text is typed in year")
		public void testAddReservationBtnWhenClientIsNotSelectedOrMonthOrDayAreEmptyAndTextIsTypedInYearShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only month is empty
			clientList.selectItem(0);
			monthFormTxt.setText(" ");
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// only day is empty
			clientList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("  ");
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected
			clientList.clearSelection();
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// both month and day are empty
			clientList.selectItem(0);
			monthFormTxt.setText("  ");
			dayFormTxt.setText(" ");
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and month is empty
			clientList.clearSelection();
			monthFormTxt.setText("   ");
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and day is empty
			clientList.clearSelection();
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("   ");
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and month and day are empty
			clientList.clearSelection();
			monthFormTxt.setText("");
			dayFormTxt.setText("");
			yearFormTxt.enterText(A_YEAR);
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Year, month and day are not empty and client is selected")
		public void testAddReservationBtnWhenYearMonthAndDayAreNotEmptyAndClientIsSelectedShouldBeEnabled() {
			addClientInList(A_CLIENT);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			clientList.selectItem(0);
			
			addReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Year, month and day are not empty and client is not selected")
		public void testAddReservationBtnWhenYearMonthAndDayAreNotEmptyAndClientIsNotSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			clientList.clearSelection();
			
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Year, month or day are empty and client is selected")
		public void testAddReservationBtnWhenYearMonthOrDayAreEmptyAndAClientIsSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only year is empty
			yearFormTxt.setText("");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// only month is empty
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(" ");
			dayFormTxt.setText(A_DAY);
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// only day is empty
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("  ");
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// both year and month are empty
			yearFormTxt.setText(" ");
			monthFormTxt.setText("  ");
			dayFormTxt.setText(A_DAY);
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// both year and day are empty
			yearFormTxt.setText("  ");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("");
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// both month and day are empty
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText("");
			dayFormTxt.setText(" ");
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
			
			// clear selection
			clientList.clearSelection();
			
			// both year, month and day are empty
			yearFormTxt.setText("   ");
			monthFormTxt.setText("   ");
			dayFormTxt.setText("   ");
			clientList.selectItem(0);
			addReservationBtn.requireDisabled();
		}
		////////////// Add Reservation Button

		////////////// Reschedule Reservation Button
		@Test @GUITest
		@DisplayName("Reservation is selected, year and month are not empty and text is typed in day")
		public void testRescheduleBtnWhenAReservationIsSelectedYearAndMonthAreNotEmptyAndTextIsTypedInDayShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			
			dayFormTxt.enterText(ANOTHER_DAY);
			
			rescheduleBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is selected, year and month are not empty and spaces are typed in day")
		public void testRescheduleBtnWhenAReservationIsSelectedYearAndMonthAreNotEmptyAndSpacesAreTypedInDayShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			
			dayFormTxt.enterText(" ");
			
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is not selected or year or month are empty and text is typed in day")
		public void testRescheduleBtnWhenReservationIsNotSelectedOrYearOrMonthAreEmptyAndTextIsTypedInDayShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			// only year is empty
			reservationList.selectItem(0);
			yearFormTxt.setText(" ");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// only month is empty
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText("  ");
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected
			reservationList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// both year and month are empty
			reservationList.selectItem(0);
			yearFormTxt.setText("  ");
			monthFormTxt.setText(" ");
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and year is empty
			reservationList.clearSelection();
			yearFormTxt.setText("   ");
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and month is empty
			reservationList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText("   ");
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
			
			// clear day
			dayFormTxt.setText("");
			
			// client is not selected and year and month are empty
			reservationList.clearSelection();
			yearFormTxt.setText("");
			monthFormTxt.setText("");
			dayFormTxt.enterText(ANOTHER_DAY);
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is selected, year and day are not empty and text is typed in month")
		public void testRescheduleBtnWhenAReservationIsSelectedYearAndDayAreNotEmptyAndTextIsTypedInMonthShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			
			monthFormTxt.enterText(ANOTHER_MONTH);
			
			rescheduleBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is selected, year and day are not empty and spaces are typed in month")
		public void testRescheduleBtnWhenAReservationIsSelectedYearAndDayAreNotEmptyAndSpacesAreTypedInMonthShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			
			monthFormTxt.enterText("  ");
			
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is not selected or year or day are empty and text is typed in month")
		public void testRescheduleBtnWhenReservationIsNotSelectedOrYearOrDayAreEmptyAndTextIsTypedInMonthShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			// only year is empty
			reservationList.selectItem(0);
			yearFormTxt.setText(" ");
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// only day is empty
			reservationList.selectItem(0);
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText("  ");
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected
			reservationList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// both year and day are empty
			reservationList.selectItem(0);
			yearFormTxt.setText("  ");
			dayFormTxt.setText(" ");
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and year is empty
			reservationList.clearSelection();
			yearFormTxt.setText("   ");
			dayFormTxt.setText(A_DAY);
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and day is empty
			reservationList.clearSelection();
			yearFormTxt.setText(A_YEAR);
			dayFormTxt.setText("   ");
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
			
			// clear month
			monthFormTxt.setText("");
			
			// client is not selected and year and day are empty
			reservationList.clearSelection();
			yearFormTxt.setText("");
			dayFormTxt.setText("");
			monthFormTxt.enterText(ANOTHER_MONTH);
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is selected, month and day are not empty and text is typed in year")
		public void testRescheduleBtnWhenAReservationIsSelectedMonthAndDayAreNotEmptyAndTextIsTypedInYearShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			yearFormTxt.enterText(ANOTHER_YEAR);
			
			rescheduleBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is selected, month and day are not empty and spaces are typed in year")
		public void testRescheduleBtnWhenAReservationIsSelectedMonthAndDayAreNotEmptyAndSpacesAreTypedInYearShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			
			yearFormTxt.enterText("   ");
			
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Reservation is not selected or month or day are empty and text is typed in year")
		public void testRescheduleBtnWhenReservationIsNotSelectedOrMonthOrDayAreEmptyAndTextIsTypedInYearShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			// only month is empty
			reservationList.selectItem(0);
			monthFormTxt.setText(" ");
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// only day is empty
			reservationList.selectItem(0);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("  ");
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected
			reservationList.clearSelection();
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// both month and day are empty
			reservationList.selectItem(0);
			monthFormTxt.setText("  ");
			dayFormTxt.setText(" ");
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and month is empty
			reservationList.clearSelection();
			monthFormTxt.setText("   ");
			dayFormTxt.setText(A_DAY);
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and day is empty
			reservationList.clearSelection();
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText("   ");
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
			
			// clear year
			yearFormTxt.setText("");
			
			// client is not selected and month and day are empty
			reservationList.clearSelection();
			monthFormTxt.setText("");
			dayFormTxt.setText("");
			yearFormTxt.enterText(ANOTHER_YEAR);
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Year, month and day are not empty and reservation is selected")
		public void testRescheduleBtnWhenYearMonthAndDayAreNotEmptyAndReservationIsSelectedShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText(ANOTHER_DAY);
			
			reservationList.selectItem(0);
			
			rescheduleBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Year, month and day are not empty and reservation is not selected")
		public void testRescheduleBtnWhenYearMonthAndDayAreNotEmptyAndReservationIsNotSelectedShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText(ANOTHER_DAY);
			
			reservationList.clearSelection();
			
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Year, month or day are empty and reservation is selected")
		public void testRescheduleBtnWhenYearMonthOrDayAreEmptyAndAReservationIsSelectedShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			// only year is empty
			yearFormTxt.setText("");
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText(ANOTHER_DAY);
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// only month is empty
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(" ");
			dayFormTxt.setText(ANOTHER_DAY);
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// only day is empty
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText("  ");
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// both year and month are empty
			yearFormTxt.setText(" ");
			monthFormTxt.setText("  ");
			dayFormTxt.setText(ANOTHER_DAY);
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// both year and day are empty
			yearFormTxt.setText("  ");
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText("");
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// both month and day are empty
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText("");
			dayFormTxt.setText(" ");
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
			
			// clear selection
			reservationList.clearSelection();
			
			// both year, month and day are empty
			yearFormTxt.setText("   ");
			monthFormTxt.setText("   ");
			dayFormTxt.setText("   ");
			reservationList.selectItem(0);
			rescheduleBtn.requireDisabled();
		}
		////////////// Reschedule Reservation Button

		////////////// Remove Reservation Button
		@Test @GUITest
		@DisplayName("A reservation is selected")
		public void testRemoveReservationBtnWhenAReservationIsSelectedShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			
			removeReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("No reservations are selected")
		public void testRemoveReservationBtnWhenNoReservationsAreSelectedShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			reservationList.clearSelection();
			
			removeReservationBtn.requireDisabled();
		}
		////////////// Remove Reservation Button

	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
	}
	////////////// Tests on controls

	////////////// Tests on methods

		////////////// Tests for 'showAllClients'
		@Test @GUITest
		@DisplayName("There are no clients")
		public void testShowAllClientsWhenThereAreNoClientsShouldNotShowAnyElements() {
			bookingSwingView.showAllClients(Collections.emptyList());
			
			assertThat(clientList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("There are some clients")
		public void testShowAllClientsWhenThereAreSomeClientsShouldShowAllOfThemAndResetSelection() {
			GuiActionRunner.execute(() -> bookingSwingView.showAllClients(
					Arrays.asList(A_CLIENT, ANOTHER_CLIENT)));
			
			assertThat(clientList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
			clientList.requireNoSelection();
		}

		@Test @GUITest
		@DisplayName("There are already some clients")
		public void testShowAllClientsWhenThereAreAlreadySomeClientsShouldShowOnlyNewOnes() {
			Client oldClient = new Client("Giovanni", "De Chirico");
			addClientInList(A_CLIENT);
			addClientInList(oldClient);
			
			GuiActionRunner.execute(() -> bookingSwingView.showAllClients(
					Arrays.asList(A_CLIENT, ANOTHER_CLIENT)));
			
			assertThat(clientList.contents())
				.doesNotContain(oldClient.toString())
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("There are some buttons enabled")
		public void testShowAllClientsWhenThereAreSomeButtonsEnabledShouldDisableThem() {
			enableButton(bookingSwingView.getRenameBtn());
			enableButton(bookingSwingView.getRemoveClientBtn());
			enableButton(bookingSwingView.getAddReservationBtn());
			
			GuiActionRunner.execute(() -> bookingSwingView.showAllClients(
					Arrays.asList(A_CLIENT, ANOTHER_CLIENT)));
			
			renameBtn.requireDisabled();
			removeClientBtn.requireDisabled();
			addReservationBtn.requireDisabled();
		}
		////////////// Tests for 'showAllClients'

		////////////// Tests for 'showAllReservations'
		@Test @GUITest
		@DisplayName("There are no reservations")
		public void testShowAllReservationsWhenThereAreNoReservationsShouldNotShowAnyElements() {
			bookingSwingView.showAllReservations(Collections.emptyList());
			
			assertThat(reservationList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("There are some reservations")
		public void testShowAllReservationsWhenThereAreSomeReservationsShouldShowAllOfThemAndResetSelection() {
			GuiActionRunner.execute(() -> bookingSwingView.showAllReservations(
					Arrays.asList(A_RESERVATION, ANOTHER_RESERVATION)));
			
			assertThat(reservationList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
			reservationList.requireNoSelection();
		}

		@Test @GUITest
		@DisplayName("There are already some reservations")
		public void testShowAllReservationsWhenThereAreAlreadySomeReservationsShouldShowOnlyNewOnes() {
			Reservation oldReservation = new Reservation(
					UUID.randomUUID(), LocalDate.parse("2023-05-09"));
			addReservationInList(A_RESERVATION);
			addReservationInList(oldReservation);
			
			GuiActionRunner.execute(() -> bookingSwingView.showAllReservations(
					Arrays.asList(A_RESERVATION, ANOTHER_RESERVATION)));
			
			assertThat(reservationList.contents())
				.doesNotContain(oldReservation.toString())
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("There are some buttons enabled")
		public void testShowAllReservationsWhenThereAreSomeButtonsEnabledShouldDisableThem() {
			enableButton(bookingSwingView.getRescheduleBtn());
			enableButton(bookingSwingView.getRemoveReservationBtn());
			
			GuiActionRunner.execute(() -> bookingSwingView.showAllReservations(
					Arrays.asList(A_RESERVATION, ANOTHER_RESERVATION)));
			
			rescheduleBtn.requireDisabled();
			removeReservationBtn.requireDisabled();
		}
		////////////// Tests for 'showAllReservations'

		////////////// Tests for 'reservationAdded'
		@Test @GUITest
		@DisplayName("Reservation list is empty")
		public void testReservationAddedWhenReservationListIsEmptyShouldAddItToTheList() {
			GuiActionRunner.execute(() -> bookingSwingView.reservationAdded(A_RESERVATION));
			
			assertThat(reservationList.contents())
				.hasSize(1)
				.containsExactly(A_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("There are already some reservations")
		public void testReservationAddedWhenThereAreAlreadySomeReservationsShouldAddItToTheListAndNotChangeTheOthers() {
			addReservationInList(A_RESERVATION);
			
			GuiActionRunner.execute(() -> bookingSwingView.reservationAdded(ANOTHER_RESERVATION));
			
			assertThat(reservationList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("A reservation is selected")
		public void testReservationAddedWhenAReservationIsSelectedShouldNotChangeTheSelection() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			
			GuiActionRunner.execute(() -> bookingSwingView.reservationAdded(ANOTHER_RESERVATION));
			
			String[] selectedReservations = reservationList.selection();
			assertThat(selectedReservations).hasSize(1);
			assertThat(selectedReservations[0]).isEqualTo(A_RESERVATION.toString());
		}
		////////////// Tests for 'reservationAdded'

		////////////// Tests for 'clientAdded'
		@Test @GUITest
		@DisplayName("Client list is empty")
		public void testClientAddedWhenClientListIsEmptyShouldAddItToTheList() {
			GuiActionRunner.execute(() -> bookingSwingView.clientAdded(A_CLIENT));
			
			assertThat(clientList.contents())
				.hasSize(1)
				.containsExactly(A_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("There are already some clients")
		public void testClientAddedWhenThereAreAlreadySomeClientsShouldAddItToTheListAndNotChangeTheOthers() {
			addClientInList(A_CLIENT);
			
			GuiActionRunner.execute(() -> bookingSwingView.clientAdded(ANOTHER_CLIENT));
			
			assertThat(clientList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("A client is selected")
		public void testClientAddedWhenAClientIsSelectedShouldNotChangeTheSelection() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			
			GuiActionRunner.execute(() -> bookingSwingView.clientAdded(ANOTHER_CLIENT));
			
			String[] selectedClients = clientList.selection();
			assertThat(selectedClients).hasSize(1);
			assertThat(selectedClients[0]).isEqualTo(A_CLIENT.toString());
		}
		////////////// Tests for 'clientAdded'

		private void enableButton(JButton button) {
			GuiActionRunner.execute(() -> button.setEnabled(true));
		}
	
	////////////// Tests on methods
}
