package io.github.marcopaglio.booking.view.swing;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

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

	@Test @GUITest
	public void testControlsInitialStates() {
		// First row
		window.label(JLabelMatcher.withText("Name"));
		window.textBox("nameInputTextBox")
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("Names must not contain numbers (e.g. 0-9) or any type of symbol"
					+ " or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		window.button(JButtonMatcher.withText("Add Client"))
			.requireDisabled()
			.requireVisible();
		
		window.label(JLabelMatcher.withText("Date"));
		window.textBox("dateInputTextBox").requireEnabled().requireEditable().requireEmpty()
			.requireToolTip("Dates must be in format aaaa-mm-dd (e.g. 2022-04-24)");
		window.button(JButtonMatcher.withText("Add Reservation"))
			.requireDisabled()
			.requireVisible();
		
		// Second row
		window.label(JLabelMatcher.withText("Surname"));
		window.textBox("surnameInputTextBox")
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
		window.label("formErrorMessageLabel").requireText(" ");
		
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
		window.label("clientErrorMessageLabel").requireText(" ");
		
		window.label("reservationErrorMessageLabel").requireText(" ");
	}
}
