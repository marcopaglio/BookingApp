package io.github.marcopaglio.booking.app.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.model.BaseEntity.ID_MONGODB;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Client.FIRSTNAME_DB;
import static io.github.marcopaglio.booking.model.Client.LASTNAME_DB;
import static io.github.marcopaglio.booking.model.Reservation.DATE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.LocalDate;
import java.util.Arrays;
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
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

@DisplayName("End-to-end tests for BookingSwingApp usign MongoDB")
@RunWith(GUITestRunner.class)
public class MongoBookingSwingAppE2E extends AssertJSwingJUnitTestCase {
	private static final int TIMEOUT = 5000;
	private static final int DEFAULT_NUM_OF_CLIENTS = 1;
	private static final int DEFAULT_NUM_OF_RESERVATIONS = 1;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final String NAME_THEN_SURNAME_OR_SURNAME_THEN_NAME_REGEX =
			".*" + A_FIRSTNAME + ".*" + A_LASTNAME + ".*"
			+ "|" +
			".*" + A_LASTNAME + ".*" + A_FIRSTNAME + ".*";
	private static final UUID A_CLIENT_UUID = UUID.fromString("b8a88ad6-739e-4df5-b3ea-82832a56843a");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("62d3c7b4-b519-46da-ab3a-4934238323b9");
	private static final String INVALID_FIRSTNAME = "Mari4";
	private static final String INVALID_LASTNAME = "De_Lucia";

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final String A_DATE = A_YEAR + "-" + A_MONTH + "-" + A_DAY;
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("6de5f28a-541a-4699-8377-e0eaa9d2b13e");
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final String ANOTHER_DATE = ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY;
	private static final String INVALID_YEAR = "2O23";
	private static final String INVALID_MONTH = "13";
	private static final String INVALID_DAY = ".5";
	private static final String INVALID_DATE = INVALID_YEAR + "-" + INVALID_MONTH + "-" + INVALID_DAY;

	private static final String DBMS = "MONGO";
	private static String mongoHost = System.getProperty("mongo.host", "localhost");
	private static int mongoPort = Integer.parseInt(System.getProperty("mongo.port", "27017"));

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

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
	private JListFixture clientList;
	private JListFixture reservationList;

	@BeforeClass
	public static void setupClient() throws Exception {
		mongoClient = getClient(String.format("mongodb://%s:%d", mongoHost, mongoPort));
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
		clientCollection = database.getCollection(CLIENT_TABLE_DB, Client.class);
		reservationCollection = database.getCollection(RESERVATION_TABLE_DB, Reservation.class);
	}

	private static MongoClient getClient(String connectionString) {
		// define the CodecProvider for POJO classes
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.conventions(Arrays.asList(ANNOTATION_CONVENTION, USE_GETTERS_FOR_SETTERS))
				.automatic(true)
				.build();
		
		// define the CodecRegistry as codecs and other related information
		CodecRegistry pojoCodecRegistry =
				fromRegistries(getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider));
		
		// configure the MongoClient for using the CodecRegistry
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(connectionString))
				.uuidRepresentation(STANDARD)
				.codecRegistry(pojoCodecRegistry)
				.build();
		return MongoClients.create(settings);
	}

	@Override
	protected void onSetUp() throws Exception {
		// make sure we always start with a clean database
		database.drop();
		
		// add entities to the database
		addTestClientToDatabase(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);
		addTestReservationToDatabase(A_CLIENT_UUID, A_LOCALDATE, A_RESERVATION_UUID);
		
		// start the Swing application
		application("io.github.marcopaglio.booking.app.swing.BookingSwingApp")
			.withArgs(
					"--dbms=" + DBMS,
					"--host=" + mongoHost,
					"--port=" + mongoPort
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
		
		// lists
		clientList = window.list("clientList");
		reservationList = window.list("reservationList");
	}

	@AfterClass
	public static void closeClient() throws Exception {
		mongoClient.close();
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
	@DisplayName("Adding client is successful")
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
			.anySatisfy(e -> assertThat(e).doesNotContain(A_FIRSTNAME, A_LASTNAME))
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
	@DisplayName("Renamed client is homonymic")
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


	public void addTestClientToDatabase(String name, String surname, UUID id) {
		Client client = new Client(name, surname);
		client.setId(id);
		ClientSession session = mongoClient.startSession();
		clientCollection.createIndex(session,
				Indexes.descending(FIRSTNAME_DB, LASTNAME_DB), new IndexOptions().unique(true));
		clientCollection.insertOne(session, client);
		session.close();
	}

	private void removeTestClientFromDatabase(UUID id) {
		ClientSession session = mongoClient.startSession();
		clientCollection.deleteOne(session, Filters.eq(ID_MONGODB, id));
		session.close();
	}

	public void addTestReservationToDatabase(UUID clientId, LocalDate localDate, UUID id) {
		Reservation reservation = new Reservation(clientId, localDate);
		reservation.setId(id);
		ClientSession session = mongoClient.startSession();
		reservationCollection.createIndex(session,
				Indexes.descending(DATE_DB), new IndexOptions().unique(true));
		reservationCollection.insertOne(session, reservation);
		session.close();
	}
}