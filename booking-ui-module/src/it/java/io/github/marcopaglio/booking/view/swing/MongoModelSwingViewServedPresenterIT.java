package io.github.marcopaglio.booking.view.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;

@DisplayName("Integration tests for BookingSwingView, ServedBookingPresenter and MongoDB")
@RunWith(GUITestRunner.class)
public class MongoModelSwingViewServedPresenterIT extends AssertJSwingJUnitTestCase {
	private static final long TIMEOUT = 5000;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("4ed2e7a3-fa50-4889-bfe4-b833ddfc4f0b");

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("92403799-acf3-45c9-896e-ef57b1f3be3b");

	private static final String MONGODB_NAME = "ITandE2ETest_db";
	private static String mongoHost = System.getProperty("mongo.host", "localhost");
	private static int mongoPort = Integer.parseInt(System.getProperty("mongo.port", "27017"));

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

	private ServedBookingPresenter servedMongoBookingPresenter;

	private FrameFixture window;

	private JTextComponentFixture nameFormTxt;
	private JTextComponentFixture surnameFormTxt;
	private JTextComponentFixture yearFormTxt;
	private JTextComponentFixture monthFormTxt;
	private JTextComponentFixture dayFormTxt;
	private JButtonFixture addReservationBtn;
	private JButtonFixture addClientBtn;
	private JButtonFixture renameBtn;
	private JButtonFixture rescheduleBtn;
	private JButtonFixture removeClientBtn;
	private JButtonFixture removeReservationBtn;
	private JListFixture clientList;
	private JListFixture reservationList;

	private Client client;
	private Reservation reservation;

	// pause conditions
	private Condition untilClientListContainsClients = new Condition("Client list to contain clients") {
		@Override
		public boolean test() {
			return clientList.contents().length != 0;
		}
	};

	private Condition untilClientListContainsNothing = new Condition("Client list to contain nothing") {
		@Override
		public boolean test() {
			return clientList.contents().length == 0;
		}
	};

	private Condition untilNameFormsAreReset = new Condition("Name forms to be reset") {
		@Override
		public boolean test() {
			return nameFormTxt.text().isEmpty() && 
					surnameFormTxt.text().isEmpty();
		}
	};

	private Condition untilReservationListContainsReservations = new Condition("Reservation list to contain reservations") {
		@Override
		public boolean test() {
			return reservationList.contents().length != 0;
		}
	};

