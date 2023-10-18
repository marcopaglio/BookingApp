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

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("92403799-acf3-45c9-896e-ef57b1f3be3b");

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

	private Client client;
	private Reservation reservation;

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
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
	}

	@AfterClass
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Test
	@DisplayName("Integration tests for 'AddClientBtn'")
	public void testAddClientBtnWhenClientIsNewShouldInsert() {
		nameFormTxt.setText(A_FIRSTNAME);
		surnameFormTxt.setText(A_LASTNAME);
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).containsExactly(client);
	}

	@Test
	@DisplayName("Integration tests for 'RenameBtn'")
	public void testRenameBtnWhenThereIsNoClientWithTheSameNewNamesShouldRenameWithoutChangingId() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		String anotherFirstName = "Maria";
		String anotherLastName = "De Lucia";
		
		nameFormTxt.setText(anotherFirstName);
		surnameFormTxt.setText(anotherLastName);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		renameBtn.click();
		
		List<Client> clientsInDB = readAllClientsFromDatabase();
		assertThat(clientsInDB).doesNotContain(client).hasSize(1);
		Client clientInDB = clientsInDB.get(0);
		assertThat(clientInDB.getFirstName()).isEqualTo(anotherFirstName);
		assertThat(clientInDB.getLastName()).isEqualTo(anotherLastName);
		assertThat(clientInDB.getId()).isEqualTo(A_CLIENT_UUID);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveClientBtn'")
	public void testRemoveClientBtnWhenClientExistsWithAnExistingReservationShouldRemove() {
		addTestClientToDatabase(client, A_CLIENT_UUID);
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRemoveClientBtn());
		
		removeClientBtn.click();
		
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
		assertThat(readAllReservationsFromDatabase())
			.filteredOn(r -> r.getClientId().equals(A_CLIENT_UUID)).isEmpty();
	}

	@Test
	@DisplayName("Integration tests for 'AddReservationBtn'")
	public void testAddReservationBtnWhenReservationIsNewAndAssociatedClientExistsShouldInsert() {
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
	@DisplayName("Integration tests for 'RescheduleBtn'")
	public void testRescheduleBtnWhenThereIsNoReservationInTheSameNewDateShouldRescheduleWithoutChangingId() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		String anotherYear = "2023";
		String anotherMonth = "09";
		String anotherDay = "05";
		
		yearFormTxt.setText(anotherYear);
		monthFormTxt.setText(anotherMonth);
		dayFormTxt.setText(anotherDay);
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRescheduleBtn());
		
		rescheduleBtn.click();
		
		List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
		assertThat(reservationsInDB).doesNotContain(reservation).hasSize(1);
		Reservation reservationInDB = reservationsInDB.get(0);
		assertThat(reservationInDB.getDate()).isEqualTo(
				LocalDate.parse(anotherYear + "-" + anotherMonth + "-" + anotherDay));
		assertThat(reservationInDB.getId()).isEqualTo(A_RESERVATION_UUID);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveReservationBtn'")
	public void testRemoveReservationBtnWhenReservationExistsShouldRemove() {
		addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
		
		addReservationInList(reservation);
		reservationList.selectItem(0);
		enableButton(bookingSwingView.getRemoveReservationBtn());
		
		removeReservationBtn.click();
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
	}

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
