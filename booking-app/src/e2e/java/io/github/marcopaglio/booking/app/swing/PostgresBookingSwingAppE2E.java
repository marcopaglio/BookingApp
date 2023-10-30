package io.github.marcopaglio.booking.app.swing;

import static io.github.marcopaglio.booking.model.BaseEntity.ID_POSTGRESQL;
import static io.github.marcopaglio.booking.model.Client.FIRSTNAME_DB;
import static io.github.marcopaglio.booking.model.Client.LASTNAME_DB;
import static io.github.marcopaglio.booking.model.Reservation.CLIENTID_DB;
import static io.github.marcopaglio.booking.model.Reservation.DATE_DB;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.LocalDate;
import java.util.Map;
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
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("End-to-end tests for BookingSwingApp using PostgreSQL")
@RunWith(GUITestRunner.class)
public class PostgresBookingSwingAppE2E extends AssertJSwingJUnitTestCase {
	private static final int TIMEOUT = 5000;
	private static final int DEFAULT_NUM_OF_CLIENTS = 1;
	private static final int DEFAULT_NUM_OF_RESERVATIONS = 1;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final String NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX =
			".*" + A_FIRSTNAME + ".*" + A_LASTNAME + ".*"
			+ "|" +
			".*" + A_LASTNAME + ".*" + A_FIRSTNAME + ".*";
	private static final UUID A_CLIENT_UUID = UUID.fromString("500775ab-0ef3-4893-999f-f70098670722");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("c5014376-5a67-4c17-a1da-0b0b92af5711");
	private static final String INVALID_FIRSTNAME = "Mari4";
	private static final String INVALID_LASTNAME = "De_Lucia";

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final String A_DATE = A_YEAR + "-" + A_MONTH + "-" + A_DAY;
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("bfe395cd-08f9-46bd-8080-269d62f0d366");
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final String ANOTHER_DATE = ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY;
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.fromString("17f600a7-af1e-41d1-8dd0-f4d00dbda750");
	private static final String INVALID_YEAR = "2O23";
	private static final String INVALID_MONTH = "13";
	private static final String INVALID_DAY = ".5";
	private static final String INVALID_DATE = INVALID_YEAR + "-" + INVALID_MONTH + "-" + INVALID_DAY;

	private static final String DBMS = "POSTGRES";

	private static String postgresHost = System.getProperty("postgres.host", "localhost");
	private static int postgresPort = Integer.parseInt(System.getProperty("postgres.port", "5432"));
	private static String postgresName = System.getProperty("postgres.name", "ITandE2ETest_db");
	private static String postgresUser = System.getProperty("postgres.user", "postgres-it-e2e");
	private static String postgresPswd = System.getProperty("postgres.pswd", "postgres-it-e2e");

	private static EntityManagerFactory emf;

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
	private JListFixture clientList;
	private JListFixture reservationList;

