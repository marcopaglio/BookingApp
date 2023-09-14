package io.github.marcopaglio.booking.view.swing;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

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

	// Tests on controls
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
		window.textBox("dateFormTxt").requireEnabled().requireEditable().requireEmpty()
			.requireToolTip("Dates must be in format aaaa-mm-dd (e.g. 2022-04-24)");
		window.button(JButtonMatcher.withText("Add Reservation"))
			.requireDisabled()
			.requireVisible();
		
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
		
		window.button(JButtonMatcher.withText("Reschedule"))
			.requireDisabled()
			.requireVisible();
		
		// Third row
		window.label("formErrorMsgLbl").requireText(" ");
		
		// Fourth row
		window.scrollPane("clientScrollPane");
		window.list("clientList");
		
		window.scrollPane("reservationScrollPane");
		window.list("reservationList");
		
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

		// Add Client Button
		@Test @GUITest
		@DisplayName("Name form is not empty and some text is typed in surname form")
		public void testAddClientBtnWhenNameFormIsNotEmptyAndSomeTextIsTypedInSurnameFormShouldBeEnabled() {
			window.textBox("nameFormTxt").setText("Mario");
			window.textBox("surnameFormTxt").enterText("Rossi");
			
			window.button(JButtonMatcher.withText("Add Client")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Name form is blank and some text is typed in surname form")
		public void testAddClientBtnWhenNameFormIsBlankAndSomeTextIsTypedInSurnameFormShouldBeDisabled() {
			window.textBox("nameFormTxt").setText(" ");
			window.textBox("surnameFormTxt").enterText("Rossi");
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Name form is not empty and some spaces are typed in surname form")
		public void testAddClientBtnWhenNameFormIsNotemptyAndSomeSpacesAreTypedInSurnameFormShouldBeDisabled() {
			window.textBox("nameFormTxt").setText("Mario");
			window.textBox("surnameFormTxt").enterText("   ");
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname form is not empty and some text is typed in name form")
		public void testAddClientBtnWhenSurnameFormIsNotEmptyAndSomeTextIsTypedInNameFormShouldBeEnabled() {
			window.textBox("surnameFormTxt").setText("Rossi");
			window.textBox("nameFormTxt").enterText("Mario");
	
			window.button(JButtonMatcher.withText("Add Client")).requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Surname form is blank and some text is typed in name form")
		public void testAddClientBtnWhenSurnameFormIsBlankAndSomeTextIsTypedInNameFormShouldBeDisabled() {
			window.textBox("surnameFormTxt").setText(" ");
			window.textBox("nameFormTxt").enterText("Mario");
			
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Surname form is not empty and some spaces are typed in name form")
		public void testAddClientBtnWhenSurnameFormIsNotEmptyAndSomeSpacesAreTypedInNameFormShouldBeEnabled() {
			window.textBox("surnameFormTxt").setText("Rossi");
			window.textBox("nameFormTxt").enterText("   ");
	
			window.button(JButtonMatcher.withText("Add Client")).requireDisabled();
		}
}
