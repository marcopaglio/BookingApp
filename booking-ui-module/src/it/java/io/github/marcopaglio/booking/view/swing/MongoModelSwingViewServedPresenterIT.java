package io.github.marcopaglio.booking.view.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;

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
import org.assertj.swing.fixture.JLabelFixture;
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
	private static final UUID A_CLIENT_UUID = UUID.fromString("9686dee5-1675-48ee-b5c3-81f164a9cf04");
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("7b565e00-59cd-4de8-b70a-a08842317d5b");

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

	private Client client, another_client;

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
		
		// entities
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		//reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		//reservation.setId(A_RESERVATION_UUID);
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
