package io.github.marcopaglio.booking.view.swing;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

import java.time.LocalDate;
import java.util.UUID;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
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

	private static final Reservation A_RESERVATION = new Reservation(
			UUID.fromString("9686dee5-1675-48ee-b5c3-81f164a9cf04"),
			LocalDate.parse("2022-04-24"));

	private FrameFixture window;

	private BookingSwingView bookingSwingView;

	@Override
	protected void onSetUp() throws Exception {
		GuiActionRunner.execute(() -> {
			bookingSwingView = new BookingSwingView();
			return bookingSwingView;
		});
		
		window = new FrameFixture(robot(), bookingSwingView);
		window.show();
	}

	////////////// Tests on controls
	@Test @GUITest
	@DisplayName("Initial states")
	public void testControlsInitialStates() {
		// First row
		window.label(JLabelMatcher.withText("First Name"));
		window.textBox("nameFormTxt")
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("Names must not contain numbers (e.g. 0-9) or any type of symbol"
					+ " or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		window.button(JButtonMatcher.withText("Add Client"))
			.requireDisabled()
			.requireVisible();
		
		window.label(JLabelMatcher.withText("Date"));
		window.textBox("yearFormTxt").requireEnabled().requireEditable().requireEmpty()
			.requireToolTip("yyyy");
		window.label(JLabelMatcher.withName("dash2Lbl").andText("-"));
		window.textBox("dayFormTxt").requireEnabled().requireEditable().requireEmpty()
			.requireToolTip("dd");
		window.label(JLabelMatcher.withName("dash1Lbl").andText("-"));
		window.textBox("monthFormTxt").requireEnabled().requireEditable().requireEmpty()
			.requireToolTip("mm");
		
		// Second row
		window.label(JLabelMatcher.withText("Last Name"));
		window.textBox("surnameFormTxt")
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("Surnames must not contain numbers (e.g. 0-9) or any type of symbol"
					+ " or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		window.button(JButtonMatcher.withText("Rename"))
			.requireDisabled()
			.requireVisible();
		
		window.button(JButtonMatcher.withText("Add Reservation"))
			.requireDisabled()
			.requireVisible();
		window.button(JButtonMatcher.withText("Reschedule"))
			.requireDisabled()
			.requireVisible();
		
		// Third row
		window.label("formErrorMsgLbl").requireText(" ");
		
		// Fourth row
		window.scrollPane("clientScrollPane");
		window.list("clientList").requireNoSelection();
		
		window.scrollPane("reservationScrollPane");
		window.list("reservationList").requireNoSelection();
		
		// Fifth row
		window.button(JButtonMatcher.withText("Remove Client"))
			.requireDisabled()
			.requireVisible();
		
		window.button(JButtonMatcher.withText("Remove Reservation"))
			.requireDisabled()
			.requireVisible();
		
		// Sixth row
		window.label("clientErrorMsgLbl").requireText(" ");
		
		window.label("reservationErrorMsgLbl").requireText(" ");
	}

		////////////// Add Client Button
		@Test @GUITest
		@DisplayName("Name is not empty and text is typed in surname")
		public void testAddClientBtnWhenNameIsNotEmptyAndTextIsTypedInSurnameShouldBeEnabled() {
			window.textBox("nameFormTxt").setText(A_FIRSTNAME);
			window.textBox("surnameFormTxt").enterText(A_LASTNAME);
			
			window.button(JButtonMatcher.withText("Add Client")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Name is empty and text is typed in surname")
		public void testAddClientBtnWhenNameIsEmptyAndTextIsTypedInSurnameShouldBeDisabled() {
			window.textBox("nameFormTxt").setText(" ");
			window.textBox("surnameFormTxt").enterText(A_LASTNAME);
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name is not empty and spaces are typed in surname")
		public void testAddClientBtnWhenNameIsNotemptyAndSpacesAreTypedInSurnameShouldBeDisabled() {
			window.textBox("nameFormTxt").setText(A_FIRSTNAME);
			window.textBox("surnameFormTxt").enterText("  ");
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname is not empty and text is typed in name")
		public void testAddClientBtnWhenSurnameIsNotEmptyAndTextIsTypedInNameShouldBeEnabled() {
			window.textBox("surnameFormTxt").setText(A_LASTNAME);
			window.textBox("nameFormTxt").enterText(A_FIRSTNAME);
	
			window.button(JButtonMatcher.withText("Add Client")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Surname is empty and text is typed in name")
		public void testAddClientBtnWhenSurnameIsEmptyAndTextIsTypedInNameShouldBeDisabled() {
			window.textBox("surnameFormTxt").setText(" ");
			window.textBox("nameFormTxt").enterText(A_FIRSTNAME);
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname is not empty and spaces are typed in name")
		public void testAddClientBtnWhenSurnameIsNotEmptyAndSpacesAreTypedInNameShouldBeDisabled() {
			window.textBox("surnameFormTxt").setText(A_LASTNAME);
			window.textBox("nameFormTxt").enterText("   ");
	
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		////////////// Rename Client Button
		@Test @GUITest
		@DisplayName("Client selected, name not empty and text is typed in surname")
		public void testRenameClientBtnWhenAClientIsSelectedNameIsNotEmptyAndTextIsTypedInSurnameShouldBeEnabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").selectItem(0);
			window.textBox("nameFormTxt").setText(A_FIRSTNAME);
			
			window.textBox("surnameFormTxt").enterText(ANOTHER_LASTNAME);
			
			window.button(JButtonMatcher.withText("Rename")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, name not empty and spaces are typed in surname")
		public void testRenameClientBtnWhenAClientIsSelectedNameIsNotEmptyAndSpacesAreTypedInSurnameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").selectItem(0);
			window.textBox("nameFormTxt").setText(A_FIRSTNAME);
			
			window.textBox("surnameFormTxt").enterText("  ");
			
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or name is empty and text is typed in surname")
		public void testRenameClientBtnWhenClientIsNotSelectedOrNameIsEmptyAndTextIsTypedInSurnameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// name is empty
			window.list("clientList").selectItem(0);
			window.textBox("nameFormTxt").setText(" ");
			window.textBox("surnameFormTxt").enterText(ANOTHER_LASTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// clear surname
			window.textBox("surnameFormTxt").setText("");
			
			// client is not selected
			window.list("clientList").clearSelection();
			window.textBox("nameFormTxt").setText(A_FIRSTNAME);
			window.textBox("surnameFormTxt").enterText(ANOTHER_LASTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// clear surname
			window.textBox("surnameFormTxt").setText("");
			
			// name is empty and client is not selected
			window.list("clientList").clearSelection();
			window.textBox("nameFormTxt").setText(" ");
			window.textBox("surnameFormTxt").enterText(ANOTHER_LASTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, surname not empty and text is typed in name")
		public void testRenameClientBtnWhenAClientIsSelectedSurnameIsNotEmptyAndTextIsTypedInNameShouldBeEnabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").selectItem(0);
			window.textBox("surnameFormTxt").setText(A_LASTNAME);
			
			window.textBox("nameFormTxt").enterText(ANOTHER_FIRSTNAME);
			
			window.button(JButtonMatcher.withText("Rename")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Client selected, surname not empty and spaces are typed in name")
		public void testRenameClientBtnWhenAClientIsSelectedSurnameIsNotEmptyAndSpacesAreTypedInNameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").selectItem(0);
			window.textBox("surnameFormTxt").setText(A_LASTNAME);
			
			window.textBox("nameFormTxt").enterText("  ");
			
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Client is not selected or surname is empty and text is typed in name")
		public void testRenameClientBtnWhenClientIsNotSelectedOrSurnameIsEmptyAndTextIsTypedInNameShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// name is empty
			window.list("clientList").selectItem(0);
			window.textBox("surnameFormTxt").setText(" ");
			window.textBox("nameFormTxt").enterText(ANOTHER_FIRSTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// clear surname
			window.textBox("nameFormTxt").setText("");
			
			// client is not selected
			window.list("clientList").clearSelection();
			window.textBox("surnameFormTxt").setText(A_LASTNAME);
			window.textBox("nameFormTxt").enterText(ANOTHER_FIRSTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// clear surname
			window.textBox("nameFormTxt").setText("");
			
			// name is empty and client is not selected
			window.list("clientList").clearSelection();
			window.textBox("surnameFormTxt").setText(" ");
			window.textBox("nameFormTxt").enterText(ANOTHER_FIRSTNAME);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name and surname not empty and client is selected")
		public void testRenameClientBtnWhenNameAndSurnameAreNotEmptyAndAClientIsSelectedShouldBeEnabled() {
			addClientInList(A_CLIENT);
			window.textBox("nameFormTxt").setText(ANOTHER_FIRSTNAME);
			window.textBox("surnameFormTxt").setText(ANOTHER_LASTNAME);
			
			window.list("clientList").selectItem(0);
			
			window.button(JButtonMatcher.withText("Rename")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Name and surname not empty and client not selected")
		public void testRenameClientBtnWhenNameAndSurnameAreNotEmptyAndAClientIsNotSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			window.textBox("nameFormTxt").setText(ANOTHER_FIRSTNAME);
			window.textBox("surnameFormTxt").setText(ANOTHER_LASTNAME);
			
			window.list("clientList").clearSelection();
			
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name or surname are empty and client is selected")
		public void testRenameClientBtnWhenNameOrSurnameAreEmptyAndAClientIsSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			// only name is empty
			window.textBox("nameFormTxt").setText("");
			window.textBox("surnameFormTxt").setText(ANOTHER_LASTNAME);
			window.list("clientList").selectItem(0);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// only surname is empty
			window.textBox("nameFormTxt").setText(ANOTHER_FIRSTNAME);
			window.textBox("surnameFormTxt").setText(" ");
			window.list("clientList").selectItem(0);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
			
			// both name and surname are empty
			window.textBox("nameFormTxt").setText(" ");
			window.textBox("surnameFormTxt").setText("");
			window.list("clientList").selectItem(0);
			window.button(JButtonMatcher.withText("Rename")).requireDisabled();
		}

		////////////// Remove Client Button
		@Test @GUITest
		@DisplayName("A client is selected")
		public void testRemoveClientBtnWhenAClientIsSelectedShouldBeEnabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").selectItem(0);
			
			window.button(JButtonMatcher.withText("Remove Client")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("No clients are selected")
		public void testRemoveClientBtnWhenNoClientsAreSelectedShouldBeDisabled() {
			addClientInList(A_CLIENT);
			window.list("clientList").clearSelection();
			
			window.button(JButtonMatcher.withText("Remove Client")).requireDisabled();
		}

		////////////// Remove Reservation Button
		@Test @GUITest
		@DisplayName("A reservation is selected")
		public void testRemoveReservationBtnWhenAReservationIsSelectedShouldBeEnabled() {
			addReservationInList(A_RESERVATION);
			window.list("reservationList").selectItem(0);
			
			window.button(JButtonMatcher.withText("Remove Reservation")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("No reservations are selected")
		public void testRemoveReservationBtnWhenNoReservationsAreSelectedShouldBeDisabled() {
			addReservationInList(A_RESERVATION);
			window.list("reservationList").clearSelection();
			
			window.button(JButtonMatcher.withText("Remove Reservation")).requireDisabled();
		}

	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
	}
}
