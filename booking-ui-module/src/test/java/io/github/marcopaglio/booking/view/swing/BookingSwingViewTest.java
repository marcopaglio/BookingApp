package io.github.marcopaglio.booking.view.swing;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.assertj.swing.annotation.GUITest;
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
	public void test() {
		// TODO
	}
}
