package io.github.marcopaglio.booking.view.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.JButton;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
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
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("4ed2e7a3-fa50-4889-bfe4-b833ddfc4f0b");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("4e506279-a3f3-4808-abb3-d055a09af078");

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final String A_DATE = A_YEAR + "-" + A_MONTH + "-" + A_DAY;
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("92403799-acf3-45c9-896e-ef57b1f3be3b");
	private static final String ANOTHER_YEAR = "2023";
	private static final String ANOTHER_MONTH = "09";
	private static final String ANOTHER_DAY = "05";
	private static final String ANOTHER_DATE = ANOTHER_YEAR + "-" + ANOTHER_MONTH + "-" + ANOTHER_DAY;
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.fromString("547b1ffc-0015-4ad8-a789-17528afd397a");

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

	private TransactionMongoManager transactionMongoManager;
	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionalBookingService transactionalBookingService;
	private RestrictedReservationValidator restrictedReservationValidator;
	private RestrictedClientValidator restrictedClientValidator;

	private ServedBookingPresenter servedMongoBookingPresenter;
	private BookingSwingView bookingSwingView;

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

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	@BeforeClass
	public static void setupClient() throws Exception {
		mongoClient = getClient(System.getProperty("mongo.connectionString", "mongodb://localhost:27017"));
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
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionMongoManager = new TransactionMongoManager(mongoClient, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionMongoManager);
		restrictedClientValidator = new RestrictedClientValidator();
		restrictedReservationValidator = new RestrictedReservationValidator();
		
		// make sure we always start with a clean database
		database.drop();
		
		GuiActionRunner.execute(() -> {
			bookingSwingView = new BookingSwingView();
			servedMongoBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					transactionalBookingService, restrictedClientValidator,
					restrictedReservationValidator);
			
			bookingSwingView.setBookingPresenter(servedMongoBookingPresenter);
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
		another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
	}

	@AfterClass
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	////////////// Integration tests for 'AddClientBtn'
	@Test
	@DisplayName("Client is new")
	public void testAddClientBtnWhenClientIsNewShouldInsert() {
		nameFormTxt.setText(A_FIRSTNAME);
		surnameFormTxt.setText(A_LASTNAME);
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}

	@Test
	@DisplayName("Client already exists")
	public void testAddClientBtnWhenClientAlreadyExistsShouldNotInsert() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		nameFormTxt.setText(A_FIRSTNAME);
		surnameFormTxt.setText(A_LASTNAME);
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		List<Client> clientsInDB = readAllClientsFromDatabase();
		assertThat(clientsInDB).containsExactly(client);
		assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
	}

	@Test
	@DisplayName("Name is not valid")
	public void testAddClientBtnWhenNameIsNotValidShouldNotInsert() {
		nameFormTxt.setText("!!!!!");
		surnameFormTxt.setText(A_LASTNAME);
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).isEmpty();
	}

	@Test
	@DisplayName("Surname is not valid")
	public void testAddClientWhenSurnameIsNotValidShouldShowErrorAndNotInsert() {
		nameFormTxt.setText(A_FIRSTNAME);
		surnameFormTxt.setText("D3 Lucia");
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).isEmpty();
	}
	////////////// Integration tests for 'AddClientBtn'


	////////////// Integration tests for 'RenameBtn'
	@Test
	@DisplayName("A same name client doesn't exist")
	public void testRenameBtnWhenThereIsNoClientWithTheSameNewNamesShouldRenameWithoutChangingId() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		nameFormTxt.setText(ANOTHER_FIRSTNAME);
		surnameFormTxt.setText(ANOTHER_LASTNAME);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		renameBtn.click();
		
		List<Client> clientsInDB = readAllClientsFromDatabase();
		assertThat(clientsInDB)
			.doesNotContain(client)
			.hasSize(1);
		Client clientInDB = clientsInDB.get(0);
		assertThat(clientInDB.getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
		assertThat(clientInDB.getLastName()).isEqualTo(ANOTHER_LASTNAME);
		assertThat(clientInDB.getId()).isEqualTo(A_CLIENT_UUID);
	}

	@Test
	@DisplayName("A same name client already exists")
	public void testRenameBtnWhenThereIsAlreadyAClientWithSameNewNamesShouldNotRename() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
		
		nameFormTxt.setText(ANOTHER_FIRSTNAME);
		surnameFormTxt.setText(ANOTHER_LASTNAME);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		renameBtn.click();
		
		assertThat(readAllClientsFromDatabase())
			.containsExactlyInAnyOrder(client, another_client)
			.filteredOn((c) -> c.getId().equals(A_CLIENT_UUID)).containsOnly(client);
	}

	@Test
	@DisplayName("Name is not valid")
	public void testRenameBtnWhenNameIsNotValidShouldNotRename() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		nameFormTxt.setText("M@ria");
		surnameFormTxt.setText(ANOTHER_LASTNAME);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}

	@Test
	@DisplayName("Surname is not valid")
	public void testRenameBtnWhenSurnameIsNotValidShouldNotRename() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		nameFormTxt.setText(ANOTHER_FIRSTNAME);
		surnameFormTxt.setText("?????");
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}
	////////////// Integration tests for 'RenameBtn'


	////////////// Integration tests for 'RemoveClientBtn'
	@Test
	@DisplayName("Client exists with existing reservation")
	public void testRemoveClientBtnWhenClientExistsWithAnExistingReservationShouldRemove() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
		addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
		
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRemoveClientBtn());
		
		removeClientBtn.click();
		
		assertThat(readAllReservationsFromDatabase())
			.filteredOn((r) -> r.getClientId() == A_CLIENT_UUID).isEmpty();
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
	}

	@Test
	@DisplayName("Client doesn't exist")
	public void testRemoveClientBtnWhenClientDoesNotExistShouldChangeNothing() {
		client.setId(A_CLIENT_UUID);
		addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
		addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
		
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRemoveClientBtn());
		
		removeClientBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).containsExactly(another_reservation);
		assertThat(readAllClientsFromDatabase()).containsExactly(another_client);
	}
	////////////// Integration tests for 'RemoveClientBtn'


	////////////// Integration tests for 'AddReservationBtn'
	@Test
	@DisplayName("Reservation is new and client exists")
	public void testAddReservationBtnWhenReservationIsNewAndAssociatedClientExistsShouldInsertAndReturnWithId() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		yearFormTxt.setText(A_YEAR);
		monthFormTxt.setText(A_MONTH);
		dayFormTxt.setText(A_DAY);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getAddReservationBtn());
		
		addReservationBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
	}

	@Test
	@DisplayName("Reservation already exists")
	public void testAddReservationBtnWhenReservationAlreadyExistsShouldNotInsert() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
		yearFormTxt.setText(A_YEAR);
		monthFormTxt.setText(A_MONTH);
		dayFormTxt.setText(A_DAY);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getAddReservationBtn());
		
		addReservationBtn.click();
		
		List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
		assertThat(reservationsInDB).containsExactly(reservation);
		assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
	}

	@Test
	@DisplayName("Date is not valid")
	public void testAddReservationWhenDateIsNotValidShouldNotInsert() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		
		yearFormTxt.setText(A_YEAR);
		monthFormTxt.setText(A_MONTH);
		dayFormTxt.setText("32");
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getAddReservationBtn());
		
		addReservationBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).isEmpty();
	}
	////////////// Integration tests for 'AddReservationBtn'


	////////////// Integration tests for 'RescheduleBtn'
	@Test
	@DisplayName("A same date reservation doesn't exist")
	public void testRescheduleBtnWhenThereIsNoReservationInTheSameNewDateShouldRescheduleWithoutChangingId() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
		yearFormTxt.setText(ANOTHER_YEAR);
		monthFormTxt.setText(ANOTHER_MONTH);
		dayFormTxt.setText(ANOTHER_DAY);
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRescheduleBtn());
		
		rescheduleBtn.click();
		
		List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
		assertThat(reservationsInDB)
			.doesNotContain(reservation)
			.hasSize(1);
		Reservation reservationInDB = reservationsInDB.get(0);
		assertThat(reservationInDB.getDate()).isEqualTo(ANOTHER_LOCALDATE);
		assertThat(reservationInDB.getId()).isEqualTo(A_RESERVATION_UUID);
	}

	@Test
	@DisplayName("A same date reservation already exists")
	public void testRescheduleBtnWhenThereIsAlreadyAReservationInTheSameNewDateShouldNotReschedule() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
		
		yearFormTxt.setText(ANOTHER_YEAR);
		monthFormTxt.setText(ANOTHER_MONTH);
		dayFormTxt.setText(ANOTHER_DAY);
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRescheduleBtn());
		
		rescheduleBtn.click();
		
		assertThat(readAllReservationsFromDatabase())
			.containsExactlyInAnyOrder(reservation, another_reservation)
			.filteredOn((r) -> r.getId().equals(A_RESERVATION_UUID)).containsOnly(reservation);
	}

	@Test
	@DisplayName("New date is not valid")
	public void testRescheduleBtnWhenNewDateIsNotValidShouldNotReschedule() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
		yearFormTxt.setText("2.23");
		monthFormTxt.setText("0J");
		dayFormTxt.setText("32");
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRescheduleBtn());
		
		rescheduleBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
	}
	////////////// Integration tests for 'RescheduleBtn'


	////////////// Integration tests for 'RemoveReservationBtn'
	@Test
	@DisplayName("Reservation exists")
	public void testRemoveReservationBtnWhenReservationExistsShouldRemove() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
		
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRemoveReservationBtn());
		
		removeReservationBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
	}

	@Test
	@DisplayName("Reservation doesn't exist")
	public void testRemoveReservationBtnWhenReservationDoesNotExistShouldChangeNothing() {
		addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
		
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRemoveReservationBtn());
		
		removeReservationBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).containsExactly(another_reservation);
	}
	////////////// Integration tests for 'RemoveReservationBtn'


	private void enableButton(JButton button) {
		GuiActionRunner.execute(() -> button.setEnabled(true));
	}

	private void addClientInList(Client client) {
		GuiActionRunner.execute(() -> bookingSwingView.getClientListModel().addElement(client));
	}

	private void addReservationInList(Reservation reservation) {
		GuiActionRunner.execute(() -> bookingSwingView.getReservationListModel().addElement(reservation));
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

	public void addTestClientToDatabase(Client client, UUID id) {
		client.setId(id);
		clientCollection.insertOne(client);
	}

	public void addTestReservationToDatabase(Reservation reservation, UUID id) {
		reservation.setId(id);
		reservationCollection.insertOne(reservation);
	}
}