	private Condition untilReservationListContainsNothing = new Condition("Reservation list to contain nothing") {
		@Override
		public boolean test() {
			return reservationList.contents().length == 0;
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

	@BeforeClass
	public static void setupClient() throws Exception {
		mongoClient = getClient(String.format("mongodb://%s:%d", mongoHost, mongoPort));
		database = mongoClient.getDatabase(MONGODB_NAME);
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
		TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
		ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
		ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
		TransactionMongoManager transactionMongoManager = new TransactionMongoManager(mongoClient, MONGODB_NAME,
				transactionHandlerFactory, clientRepositoryFactory, reservationRepositoryFactory);
		
		TransactionalBookingService transactionalBookingService = new TransactionalBookingService(transactionMongoManager);
		RestrictedClientValidator restrictedClientValidator = new RestrictedClientValidator();
		RestrictedReservationValidator restrictedReservationValidator = new RestrictedReservationValidator();
		
		// make sure we always start with a clean database
		database.drop();
		
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			BookingSwingView bookingSwingView = new BookingSwingView();
			servedMongoBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					transactionalBookingService, restrictedClientValidator,
					restrictedReservationValidator);
			
			bookingSwingView.setBookingPresenter(servedMongoBookingPresenter);
			return bookingSwingView;
		}));
		window.show();
		
		// text fields
		nameFormTxt = window.textBox("nameFormTxt");
		surnameFormTxt = window.textBox("surnameFormTxt");
		yearFormTxt = window.textBox("yearFormTxt");
		monthFormTxt = window.textBox("monthFormTxt");
		dayFormTxt = window.textBox("dayFormTxt");
		
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
		
		// entities
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
	}

	@AfterClass
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Test
	@DisplayName("Integration tests for 'AddClientBtn'")
	public void testAddClientBtnWhenClientIsNewShouldInsert() {
		nameFormTxt.enterText(A_FIRSTNAME);
		surnameFormTxt.enterText(A_LASTNAME);
		
		addClientBtn.click();
		
		pause(untilClientListContainsClients, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}

	@Test
	@DisplayName("Integration tests for 'RenameBtn'")
	public void testRenameBtnWhenThereIsNoClientWithTheSameNewNamesShouldRenameWithoutChangingId() {
		String anotherFirstName = "Maria";
		String anotherLastName = "De Lucia";
		
		addTestClientToDatabase(client, A_CLIENT_UUID);
		updateClientList();
		
		clientList.selectItem(0);
		nameFormTxt.enterText(anotherFirstName);
		surnameFormTxt.enterText(anotherLastName);
		
		renameBtn.click();
		
		pause(untilNameFormsAreReset, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase())
			.doesNotContain(client)
			.hasSize(1);
		Client clientInDB = readAllClientsFromDatabase().get(0);
		assertThat(clientInDB.getFirstName()).isEqualTo(anotherFirstName);
		assertThat(clientInDB.getLastName()).isEqualTo(anotherLastName);
		assertThat(clientInDB.getId()).isEqualTo(A_CLIENT_UUID);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveClientBtn'")
	public void testRemoveClientBtnWhenClientExistsWithAnExistingReservationShouldRemove() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		updateClientList();
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		updateReservationList();
		
		clientList.selectItem(0);
		
		removeClientBtn.click();
		
		pause(untilClientListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
		assertThat(readAllReservationsFromDatabase())
			.filteredOn(r -> Objects.equals(r.getClientId(), A_CLIENT_UUID)).isEmpty();
	}

	@Test
	@DisplayName("Integration tests for 'AddReservationBtn'")
	public void testAddReservationBtnWhenReservationIsNewAndAssociatedClientExistsShouldInsert() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		updateClientList();
		
		clientList.selectItem(0);
		yearFormTxt.enterText(A_YEAR);
		monthFormTxt.enterText(A_MONTH);
		dayFormTxt.enterText(A_DAY);
		
		addReservationBtn.click();
		
		pause(untilReservationListContainsReservations, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
	}

	@Test
	@DisplayName("Integration tests for 'RescheduleBtn'")
	public void testRescheduleBtnWhenThereIsNoReservationInTheSameNewDateShouldRescheduleWithoutChangingId() {
		String anotherYear = "2023";
		String anotherMonth = "09";
		String anotherDay = "05";
		LocalDate anotherDate = LocalDate.parse(anotherYear + "-" + anotherMonth + "-" + anotherDay);
		
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		updateReservationList();
		
		reservationList.selectItem(0);
		yearFormTxt.enterText(anotherYear);
		monthFormTxt.enterText(anotherMonth);
		dayFormTxt.enterText(anotherDay);
		
		rescheduleBtn.click();
		
		pause(untilDateFormsAreReset, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase())
			.doesNotContain(reservation)
			.hasSize(1);
		Reservation reservationInDB = readAllReservationsFromDatabase().get(0);
		assertThat(reservationInDB.getDate()).isEqualTo(anotherDate);
		assertThat(reservationInDB.getId()).isEqualTo(A_RESERVATION_UUID);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveReservationBtn'")
	public void testRemoveReservationBtnWhenReservationExistsShouldRemove() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		updateReservationList();
		
		reservationList.selectItem(0);
		
		removeReservationBtn.click();
		
		pause(untilReservationListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
	}


	// List updater
	private void updateClientList() {
		GuiActionRunner.execute(() -> servedMongoBookingPresenter.allClients());
	}

	private void updateReservationList() {
		GuiActionRunner.execute(() -> servedMongoBookingPresenter.allReservations());
	}

	// database modifiers
	public void addTestClientToDatabase(Client client, UUID id) {
		client.setId(id);
		clientCollection.insertOne(client);
	}

	public void addTestReservationToDatabase(Reservation reservation, UUID id) {
		reservation.setId(id);
		reservationCollection.insertOne(reservation);
	}

	private List<Client> readAllClientsFromDatabase() {
		return StreamSupport
				.stream(clientCollection.find().spliterator(), false)
				.collect(Collectors.toList());
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		return StreamSupport
				.stream(reservationCollection.find().spliterator(), false)
				.collect(Collectors.toList());
	}
}