	@BeforeClass
	public static void setupEmf() throws Exception {
		String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
				postgresHost, postgresPort, postgresName);
		emf = Persistence.createEntityManagerFactory("postgres-app", Map.of(
				"jakarta.persistence.jdbc.url", jdbcUrl,
				"jakarta.persistence.jdbc.user", postgresUser,
				"jakarta.persistence.jdbc.password", postgresPswd));
	}

	@Override
	protected void onSetUp() throws Exception {
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		// add entities to the database
		addTestClientToDatabase(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);
		addTestReservationToDatabase(A_CLIENT_UUID, A_LOCALDATE, A_RESERVATION_UUID);
		
		// start the Swing application
		application("io.github.marcopaglio.booking.app.swing.BookingSwingApp")
			.withArgs(
					"--dbms=" + DBMS,
					"--host=" + postgresHost,
					"--port=" + postgresPort,
					"--name=" + postgresName,
					"--user=" + postgresUser,
					"--pswd=" + postgresPswd
			).start();
		
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
		
		// lists
		clientList = window.list("clientList");
		reservationList = window.list("reservationList");
	}

	@AfterClass
	public static void closeEmf() throws Exception {
		emf.close();
	}


	@Test @GUITest
	@DisplayName("Application is started")
	public void testApplicationWhenIsStartedShouldShowAllDatabaseElements() {
		assertThat(clientList.contents())
			.anySatisfy(e -> assertThat(e).contains(A_FIRSTNAME, A_LASTNAME));
		
		assertThat(reservationList.contents())
			.anySatisfy(e -> assertThat(e).contains(A_LOCALDATE.toString()));
	}


	////////////// Add client
	@Test @GUITest
	@DisplayName("Adding client succeeds")
	public void testAddClientWhenIsSuccessfulShouldShowTheNewClient() {
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		addClientBtn.click();
		
		pause(
			new Condition("Client list to contain different number of clients than the default ones") {
				@Override
				public boolean test() {
					return clientList.contents().length != DEFAULT_NUM_OF_CLIENTS;
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(clientList.contents())
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
	}

	@Test @GUITest
	@DisplayName("Name is invalid")
	public void testAddClientWhenNameIsInvalidShouldShowFormError() {
		nameFormTxt.enterText(INVALID_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		addClientBtn.click();
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_FIRSTNAME);
	}

	@Test @GUITest
	@DisplayName("Surname is invalid")
	public void testAddClientWhenSurnameIsInvalidShouldShowFormError() {
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(INVALID_LASTNAME);
		
		addClientBtn.click();
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Adding client fails")
	public void testAddClientWhenIsAFailureShouldShowAnOperationError() {
		nameFormTxt.enterText(A_FIRSTNAME);
		surnameFormTxt.enterText(A_LASTNAME);
		
		addClientBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
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
		
		pause(
			new Condition("Name forms to be reset") {
				@Override
				public boolean test() {
					return nameFormTxt.text().isEmpty() && 
							surnameFormTxt.text().isEmpty();
				}
			}
		, timeout(TIMEOUT));
		
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
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_FIRSTNAME);
	}

	@Test @GUITest
	@DisplayName("New surname is invalid")
	public void testRenameClientWhenNewSurnameIsInvalidShouldShowFormError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(INVALID_LASTNAME);
		
		renameBtn.click();
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Renamed client would be homonymic")
	public void testRenameClientWhenThereIsHomonymyShouldShowAnOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		addTestClientToDatabase(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME, ANOTHER_CLIENT_UUID);
		
		renameBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
	}

	@Test @GUITest
	@DisplayName("Renaming client fails")
	public void testRenameClientWhenIsAFailureShouldShowAnOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		nameFormTxt.enterText(ANOTHER_FIRSTNAME);
		surnameFormTxt.enterText(ANOTHER_LASTNAME);
		
		removeTestClientFromDatabase(A_CLIENT_UUID);
		
		renameBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_FIRSTNAME, A_LASTNAME);
	}
	////////////// Rename client


	////////////// Add reservation
	@Test @GUITest
	@DisplayName("Adding reservation is successful")
	public void testAddReservationWhenIsSuccessfulShouldShowTheNewReservation() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		addReservationBtn.click();
		
		pause(
			new Condition("Reservation list to contain different number of reservations than the default ones") {
				@Override
				public boolean test() {
					return reservationList.contents().length != DEFAULT_NUM_OF_RESERVATIONS;
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(reservationList.contents())
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_DATE));
	}

	@Test @GUITest
	@DisplayName("Date is invalid")
	public void testAddReservationWhenDateIsInvalidShouldShowFormError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(INVALID_YEAR);
		monthFormTxt.enterText(INVALID_MONTH);
		dayFormTxt.enterText(INVALID_DAY);
		
		addReservationBtn.click();
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_DATE);
	}

	@Test @GUITest
	@DisplayName("Selected client is wrong")
	public void testAddReservationWhenSelectedClientIsWrongShouldShowOperationError() {
		clientList.selectItem(Pattern.compile(NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		removeTestClientFromDatabase(A_CLIENT_UUID);
		
		addReservationBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
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
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_DATE);
	}
	////////////// Add reservation


	////////////// Reschedule reservation
	@Test @GUITest
	@DisplayName("Rescheduling reservation succeeds")
	public void testRescheduleReservationWhenIsSuccessfulShouldShowChanges() {
		reservationList.selectItem(Pattern.compile(".*" + A_DATE + ".*"));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		rescheduleBtn.click();
		
		pause(
			new Condition("Date forms to be reset") {
				@Override
				public boolean test() {
					return yearFormTxt.text().isEmpty() && 
							monthFormTxt.text().isEmpty() && 
							dayFormTxt.text().isEmpty();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(reservationList.contents())
			.allSatisfy(e -> assertThat(e).doesNotContain(A_DATE))
			.anySatisfy(e -> assertThat(e).contains(ANOTHER_DATE));
	}

	@Test @GUITest
	@DisplayName("New date is invalid")
	public void testRescheduleReservationWhenNewDateIsInvalidShouldShowFormError() {
		reservationList.selectItem(Pattern.compile(".*" + A_DATE + ".*"));
		yearFormTxt.enterText(INVALID_YEAR);
		monthFormTxt.enterText(INVALID_MONTH);
		dayFormTxt.enterText(INVALID_DAY);
		
		rescheduleBtn.click();
		
		pause(
			new Condition("Form error to contain a message") {
				@Override
				public boolean test() {
					return !formErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(formErrorMsgLbl.text()).contains(INVALID_DATE);
	}

	@Test @GUITest
	@DisplayName("Rescheduled reservation would be simultaneous")
	public void testRescheduleReservationWhenThereIsSimultaneityShouldShowAnOperationError() {
		reservationList.selectItem(Pattern.compile(".*" + A_DATE + ".*"));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		addTestReservationToDatabase(A_CLIENT_UUID, ANOTHER_LOCALDATE, ANOTHER_RESERVATION_UUID);
		
		rescheduleBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(ANOTHER_DATE);
	}

	@Test @GUITest
	@DisplayName("Rescheduling reservation fails")
	public void testRescheduleReservationWhenIsAFailureShouldShowAnOperationError() {
		reservationList.selectItem(Pattern.compile(".*" + A_DATE + ".*"));
		yearFormTxt.enterText(ANOTHER_YEAR);
		monthFormTxt.enterText(ANOTHER_MONTH);
		dayFormTxt.enterText(ANOTHER_DAY);
		
		removeTestReservationFromDatabase(A_RESERVATION_UUID);
		
		rescheduleBtn.click();
		
		pause(
			new Condition("Operation error to contain a message") {
				@Override
				public boolean test() {
					return !operationErrorMsgLbl.text().isBlank();
				}
			}
		, timeout(TIMEOUT));
		
		assertThat(operationErrorMsgLbl.text()).contains(A_DATE);
	}
	////////////// Reschedule reservation


	private void addTestClientToDatabase(String name, String surname, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + CLIENT_TABLE_DB +
				"(" + ID_POSTGRESQL + ", "+ FIRSTNAME_DB + ", " + LASTNAME_DB + ") " +
				"VALUES ('" + id + "', '" + name + "', '" + surname + "')")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	private void removeTestClientFromDatabase(UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("DELETE FROM " + CLIENT_TABLE_DB +
				" WHERE " + ID_POSTGRESQL + "='" + id +"'")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	private void addTestReservationToDatabase(UUID clientId, LocalDate localDate, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + RESERVATION_TABLE_DB +
				"(" + ID_POSTGRESQL + ", "+ CLIENTID_DB + ", " + DATE_DB + ") " +
				"VALUES ('" + id + "', '" + clientId + "', '" + localDate + "')")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	private void removeTestReservationFromDatabase(UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("DELETE FROM " + RESERVATION_TABLE_DB +
				" WHERE " + ID_POSTGRESQL + "='" + id +"'")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}
}