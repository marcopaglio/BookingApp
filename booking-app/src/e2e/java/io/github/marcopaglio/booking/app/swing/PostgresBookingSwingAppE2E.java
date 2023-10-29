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

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("End-to-end tests for BookingSwingApp using PostgreSQL")
@RunWith(GUITestRunner.class)
public class PostgresBookingSwingAppE2E extends AssertJSwingJUnitTestCase {
	private static final int TIMEOUT = 5000;
	private static final int DEFAULT_NUM_OF_CLIENTS = 1;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("500775ab-0ef3-4893-999f-f70098670722");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String INVALID_FIRSTNAME = "Mari@";
	private static final String INVALID_LASTNAME = "De_Lucia";

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("bfe395cd-08f9-46bd-8080-269d62f0d366");

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
	private JLabelFixture formErrorMsgLbl;
	private JLabelFixture operationErrorMsgLbl;
	private JButtonFixture addClientBtn;
	private JListFixture clientList;
	private JListFixture reservationList;

	private Client client;
	private Reservation reservation;

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
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		addTestClientToDatabase(client, A_CLIENT_UUID);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
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
		
		// error labels
		formErrorMsgLbl = window.label("formErrorMsgLbl");
		operationErrorMsgLbl = window.label("operationErrorMsgLbl");
		
		// buttons
		addClientBtn = window.button(JButtonMatcher.withText("Add Client"));
		
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
		
		assertThat(formErrorMsgLbl.text()).contains("name");
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
		
		assertThat(formErrorMsgLbl.text()).contains("surname");
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



	private void addTestClientToDatabase(Client client, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + CLIENT_TABLE_DB +
				"(" + ID_POSTGRESQL + ", "+ FIRSTNAME_DB + ", " + LASTNAME_DB + ") " +
				"VALUES ('" + id + "', '" + client.getFirstName() + "', '" + client.getLastName() + "')")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	private void addTestReservationToDatabase(Reservation reservation, UUID id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("INSERT INTO " + RESERVATION_TABLE_DB +
				"(" + ID_POSTGRESQL + ", "+ CLIENTID_DB + ", " + DATE_DB + ") " +
				"VALUES ('" + id + "', '" + reservation.getClientId() + "', '" + reservation.getDate() + "')")
			.executeUpdate();
		em.getTransaction().commit();
		em.close();
	}
}