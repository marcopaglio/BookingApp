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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;

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
	private static final int TIMEOUT = 5000;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);
	private static final UUID A_CLIENT_UUID = UUID.fromString("9686dee5-1675-48ee-b5c3-81f164a9cf04");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final Client ANOTHER_CLIENT = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final String A_DATE = A_YEAR + "-" + A_MONTH + "-" + A_DAY;
	private static final Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, LocalDate.parse(A_DATE));
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final String ANOTHER_DATE = ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY;
	private static final Reservation ANOTHER_RESERVATION = new Reservation(A_CLIENT_UUID, LocalDate.parse(ANOTHER_DATE));

	private static final String OPERATIONS_ERROR_MSG = "An error message that involves operations.";
	private static final String FORMS_ERROR_MSG = "An error message that involves forms.";

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
	private BookingPresenter bookingPresenter;

	private BookingSwingView bookingSwingView;

	private AutoCloseable closeable;

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
			.requireToolTip("year");
		window.label(JLabelMatcher.withName("dash2Lbl").andText("-"));
		monthFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("month");
		window.label(JLabelMatcher.withName("dash1Lbl").andText("-"));
		dayFormTxt
			.requireEnabled()
			.requireEditable()
			.requireEmpty()
			.requireToolTip("day");
		
		// Second row
		formErrorMsgLbl.requireText(" ");
		
		// Third row
		addClientBtn.requireDisabled().requireVisible();
		renameBtn.requireDisabled().requireVisible();
		addReservationBtn.requireDisabled().requireVisible();
		rescheduleBtn.requireDisabled().requireVisible();
		
		// Fourth row
		window.scrollPane("clientScrollPane");
		clientList.requireNoSelection();
		
		window.scrollPane("reservationScrollPane");
		reservationList.requireNoSelection();
		
		// Fifth row
		removeClientBtn.requireDisabled().requireVisible();
		removeReservationBtn.requireDisabled().requireVisible();
		
		// Sixth row
		operationErrorMsgLbl.requireText(" ");
	}

		////////////// Reservation List Selection
		@Test @GUITest
		@DisplayName("The associated client exists")
		public void testReservationListSelectionWhenTheAssociatedClientExistsShouldSelectIt() {
			A_CLIENT.setId(A_CLIENT_UUID);
			addClientInList(A_CLIENT);
			addReservationInList(A_RESERVATION);
			
			reservationList.selectItem(0);
			
			clientList.requireSelection(0);
		}
	
		@Test @GUITest
		@DisplayName("The associated client does not exist")
		public void testReservationListSelectionWhenTheAssociatedClientDoesNotExistShouldClearSelection() {
			addClientInList(ANOTHER_CLIENT);
			addReservationInList(A_RESERVATION);
			
			clientList.selectItem(0);
			
			reservationList.selectItem(0);
			
			clientList.requireNoSelection();
		}

		@Test @GUITest
		@DisplayName("Null reservation")
		public void testReservationListSelectionWhenTheReservationSelectedIsNullShouldClearSelection() {
			addClientInList(A_CLIENT);
			addReservationInList(null);
			
			clientList.selectItem(0);
			
			reservationList.selectItem(0);
			
			clientList.requireNoSelection();
		}
	
		@Test @GUITest
		@DisplayName("There are null clients")
		public void testReservationListSelectionWhenThereAreNullClientsBeforeTheAssociatedClientShouldNotThrow() {
			addClientInList(null);
			A_CLIENT.setId(A_CLIENT_UUID);
			addClientInList(A_CLIENT);
			addReservationInList(A_RESERVATION);
			
			assertThatNoException().isThrownBy(() -> reservationList.selectItem(0));
		}
		////////////// Reservation List Selection

		////////////// Add Client Button
			////////////// Name Form Text
			@Test @GUITest
			@DisplayName("Not blank surname and text is typed")
			public void testNameFormTxtWhenSurnameIsNotBlankAndTextIsTypedShouldEnableAddClientBtn() {
				surnameFormTxt.setText(A_LASTNAME);
				nameFormTxt.enterText(A_FIRSTNAME);
		
				addClientBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add client enabled and text is deleted")
			public void testNameFormTxtWhenAddClientBtnIsEnabledAndTextIsDeletedShouldDisableAddClientBtn() {
				surnameFormTxt.setText(A_LASTNAME);
				nameFormTxt.setText(A_FIRSTNAME);
				enableButton(bookingSwingView.getAddClientBtn());
				
				nameFormTxt.deleteText();
		
				addClientBtn.requireDisabled();
			}
	
			@Test @GUITest
			@DisplayName("Not blank surname and spaces are typed")
			public void testNameFormTxtWhenSurnameIsNotBlankAndSpacesAreTypedShouldNotEnableAddClientBtn() {
				surnameFormTxt.setText(A_LASTNAME);
				nameFormTxt.enterText("   ");
		
				addClientBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty surname and text is typed")
			public void testNameFormTxtWhenSurnameIsEmptyAndTextIsTypedShouldNotEnableAddClientBtn() {
				surnameFormTxt.setText("");
				nameFormTxt.enterText(A_FIRSTNAME);
				
				addClientBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank surname and text is typed")
			public void testNameFormTxtWhenSurnameIsBlankAndTextIsTypedShouldNotEnableAddClientBtn() {
				surnameFormTxt.setText("  ");
				nameFormTxt.enterText(A_FIRSTNAME);
				
				addClientBtn.requireDisabled();
			}
			////////////// Name Form Text

			////////////// Surname Form Text
			@Test @GUITest
			@DisplayName("Not blank name and text is typed")
			public void testSurnameFormTxtWhenNameIsNotBlankAndTextIsTypedShouldEnableAddClientBtn() {
				nameFormTxt.setText(A_FIRSTNAME);
				surnameFormTxt.enterText(A_LASTNAME);
				
				addClientBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add client enabled and text is deleted")
			public void testSurnameFormTxtWhenAddClientBtnIsEnabledAndTextIsDeletedShouldDisableAddClientBtn() {
				nameFormTxt.setText(A_FIRSTNAME);
				surnameFormTxt.enterText(A_LASTNAME);
				enableButton(bookingSwingView.getAddClientBtn());
				
				surnameFormTxt.deleteText();
				
				addClientBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Not blank name and spaces are typed")
			public void testSurnameFormTxtWhenNameIsNotBlankAndSpacesAreTypedShouldNotEnableAddClientBtn() {
				nameFormTxt.setText(A_FIRSTNAME);
				surnameFormTxt.enterText("  ");
				
				addClientBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty name and text is typed")
			public void testSurnameFormTxtWhenNameIsEmptyAndTextIsTypedShouldNotEnableAddClientBtn() {
				nameFormTxt.setText("");
				surnameFormTxt.enterText(A_LASTNAME);
				
				addClientBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank name and text is typed")
			public void testSurnameFormTxtWhenNameIsBlankAndTextIsTypedShouldNotEnableAddClientBtn() {
				nameFormTxt.setText("  ");
				surnameFormTxt.enterText(A_LASTNAME);
				
				addClientBtn.requireDisabled();
			}
			////////////// Surname Form Text

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testAddClientBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			nameFormTxt.setText(A_FIRSTNAME);
			surnameFormTxt.setText(A_LASTNAME);
			enableButton(bookingSwingView.getAddClientBtn());
			
			addClientBtn.click();
			
			addClientBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT)).addClient(A_FIRSTNAME, A_LASTNAME);
		}
		////////////// Add Client Button


		////////////// Rename Button
			////////////// Name Form Text
			@Test @GUITest
			@DisplayName("Client selected, not blank surname and text is typed")
			public void testNameFormTxtWhenAClientIsSelectedAndSurnameIsNotBlankAndTextIsTypedShouldEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				surnameFormTxt.setText(A_LASTNAME);
				
				nameFormTxt.enterText(ANOTHER_FIRSTNAME);
				
				renameBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Rename enabled and text is deleted")
			public void testNameFormTxtWhenRenameBtnIsEnabledAndTextIsDeletedShouldDisableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(ANOTHER_FIRSTNAME);
				surnameFormTxt.setText(A_LASTNAME);
				enableButton(bookingSwingView.getRenameBtn());
				
				nameFormTxt.deleteText();
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client selected, not blank surname and spaces are typed")
			public void testNameFormTxtWhenAClientIsSelectedAndSurnameIsNotBlankAndSpacesAreTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				surnameFormTxt.setText(A_LASTNAME);
				
				nameFormTxt.enterText("  ");
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty surname and text is typed")
			public void testNameFormTxtWhenSurnameIsEmptyAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				surnameFormTxt.setText("");
				
				nameFormTxt.enterText(ANOTHER_FIRSTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank surname and text is typed")
			public void testNameFormTxtWhenSurnameIsBlankAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				surnameFormTxt.setText("   ");
				
				nameFormTxt.enterText(ANOTHER_FIRSTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected and text is typed")
			public void testNameFormTxtWhenClientIsNotSelectedAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				surnameFormTxt.setText(A_LASTNAME);
				
				nameFormTxt.enterText(ANOTHER_FIRSTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank surname and text is typed")
			public void testNameFormTxtWhenClientIsNotSelectedAndSurnameIsBlankAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				surnameFormTxt.setText("  ");
				
				nameFormTxt.enterText(ANOTHER_FIRSTNAME);
				
				renameBtn.requireDisabled();
			}
			////////////// Name Form Text

			////////////// Surname Form Text
			@Test @GUITest
			@DisplayName("Client selected, not blank name and text is typed")
			public void testSurnameFormTxtWhenAClientIsSelectedAndNameIsNotBlankAndTextIsTypedShouldEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(A_FIRSTNAME);
				
				surnameFormTxt.enterText(ANOTHER_LASTNAME);
				
				renameBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Rename enabled and text is deleted")
			public void testSurnameFormTxtWhenRenameBtnIsEnabledAndTextIsDeletedShouldDisableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(A_FIRSTNAME);
				surnameFormTxt.setText(ANOTHER_LASTNAME);
				enableButton(bookingSwingView.getRenameBtn());
				
				surnameFormTxt.deleteText();
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client selected, not blank name and spaces are typed")
			public void testSurnameFormTxtWhenAClientIsSelectedAndNameIsNotBlankAndSpacesAreTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(A_FIRSTNAME);
				
				surnameFormTxt.enterText("  ");
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty name and text is typed")
			public void testSurnameFormTxtWhenNameIsEmptyAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText("");
				
				surnameFormTxt.enterText(ANOTHER_LASTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank name and text is typed")
			public void testSurnameFormTxtWhenNameIsBlankAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(" ");
				
				surnameFormTxt.enterText(ANOTHER_LASTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected and text is typed")
			public void testSurnameFormTxtWhenClientIsNotSelectedAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				nameFormTxt.setText(A_FIRSTNAME);
				
				surnameFormTxt.enterText(ANOTHER_LASTNAME);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank name and text is typed")
			public void testSurnameFormTxtWhenClientIsNotSelectedAndNameIsBlankAndTextIsTypedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				nameFormTxt.setText("");
				
				surnameFormTxt.enterText(ANOTHER_LASTNAME);
				
				renameBtn.requireDisabled();
			}
			////////////// Surname Form Text

			////////////// Client List
			@Test @GUITest
			@DisplayName("Not blank name and surname and client is selected")
			public void testClientListWhenNameAndSurnameAreNotBlankAndAClientIsSelectedShouldEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText(ANOTHER_FIRSTNAME);
				surnameFormTxt.setText(ANOTHER_LASTNAME);
				
				clientList.selectItem(0);
				
				renameBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Rename enabled and client is deselected")
			public void testClientListWhenRenameBtnIsEnabledAndAClientIsDeselectedShouldDisableRenameBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				nameFormTxt.setText(ANOTHER_FIRSTNAME);
				surnameFormTxt.setText(ANOTHER_LASTNAME);
				enableButton(bookingSwingView.getRenameBtn());
				
				// unselectItem would be more clear but it doesn't trigger the list selection listener
				clientList.clearSelection();
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty name and client is selected")
			public void testClientListWhenNameIsEmptyAndAClientIsSelectedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText("");
				surnameFormTxt.setText(ANOTHER_LASTNAME);
				
				clientList.selectItem(0);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank name and client is selected")
			public void testClientListWhenNameIsBlankAndAClientIsSelectedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText(" ");
				surnameFormTxt.setText(ANOTHER_LASTNAME);
				
				clientList.selectItem(0);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty surname and client is selected")
			public void testClientListWhenSurnameIsEmptyAndAClientIsSelectedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText(ANOTHER_FIRSTNAME);
				surnameFormTxt.setText("");
				
				clientList.selectItem(0);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank surname and client is selected")
			public void testClientListWhenSurnameIsBlankAndAClientIsSelectedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText(ANOTHER_FIRSTNAME);
				surnameFormTxt.setText("  ");
				
				clientList.selectItem(0);
				
				renameBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank name and surname and client is selected")
			public void testClientListWhenBothNameAndSurnameAreBlankAndAClientIsSelectedShouldNotEnableRenameBtn() {
				addClientInList(A_CLIENT);
				nameFormTxt.setText("");
				surnameFormTxt.setText("   ");
				
				clientList.selectItem(0);
				
				renameBtn.requireDisabled();
			}
			////////////// Client List

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testRenameBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			enableButton(bookingSwingView.getRenameBtn());
			
			renameBtn.click();
			
			renameBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT))
				.renameClient(A_CLIENT, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		}
		////////////// Rename Button


		////////////// Remove Client Button
			////////////// Client List
			@Test @GUITest
			@DisplayName("Client is selected")
			public void testClientListWhenAClientIsSelectedShouldEnableRemoveClientBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				
				removeClientBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Remove client enabled and client is deselected")
			public void testClientListWhenRemoveClientBtnIsEnabledAndClientIsDeselectedShouldDisableRemoveClientBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				enableButton(bookingSwingView.getRemoveClientBtn());
				
				// unselectItem would be more clear but it doesn't trigger the list selection listener
				clientList.clearSelection();
				
				removeClientBtn.requireDisabled();
			}
			////////////// Client List

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testRemoveClientBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			enableButton(bookingSwingView.getRemoveClientBtn());
			
			removeClientBtn.click();
			
			removeClientBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT)).deleteClient(A_CLIENT);
		}
		////////////// Remove Client Button


		////////////// Add Reservation Button
			////////////// Day Form Text
			@Test @GUITest
			@DisplayName("Client selected, not blank year and month and text is typed")
			public void testDayFormTxtWhenAClientIsSelectedAndBothYearAndMonthAreNotBlankAndTextIsTypedShouldEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add reservation enabled and text is deleted")
			public void testDayFormTxtWhenAddReservationBtnIsEnabledAndTextIsDeletedShouldDisableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getAddReservationBtn());
				
				dayFormTxt.deleteText();
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client selected, not blank year and month and spaces are typed")
			public void testDayFormTxtWhenAClientIsSelectedAndBothYearAndMonthAreNotBlankAndSpacesAreTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(" ");
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and text is typed")
			public void testDayFormTxtWhenMonthIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("");
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and text is typed")
			public void testDayFormTxtWhenMonthIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("   ");
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and text is typed")
			public void testDayFormTxtWhenYearIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and text is typed")
			public void testDayFormTxtWhenYearIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("   ");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected and text is typed")
			public void testDayFormTxtWhenClientIsNotSelectedAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and month and text is typed")
			public void testDayFormTxtWhenBothYearAndMonthAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("  ");
				monthFormTxt.setText("");
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank year and text is typed")
			public void testDayFormTxtWhenClientIsNotSelectedAndYearIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText("  ");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank month and text is typed")
			public void testDayFormTxtWhenClientIsNotSelectedAndMonthIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("");
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank year and month and text is typed")
			public void testDayFormTxtWhenClientIsNotSelectedAndBothYearAndMonthAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText("");
				monthFormTxt.setText("  ");
				
				dayFormTxt.enterText(A_DAY);
				
				addReservationBtn.requireDisabled();
			}
			////////////// Day Form Text

			////////////// Month Form Text
			@Test @GUITest
			@DisplayName("Client selected, not blank year and day and text is typed")
			public void testMonthFormTxtWhenAClientIsSelectedAndBothYearAndDayAreNotBlankAndTextIsTypedShouldEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add reservation enabled and text is deleted")
			public void testMonthFormTxtWhenAddReservationBtnIsEnabledAndTextIsDeletedShouldDisableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getAddReservationBtn());
				
				monthFormTxt.deleteText();
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client selected, both not blank year and day and spaces are typed")
			public void testMonthFormTxtWhenAClientIsSelectedAndBothYearAndDayAreNotBlankAndSpacesAreTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText("  ");
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and text is typed")
			public void testMonthFormTxtWhenYearIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and text is typed")
			public void testMonthFormTxtWhenYearIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("  ");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and text is typed")
			public void testMonthFormTxtWhenDayIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText("");
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and text is typed")
			public void testMonthFormTxtWhenDayIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText("  ");
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected and text is typed")
			public void testMonthFormTxtWhenClientIsNotSelectedAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and day and text is typed")
			public void testMonthFormTxtWhenBothYearAndDayAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText("");
				dayFormTxt.setText("");
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank year and text is typed")
			public void testMonthFormTxtWhenClientIsNotSelectedAndYearIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText("  ");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank day and text is typed")
			public void testMonthFormTxtWhenClientIsNotSelectedAndDayIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText("  ");
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank year and day and text is typed")
			public void testMonthFormTxtWhenClientIsNotSelectedAndBothYearAndDayAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				yearFormTxt.setText("");
				dayFormTxt.setText("  ");
				
				monthFormTxt.enterText(A_MONTH);
				
				addReservationBtn.requireDisabled();
			}
			////////////// Month Form Text

			////////////// Year Form Text
			@Test @GUITest
			@DisplayName("Client selected, not blank month and day and text is typed")
			public void testYearFormTxtWhenAClientIsSelectedAndBothMonthAndDayAreNotBlankAndTextIsTypedShouldEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add Reservation enabled and text is deleted")
			public void testYearFormTxtWhenAddReservationBtnIsEnabledAndTextIsDeletedShouldDisableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getAddReservationBtn());
				
				yearFormTxt.deleteText();
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client selected, not blank month and day and spaces are typed")
			public void testYearFormTxtWhenAClientIsSelectedAndBothMonthAndDayAreNotBlankAndSpacesAreTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText("   ");
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and text is typed")
			public void testYearFormTxtWhenMonthIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and text is typed")
			public void testYearFormTxtWhenMonthIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText("   ");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and text is typed")
			public void testYearFormTxtWhenDayIsEmptyAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("");
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and text is typed")
			public void testYearFormTxtWhenDayIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("   ");
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected and text is typed")
			public void testYearFormTxtWhenClientIsNotSelectedAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and day and text is typed")
			public void testYearFormTxtWhenBothMonthAndDayAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				monthFormTxt.setText("  ");
				dayFormTxt.setText("  ");
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank month and text is typed")
			public void testYearFormTxtWhenClientIsNotSelectedAndMonthIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				monthFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank day and text is typed")
			public void testYearFormTxtWhenClientIsNotSelectedAndDayIsBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("");
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Client not selected, blank month and day and text is typed")
			public void testYearFormTxtWhenClientIsNotSelectedAndBothMonthAndDayAreBlankAndTextIsTypedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.clearSelection();
				monthFormTxt.setText("  ");
				dayFormTxt.setText("");
				
				yearFormTxt.enterText(A_YEAR);
				
				addReservationBtn.requireDisabled();
			}
			////////////// Year Form Text

			////////////// Client List
			@Test @GUITest
			@DisplayName("Not blank year, month and day and client is selected")
			public void testClientListWhenBothYearMonthAndDayAreNotBlankAndClientIsSelectedShouldEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Add reservation enabled and client is deselected")
			public void testClientListWhenAddReservationBtnIsEnabledAndClientIsDeselectedShouldDisableAddReservationBtn() {
				addClientInList(A_CLIENT);
				clientList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getAddReservationBtn());
				
				clientList.clearSelection();
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and day and client is selected")
			public void testClientListWhenYearIsEmptyAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText("");
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and client is selected")
			public void testClientListWhenYearIsBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText("   ");
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and client is selected")
			public void testClientListWhenMonthIsEmptyAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and client is selected")
			public void testClientListWhenMonthIsBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("  ");
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and client is selected")
			public void testClientListWhenDayIsEmptyAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("");
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and client is selected")
			public void testClientListWhenDayIsBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(" ");
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and month and client is selected")
			public void testClientListWhenBothYearAndMonthAreBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText("");
				monthFormTxt.setText(" ");
				dayFormTxt.setText(A_DAY);
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and day and client is selected")
			public void testClientListWhenBothYearAndDayAreBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText("  ");
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("");
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and day and client is selected")
			public void testClientListWhenBothMonthAndDayAreBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("");
				dayFormTxt.setText("   ");
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year, month and day and client is selected")
			public void testClientListWhenBothYearAndMonthAndDayAreBlankAndAClientIsSelectedShouldNotEnableAddReservationBtn() {
				addClientInList(A_CLIENT);
				yearFormTxt.setText("   ");
				monthFormTxt.setText("  ");
				dayFormTxt.setText("   ");
				
				clientList.selectItem(0);
				
				addReservationBtn.requireDisabled();
			}
			////////////// Client List

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testAddReservationBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			enableButton(bookingSwingView.getAddReservationBtn());
			
			addReservationBtn.click();
			
			addReservationBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT)).addReservation(A_CLIENT, A_DATE);
		}
		////////////// Add Reservation Button


		////////////// Reschedule Reservation Button
			////////////// Day Form Text
			@Test @GUITest
			@DisplayName("Reservation selected, not blank year and month and text is typed")
			public void testDayFormTxtWhenAReservationIsSelectedAndBothYearAndMonthAreNotBlankAndTextIsTypedShouldEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Reschedule enabled and text is deleted")
			public void testDayFormTxtWhenRescheduleBtnIsEnabledAndTextIsDeletedShouldDisableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(ANOTHER_DAY);
				enableButton(bookingSwingView.getRescheduleBtn());
				
				dayFormTxt.deleteText();
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation selected, not blank year and month and spaces are typed")
			public void testDayFormTxtWhenAReservationIsSelectedAndBothYearAndMonthAreNotBlankAndSpacesAreTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(" ");
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and text is typed")
			public void testDayFormTxtWhenYearIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText("");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and text is typed")
			public void testDayFormTxtWhenYearIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText("   ");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and text is typed")
			public void testDayFormTxtWhenMonthIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("");
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and text is typed")
			public void testDayFormTxtWhenMonthIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("   ");
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and text is typed")
			public void testDayFormTxtWhenReservationIsNotSelectedAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and month and text is typed")
			public void testDayFormTxtWhenBothYearAndMonthAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText("  ");
				monthFormTxt.setText("");
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank year and text is typed")
			public void testDayFormTxtWhenReservationIsNotSelectedAndYearIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText("");
				monthFormTxt.setText(A_MONTH);
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank month and text is typed")
			public void testDayFormTxtWhenReservationIsNotSelectedAndMonthIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText("  ");
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank year and month and text is typed")
			public void testDayFormTxtWhenReservationIsNotSelectedAndBothYearAndMonthAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText("");
				monthFormTxt.setText("  ");
				
				dayFormTxt.enterText(ANOTHER_DAY);
				
				rescheduleBtn.requireDisabled();
			}
			////////////// Day Form Text

			////////////// Month Form Text
			@Test @GUITest
			@DisplayName("Reservation selected, not blank year and day and text is typed")
			public void testMonthFormTxtWhenAReservationIsSelectedAndBothYearAndDayAreNotBlankAndTextIsTypedShouldEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Reschedule enabled and text is deleted")
			public void testMonthFormTxtWhenRescheduleBtnIsEnabledAndTextIsDeletedShouldDisableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getRescheduleBtn());
				
				monthFormTxt.deleteText();
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation selected, not blank year and day and spaces are typed")
			public void testMonthFormTxtWhenAReservationIsSelectedAndBothYearAndDayAreNotBlankAndSpacesAreTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText("  ");
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and text is typed")
			public void testMonthFormTxtWhenYearIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and text is typed")
			public void testMonthFormTxtWhenYearIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText("   ");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and text is typed")
			public void testMonthFormTxtWhenDayIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText("");
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and text is typed")
			public void testMonthFormTxtWhenDayIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(" ");
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and text is typed")
			public void testMonthFormTxtWhenReservationIsNotSelectedAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and day and text is typed")
			public void testMonthFormTxtWhenBothYearAndDayAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(" ");
				dayFormTxt.setText(" ");
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank year and text is typed")
			public void testMonthFormTxtWhenReservationIsNotSelectedAndYearIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText("  ");
				dayFormTxt.setText(A_DAY);
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank day and text is typed")
			public void testMonthFormTxtWhenReservationIsNotSelectedAndDayIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText(A_YEAR);
				dayFormTxt.setText("");
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected, blank year and day and text is typed")
			public void testMonthFormTxtWhenReservationIsNotSelectedAndBothYearAndDayAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				yearFormTxt.setText("");
				dayFormTxt.setText("   ");
				
				monthFormTxt.enterText(ANOTHER_MONTH);
				
				rescheduleBtn.requireDisabled();
			}
			////////////// Month Form Text

			////////////// Year Form Text
			@Test @GUITest
			@DisplayName("Reservation selected, not blank month and day and text is typed")
			public void testYearFormTxtWhenAReservationIsSelectedAndBothMonthAndDayAreNotBlankAndTextIsTypedShouldEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Reschedule enabled and text is deleted")
			public void testYearFormTxtWhenRescheduleBtnIsEnabledAndTextIsTypedShouldDisableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				enableButton(bookingSwingView.getRescheduleBtn());
				
				yearFormTxt.deleteText();
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation is selected, not blank month and day and spaces are typed")
			public void testYearFormTxtWhenAReservationIsSelectedAndBothMonthAndDayAreNotBlankAndSpacesAreTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText("   ");
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and text is typed")
			public void testYearFormTxtWhenMonthIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText("");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and text is typed")
			public void testYearFormTxtWhenMonthIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText("  ");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and text is typed")
			public void testYearFormTxtWhenDayIsEmptyAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("");
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and text is typed")
			public void testYearFormTxtWhenDayIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText("  ");
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and text is typed")
			public void testYearFormTxtWhenReservationIsNotSelectedAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and day and text is typed")
			public void testYearFormTxtWhenBothMonthAndDayAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				monthFormTxt.setText(" ");
				dayFormTxt.setText(" ");
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and blank month and text is typed")
			public void testYearFormTxtWhenReservationIsNotSelectedAndMonthIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				monthFormTxt.setText(" ");
				dayFormTxt.setText(A_DAY);
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and blank day and text is typed")
			public void testYearFormTxtWhenReservationIsNotSelectedAndDayIsBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				monthFormTxt.setText(A_MONTH);
				dayFormTxt.setText(" ");
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Reservation not selected and blank month and day and text is typed")
			public void testYearFormTxtWhenReservationIsNotSelectedAndBothMonthAndDayAreBlankAndTextIsTypedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.clearSelection();
				monthFormTxt.setText("  ");
				dayFormTxt.setText("  ");
				
				yearFormTxt.enterText(ANOTHER_YEAR);
				
				rescheduleBtn.requireDisabled();
			}
			////////////// Year Form Text

			////////////// Reservation List
			@Test @GUITest
			@DisplayName("Not blank year, month and day and reservation is selected")
			public void testReservationListWhenBothYearAndMonthAndDayAreNotBlankAndReservationIsSelectedShouldEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Reschedule enabled and reservation is deselected")
			public void testReservationListWhenRescheduleBtnIsEnabledAndReservationIsDeselectedShouldDisabledRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText(ANOTHER_DAY);
				enableButton(bookingSwingView.getRescheduleBtn());
				
				reservationList.clearSelection();
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty year and reservation is selected")
			public void testReservationListWhenYearIsEmptyAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText("");
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and reservation is selected")
			public void testReservationListWhenYearIsBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(" ");
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty month and reservation is selected")
			public void testReservationListWhenMonthIsEmptyAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText("");
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and reservation is selected")
			public void testReservationListWhenMonthIsBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText("   ");
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Empty day and reservation is selected")
			public void testReservationListWhenDayIsEmptyAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText("");
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank day and reservation is selected")
			public void testReservationListWhenDayIsBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText("  ");
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and month and reservation is selected")
			public void testReservationListWhenBothYearAndMonthAreBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText("  ");
				monthFormTxt.setText("  ");
				dayFormTxt.setText(ANOTHER_DAY);
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year and day and reservation is selected")
			public void testReservationListWhenBothYearAndDayAreBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText("   ");
				monthFormTxt.setText(ANOTHER_MONTH);
				dayFormTxt.setText("   ");
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank month and day and reservation is selected")
			public void testReservationListWhenBothMonthAndDayAreBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText(ANOTHER_YEAR);
				monthFormTxt.setText(" ");
				dayFormTxt.setText(" ");
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}

			@Test @GUITest
			@DisplayName("Blank year, month and day and reservation is selected")
			public void testReservationListWhenBothYearAndMonthAndDayAreBlankAndAReservationIsSelectedShouldNotEnableRescheduleBtn() {
				addReservationInList(A_RESERVATION);
				yearFormTxt.setText("");
				monthFormTxt.setText("");
				dayFormTxt.setText("");
				
				reservationList.selectItem(0);
				
				rescheduleBtn.requireDisabled();
			}
			////////////// Reservation List

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testRescheduleBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText(ANOTHER_DAY);
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			enableButton(bookingSwingView.getRescheduleBtn());
			
			rescheduleBtn.click();
			
			rescheduleBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT))
				.rescheduleReservation(A_RESERVATION, ANOTHER_DATE);
		}
		////////////// Reschedule Reservation Button

		////////////// Remove Reservation Button
			////////////// Reservation List
			@Test @GUITest
			@DisplayName("Reservation is selected")
			public void testReservationListWhenAReservationIsSelectedShouldEnableRemoveReservationBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				
				removeReservationBtn.requireEnabled();
			}

			@Test @GUITest
			@DisplayName("Remove reservation enabled and reservation is deselected")
			public void testReservationListWhenRemoveReservationBtnIsEnabledAndReservationIsDeselectedShouldDisableRemoveReservationBtn() {
				addReservationInList(A_RESERVATION);
				reservationList.selectItem(0);
				enableButton(bookingSwingView.getRemoveReservationBtn());
				
				// unselectItem would be more clear but it doesn't trigger the list selection listener
				reservationList.clearSelection();
				
				removeReservationBtn.requireDisabled();
			}
			////////////// Reservation List

		@Test @GUITest
		@DisplayName("Button is clicked")
		public void testRemoveReservationBtnWhenItIsClickedShouldDelegateToPresenterAndDisableIt() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			enableButton(bookingSwingView.getRemoveReservationBtn());
			
			removeReservationBtn.click();
			
			removeReservationBtn.requireDisabled();
			verify(bookingPresenter, timeout(TIMEOUT)).deleteReservation(A_RESERVATION);
		}
		////////////// Remove Reservation Button
	////////////// Tests on controls



	////////////// Tests on methods
		////////////// Tests for 'showAllClients'
		@Test @GUITest
		@DisplayName("Empty list")
		public void testShowAllClientsListOfClientsIsEmptyShouldNotShowAnyElements() {
			bookingSwingView.showAllClients(Collections.emptyList());
			
			assertThat(clientList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Not empty list")
		public void testShowAllClientsWhenListOfClientsIsNotEmptyShouldShowAllOfThem() {
			bookingSwingView.showAllClients(Arrays.asList(A_CLIENT, ANOTHER_CLIENT));
			
			assertThat(clientList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("Other clients of which one selected")
		public void testShowAllClientsWhenThereAreOtherClientsOfWhichOneIsSelectedShouldResetSelectionAndShowOnlyNewOnes() {
			Client oldClient = new Client("Giovanni", "De Chirico");
			
			addClientInList(A_CLIENT);
			addClientInList(oldClient);
			clientList.selectItem(0);
			
			bookingSwingView.showAllClients(Arrays.asList(A_CLIENT, ANOTHER_CLIENT));
			
			clientList.requireNoSelection();
			assertThat(clientList.contents())
				.doesNotContain(oldClient.toString())
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("Buttons enabled")
		public void testShowAllClientsWhenThereAreSpecificButtonsEnabledShouldDisableThem() {
			enableButton(bookingSwingView.getRenameBtn());
			enableButton(bookingSwingView.getRemoveClientBtn());
			enableButton(bookingSwingView.getAddReservationBtn());
			
			bookingSwingView.showAllClients(Collections.emptyList());
			
			renameBtn.requireDisabled();
			removeClientBtn.requireDisabled();
			addReservationBtn.requireDisabled();
		}
		////////////// Tests for 'showAllClients'


		////////////// Tests for 'showAllReservations'
		@Test @GUITest
		@DisplayName("Empty list")
		public void testShowAllReservationsWhenListOfReservationsIsEmptyShouldNotShowAnyElements() {
			bookingSwingView.showAllReservations(Collections.emptyList());
			
			assertThat(reservationList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Not empty list")
		public void testShowAllReservationsWhenListOfReservationsIsNotEmptyShouldShowAllOfThem() {
			bookingSwingView.showAllReservations(Arrays.asList(A_RESERVATION, ANOTHER_RESERVATION));
			
			assertThat(reservationList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("Other reservations of which one selected")
		public void testShowAllReservationsWhenThereAreOtherReservationsOfWhichOneIsSelectedShouldResetSelectionAndShowOnlyNewOnes() {
			Reservation oldReservation = new Reservation(
					UUID.randomUUID(), LocalDate.parse("2023-05-09"));
			
			addReservationInList(A_RESERVATION);
			addReservationInList(oldReservation);
			reservationList.selectItem(0);
			
			bookingSwingView.showAllReservations(Arrays.asList(A_RESERVATION, ANOTHER_RESERVATION));
			
			reservationList.requireNoSelection();
			assertThat(reservationList.contents())
				.doesNotContain(oldReservation.toString())
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("Buttons enabled")
		public void testShowAllReservationsWhenThereAreSpecificButtonsEnabledShouldDisableThem() {
			enableButton(bookingSwingView.getRescheduleBtn());
			enableButton(bookingSwingView.getRemoveReservationBtn());
			
			bookingSwingView.showAllReservations(Collections.emptyList());
			
			rescheduleBtn.requireDisabled();
			removeReservationBtn.requireDisabled();
		}
		////////////// Tests for 'showAllReservations'


		////////////// Tests for 'reservationAdded'
		@Test @GUITest
		@DisplayName("Empty list")
		public void testReservationAddedWhenReservationListIsEmptyShouldAddItToTheList() {
			bookingSwingView.reservationAdded(A_RESERVATION);
			
			assertThat(reservationList.contents())
				.hasSize(1)
				.containsExactly(A_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("Other reservations of which one selected")
		public void testReservationAddedWhenThereAreOtherReservationsOfWhichOneIsSelectedShouldNotChangeTheOthersAndTheSelection() {
			addReservationInList(A_RESERVATION);
			reservationList.selectItem(0);
			
			bookingSwingView.reservationAdded(ANOTHER_RESERVATION);
			
			assertThat(reservationList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_RESERVATION.toString(), ANOTHER_RESERVATION.toString());
			
			String[] selectedReservations = reservationList.selection();
			assertThat(selectedReservations).hasSize(1);
			assertThat(selectedReservations[0]).isEqualTo(A_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("Not empty forms and buttons enabled")
		public void testReservationAddedWhenReservationFormsAreNotEmptyAndRelatedButtonsAreEnabledShouldResetFormsAndDisableButtons() {
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			enableButton(bookingSwingView.getAddReservationBtn());
			enableButton(bookingSwingView.getRescheduleBtn());
			
			bookingSwingView.reservationAdded(A_RESERVATION);
			
			yearFormTxt.requireEmpty();
			monthFormTxt.requireEmpty();
			dayFormTxt.requireEmpty();
			addReservationBtn.requireDisabled();
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testReservationAddedWhenThereAreErrorMessagesShouldResetErrors() {
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.reservationAdded(A_RESERVATION);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'reservationAdded'


		////////////// Tests for 'clientAdded'
		@Test @GUITest
		@DisplayName("Empty list")
		public void testClientAddedWhenClientListIsEmptyShouldAddItToTheList() {
			bookingSwingView.clientAdded(A_CLIENT);
			
			assertThat(clientList.contents())
				.hasSize(1)
				.containsExactly(A_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("Other clients of which one is selected")
		public void testClientAddedWhenThereAreOtherClientsOfWhichOneIsSelectedShouldNotChangeTheOthersAndTheSelection() {
			addClientInList(A_CLIENT);
			clientList.selectItem(0);
			
			bookingSwingView.clientAdded(ANOTHER_CLIENT);
			
			assertThat(clientList.contents())
				.hasSize(2)
				.containsExactlyInAnyOrder(A_CLIENT.toString(), ANOTHER_CLIENT.toString());
			
			String[] selectedClients = clientList.selection();
			assertThat(selectedClients).hasSize(1);
			assertThat(selectedClients[0]).isEqualTo(A_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("Not empty forms and buttons enabled")
		public void testClientAddedWhenClientFormsAreNotEmptyAndRelatedButtonsAreEnabledShouldResetFormsAndDisableButtons() {
			nameFormTxt.setText(A_FIRSTNAME);
			surnameFormTxt.setText(A_LASTNAME);
			enableButton(bookingSwingView.getAddClientBtn());
			enableButton(bookingSwingView.getRenameBtn());
			
			bookingSwingView.clientAdded(A_CLIENT);
			
			nameFormTxt.requireEmpty();
			surnameFormTxt.requireEmpty();
			addClientBtn.requireDisabled();
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testClientAddedWhenThereAreErrorMessagesShouldResetErrors() {
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.clientAdded(A_CLIENT);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'clientAdded'


		////////////// Tests for 'reservationRemoved'
		@Test @GUITest
		@DisplayName("Only that reservation")
		public void testReservationRemovedWhenThereIsOnlyThatReservationShouldClearTheList() {
			addReservationInList(A_RESERVATION);
			
			bookingSwingView.reservationRemoved(A_RESERVATION);
			
			assertThat(reservationList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Others reservations")
		public void testReservationRemovedWhenThereAreOtherReservationsShouldRemoveItFromTheListAndNotChangeTheOthers() {
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			
			bookingSwingView.reservationRemoved(A_RESERVATION);
			
			assertThat(reservationList.contents()).containsExactly(ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("That reservation selected and buttons enabled")
		public void testReservationRemovedWhenThatReservationIsSelectedAndRelatedButtonsAreEnabledShouldClearTheSelectionAndDisableButtons() {
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			
			reservationList.selectItem(A_RESERVATION.toString());
			enableButton(bookingSwingView.getRescheduleBtn());
			enableButton(bookingSwingView.getRemoveReservationBtn());
			
			bookingSwingView.reservationRemoved(A_RESERVATION);
			
			reservationList.requireNoSelection();
			rescheduleBtn.requireDisabled();
			removeReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Another reservation selected and buttons enabled")
		public void testReservationRemovedWhenAnotherReservationIsSelectedAndRelatedButtonsAreEnabledShouldNotChangeTheSelectionAndNotDisableButtons() {
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			
			// Important: selecting an item that successes the removed one
			//            triggers the selection listener.
			reservationList.selectItem(ANOTHER_RESERVATION.toString());
			yearFormTxt.setText(ANOTHER_YEAR);
			monthFormTxt.setText(ANOTHER_MONTH);
			dayFormTxt.setText(ANOTHER_DAY);
			enableButton(bookingSwingView.getRescheduleBtn());
			enableButton(bookingSwingView.getRemoveReservationBtn());
			
			bookingSwingView.reservationRemoved(A_RESERVATION);
			
			String[] selectedReservations = reservationList.selection();
			assertThat(selectedReservations).hasSize(1);
			assertThat(selectedReservations[0]).isEqualTo(ANOTHER_RESERVATION.toString());
			rescheduleBtn.requireEnabled();
			removeReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testReservationRemovedWhenThereAreErrorMessagesShouldResetErrors() {
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.reservationRemoved(A_RESERVATION);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'reservationRemoved'


		////////////// Tests for 'clientRemoved'
		@Test @GUITest
		@DisplayName("Only that client")
		public void testClientRemovedWhenThereIsOnlyThatClientShouldClearTheList() {
			addClientInList(A_CLIENT);
			
			bookingSwingView.clientRemoved(A_CLIENT);
			
			assertThat(clientList.contents()).isEmpty();
		}

		@Test @GUITest
		@DisplayName("Others clients")
		public void testClientRemovedWhenThereAreOtherClientsShouldRemoveItFromTheListAndNotChangeTheOthers() {
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			
			bookingSwingView.clientRemoved(A_CLIENT);
			
			assertThat(clientList.contents()).containsExactly(ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("That client selected and buttons enabled")
		public void testClientRemovedWhenThatClientIsSelectedAndRelatedButtonsAreEnabledShouldClearTheSelectionAndDisableButtons() {
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			
			clientList.selectItem(ANOTHER_CLIENT.toString());
			enableButton(bookingSwingView.getRenameBtn());
			enableButton(bookingSwingView.getRemoveClientBtn());
			enableButton(bookingSwingView.getAddReservationBtn());
			
			bookingSwingView.clientRemoved(ANOTHER_CLIENT);
			
			clientList.requireNoSelection();
			renameBtn.requireDisabled();
			removeClientBtn.requireDisabled();
			addReservationBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Another client selected and buttons enabled")
		public void testClientRemovedWhenAnotherClientIsSelectedAndRelatedButtonsAreEnabledShouldNotChangeTheSelectionAndNotDisableButtons() {
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			
			// Important: selecting an item that successes the removed one
			//            triggers the selection listener.
			clientList.selectItem(ANOTHER_CLIENT.toString());
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			enableButton(bookingSwingView.getRenameBtn());
			enableButton(bookingSwingView.getRemoveClientBtn());
			enableButton(bookingSwingView.getAddReservationBtn());
			
			bookingSwingView.clientRemoved(A_CLIENT);
			
			String[] selectedClients = clientList.selection();
			assertThat(selectedClients).hasSize(1);
			assertThat(selectedClients[0]).isEqualTo(ANOTHER_CLIENT.toString());
			renameBtn.requireEnabled();
			removeClientBtn.requireEnabled();
			addReservationBtn.requireEnabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testClientRemovedWhenThereAreErrorMessagesShouldResetErrors() {
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.clientRemoved(A_CLIENT);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'clientRemoved'


		////////////// Tests for 'clientRenamed'
		@Test @GUITest
		@DisplayName("Only the old client")
		public void testClientRenamedWhenThereIsOnlyTheOldClientInTheListShouldRenameItAndNotAddNothingElse() {
			Client renamedClient = new Client("Giovanni", "De Chirico");
			addClientInList(A_CLIENT);
			
			bookingSwingView.clientRenamed(A_CLIENT, renamedClient);
			
			assertThat(clientList.contents())
				.doesNotContain(A_CLIENT.toString())
				.containsExactly(renamedClient.toString());
		}

		@Test @GUITest
		@DisplayName("Not only the old client")
		public void testClientRenamedWhenThereIsNotOnlyTheOldClientInTheListShouldRenameItNotChangeThePositionAndNotChangeTheOthers() {
			Client renamedClient = new Client("Giovanni", "De Chirico");
			
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			int initialIndex = bookingSwingView.getClientListModel().indexOf(A_CLIENT);
			
			bookingSwingView.clientRenamed(A_CLIENT, renamedClient);
			
			assertThat(clientList.contents())
				.doesNotContain(A_CLIENT.toString())
				.containsExactlyInAnyOrder(ANOTHER_CLIENT.toString(), renamedClient.toString());
			assertThat(bookingSwingView.getClientListModel().indexOf(renamedClient))
				.isEqualTo(initialIndex);
		}

		@Test @GUITest
		@DisplayName("That client selected")
		public void testClientRenamedWhenThatClientIsSelectedShouldSelectTheRenamedOne() {
			Client renamedClient = new Client("Giovanni", "De Chirico");
			
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			clientList.selectItem(A_CLIENT.toString());
			
			bookingSwingView.clientRenamed(A_CLIENT, renamedClient);
			
			String[] selectedClients = clientList.selection();
			assertThat(selectedClients).hasSize(1);
			assertThat(selectedClients[0]).isEqualTo(renamedClient.toString());
		}

		@Test @GUITest
		@DisplayName("Another client selected")
		public void testClientRenamedWhenAnotherClientIsSelectedShouldNotChangeTheSelection() {
			addClientInList(A_CLIENT);
			addClientInList(ANOTHER_CLIENT);
			clientList.selectItem(ANOTHER_CLIENT.toString());
			
			bookingSwingView.clientRenamed(A_CLIENT, new Client("Giovanni", "De Chirico"));
			
			String[] selectedClients = clientList.selection();
			assertThat(selectedClients).hasSize(1);
			assertThat(selectedClients[0]).isEqualTo(ANOTHER_CLIENT.toString());
		}

		@Test @GUITest
		@DisplayName("Not empty forms and buttons enabled")
		public void testClientRenamedWhenClientFormsAreNotEmptyAndRelatedButtonsAreEnabledShouldResetFormsAndDisableButtons() {
			addClientInList(A_CLIENT);
			
			nameFormTxt.setText(ANOTHER_FIRSTNAME);
			surnameFormTxt.setText(ANOTHER_LASTNAME);
			enableButton(bookingSwingView.getAddClientBtn());
			enableButton(bookingSwingView.getRenameBtn());
			
			bookingSwingView.clientRenamed(A_CLIENT, ANOTHER_CLIENT);
			
			nameFormTxt.requireEmpty();
			surnameFormTxt.requireEmpty();
			addClientBtn.requireDisabled();
			renameBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testClientRenamedWhenThereAreErrorMessagesShouldResetErrors() {
			addClientInList(A_CLIENT);
			
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.clientRenamed(A_CLIENT, ANOTHER_CLIENT);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'clientRenamed'


		////////////// Tests for 'reservationRescheduled'
		@Test @GUITest
		@DisplayName("Only the old reservation")
		public void testReservationRescheduledWhenThereIsOnlyTheOldReservationInTheListShouldRescheduleItAndNotAddNothingElse() {
			Reservation rescheduledReservation = new Reservation(
					A_CLIENT_UUID, LocalDate.parse("2024-03-21"));
			addReservationInList(A_RESERVATION);
			
			bookingSwingView.reservationRescheduled(A_RESERVATION, rescheduledReservation);
			
			assertThat(reservationList.contents())
				.doesNotContain(A_RESERVATION.toString())
				.containsExactly(rescheduledReservation.toString());
		}

		@Test @GUITest
		@DisplayName("Not only the old reservation")
		public void testReservationRescheduledWhenThereIsNotOnlyTheOldReservationInTheListShouldRescheduleItNotChangeThePositionAndNotChangeTheOthers() {
			Reservation rescheduledReservation = new Reservation(
					A_CLIENT_UUID, LocalDate.parse("2024-03-21"));
			
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			int initialIndex = bookingSwingView.getReservationListModel().indexOf(A_RESERVATION);
			
			bookingSwingView.reservationRescheduled(A_RESERVATION, rescheduledReservation);
			
			assertThat(reservationList.contents())
				.doesNotContain(A_RESERVATION.toString())
				.containsExactlyInAnyOrder(ANOTHER_RESERVATION.toString(), rescheduledReservation.toString());
			assertThat(bookingSwingView.getReservationListModel().indexOf(rescheduledReservation))
				.isEqualTo(initialIndex);
		}

		@Test @GUITest
		@DisplayName("That reservation selected")
		public void testReservationRescheduledWhenThatReservationIsSelectedShouldSelectTheRescheduledOne() {
			Reservation rescheduledReservation = new Reservation(
					A_CLIENT_UUID, LocalDate.parse("2024-03-21"));
			
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			reservationList.selectItem(A_RESERVATION.toString());
			
			bookingSwingView.reservationRescheduled(A_RESERVATION, rescheduledReservation);
			
			String[] selectedReservations = reservationList.selection();
			assertThat(selectedReservations).hasSize(1);
			assertThat(selectedReservations[0]).isEqualTo(rescheduledReservation.toString());
		}

		@Test @GUITest
		@DisplayName("Another reservation selected")
		public void testReservationRescheduledWhenAnotherReservationIsSelectedShouldNotChangeTheSelection() {
			addReservationInList(A_RESERVATION);
			addReservationInList(ANOTHER_RESERVATION);
			reservationList.selectItem(ANOTHER_RESERVATION.toString());
			
			bookingSwingView.reservationRescheduled(A_RESERVATION,
					new Reservation(A_CLIENT_UUID, LocalDate.parse("2024-03-21")));
			
			String[] selectedReservations = reservationList.selection();
			assertThat(selectedReservations).hasSize(1);
			assertThat(selectedReservations[0]).isEqualTo(ANOTHER_RESERVATION.toString());
		}

		@Test @GUITest
		@DisplayName("Not empty forms and button enabled")
		public void testReservationRescheduledWhenReservationFormsAreNotEmptyAndRelatedButtonsAreEnabledShouldResetFormsAndDisableButtons() {
			addReservationInList(A_RESERVATION);
			
			yearFormTxt.setText(A_YEAR);
			monthFormTxt.setText(A_MONTH);
			dayFormTxt.setText(A_DAY);
			enableButton(bookingSwingView.getAddReservationBtn());
			enableButton(bookingSwingView.getRescheduleBtn());
			
			bookingSwingView.reservationRescheduled(A_RESERVATION, ANOTHER_RESERVATION);
			
			yearFormTxt.requireEmpty();
			monthFormTxt.requireEmpty();
			dayFormTxt.requireEmpty();
			addReservationBtn.requireDisabled();
			rescheduleBtn.requireDisabled();
		}

		@Test @GUITest
		@DisplayName("Error messages")
		public void testReservationRescheduledWhenThereAreFormAndReservationErrorMessagesShouldResetErrors() {
			addReservationInList(A_RESERVATION);
			
			setTextLabel(bookingSwingView.getFormErrorMsgLbl(), FORMS_ERROR_MSG);
			setTextLabel(bookingSwingView.getOperationErrorMsgLbl(), OPERATIONS_ERROR_MSG);
			
			bookingSwingView.reservationRescheduled(A_RESERVATION, ANOTHER_RESERVATION);
			
			formErrorMsgLbl.requireText(" ");
			operationErrorMsgLbl.requireText(" ");
		}
		////////////// Tests for 'reservationRescheduled'


		///////////// Tests for error viewers
		@Test @GUITest
		@DisplayName("Test for 'showOperationError'")
		public void testShowOperationErrorShouldShowTheMessage() {
			bookingSwingView.showOperationError(OPERATIONS_ERROR_MSG);
			
			operationErrorMsgLbl.requireText(OPERATIONS_ERROR_MSG);
		}

		@Test @GUITest
		@DisplayName("Test for 'showFormError'")
		public void testShowFormErrorShouldShowTheMessage() {
			bookingSwingView.showFormError(FORMS_ERROR_MSG);
			
			formErrorMsgLbl.requireText(FORMS_ERROR_MSG);
		}
		///////////// Tests for error viewers

		private void setTextLabel(JLabel label, String text) {
			GuiActionRunner.execute(() -> label.setText(text));
		}
	////////////// Tests on methods

	private void enableButton(JButton button) {
		GuiActionRunner.execute(() -> button.setEnabled(true));
	}

	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
	}
}
