package io.github.marcopaglio.booking.view.swing;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import javax.swing.JButton;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests for BookingSwingView, ServedBookingPresenter and PostgreSQL")
@RunWith(GUITestRunner.class)
public class PostgresModelSwingViewServedPresenterIT extends AssertJSwingJUnitTestCase {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";

	private static EntityManagerFactory emf;

	private TransactionPostgresManager transactionPostgresManager;
	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionalBookingService transactionalBookingService;
	private RestrictedReservationValidator restrictedReservationValidator;
	private RestrictedClientValidator restrictedClientValidator;

	private ServedBookingPresenter servedPostgresBookingPresenter;
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
	public static void setupEmf() throws Exception {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "IntegrationTest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@Override
	protected void onSetUp() throws Exception {
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionPostgresManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionPostgresManager);
		restrictedClientValidator = new RestrictedClientValidator();
		restrictedReservationValidator = new RestrictedReservationValidator();
		
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		GuiActionRunner.execute(() -> {
			bookingSwingView = new BookingSwingView();
			servedPostgresBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					transactionalBookingService, restrictedClientValidator,
					restrictedReservationValidator);
			
			bookingSwingView.setBookingPresenter(servedPostgresBookingPresenter);
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
	}

	@AfterClass
	public static void closeEmf() throws Exception {
		emf.close();
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
		addTestClientToDatabase(client);
		UUID existingId = client.getId();
		
		nameFormTxt.setText(A_FIRSTNAME);
		surnameFormTxt.setText(A_LASTNAME);
		enableButton(bookingSwingView.getAddClientBtn());
		
		addClientBtn.click();
		
		List<Client> clientsInDB = readAllClientsFromDatabase();
		assertThat(clientsInDB).containsExactly(client);
		assertThat(clientsInDB.get(0).getId()).isEqualTo(existingId);
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
		addTestClientToDatabase(client);
		UUID client_id = client.getId();
		
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
		assertThat(clientInDB.getId()).isEqualTo(client_id);
	}

	@Test
	@DisplayName("A same name client already exists")
	public void testRenameBtnWhenThereIsAlreadyAClientWithSameNewNamesShouldNotRename() {
		addTestClientToDatabase(client);
		addTestClientToDatabase(another_client);
		UUID client_id = client.getId();
		
		nameFormTxt.setText(ANOTHER_FIRSTNAME);
		surnameFormTxt.setText(ANOTHER_LASTNAME);
		addClientInList(client);
		clientList.selectItem(0);
		enableButton(bookingSwingView.getRenameBtn());
		
		renameBtn.click();
		
		assertThat(readAllClientsFromDatabase())
			.containsExactlyInAnyOrder(client, another_client)
			.filteredOn((c) -> c.getId().equals(client_id)).containsOnly(client);
	}

	@Test
	@DisplayName("Name is not valid")
	public void testRenameBtnWhenNameIsNotValidShouldNotRename() {
		addTestClientToDatabase(client);
		
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
		addTestClientToDatabase(client);
		
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
		EntityManager em = emf.createEntityManager();
		List<Client> clientsInDB = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
		em.close();
		return clientsInDB;
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		EntityManager em = emf.createEntityManager();
		List<Reservation> reservationsInDB = em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
		em.close();
		return reservationsInDB;
	}

	private void addTestClientToDatabase(Client client) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(client);
		em.getTransaction().commit();
		em.close();
	}

	private void addTestReservationToDatabase(Reservation reservation) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(reservation);
		em.getTransaction().commit();
		em.close();
	}
}
