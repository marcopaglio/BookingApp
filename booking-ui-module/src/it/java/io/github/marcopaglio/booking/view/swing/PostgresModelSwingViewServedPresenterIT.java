package io.github.marcopaglio.booking.view.swing;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
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
	private static final long TIMEOUT = 5000;

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("60fb39bc-501e-4350-ac9b-4638521feb4e");

	private static final String A_YEAR = "2022";
	private static final String A_MONTH = "04";
	private static final String A_DAY = "24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_YEAR + "-" + A_MONTH + "-" + A_DAY);

	private static EntityManagerFactory emf;

	private ServedBookingPresenter servedPostgresBookingPresenter;

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
	public static void setupEmf() throws Exception {
		System.setProperty("db.host", System.getProperty("postgres.host", "localhost"));
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "ITandE2ETest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@Override
	protected void onSetUp() throws Exception {
		TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
		ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
		ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
		TransactionPostgresManager transactionPostgresManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		TransactionalBookingService transactionalBookingService = new TransactionalBookingService(transactionPostgresManager);
		RestrictedClientValidator restrictedClientValidator = new RestrictedClientValidator();
		RestrictedReservationValidator restrictedReservationValidator = new RestrictedReservationValidator();
		
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			BookingSwingView bookingSwingView = new BookingSwingView();
			servedPostgresBookingPresenter = new ServedBookingPresenter(bookingSwingView,
					transactionalBookingService, restrictedClientValidator,
					restrictedReservationValidator);
			
			bookingSwingView.setBookingPresenter(servedPostgresBookingPresenter);
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
	public static void closeEmf() throws Exception {
		emf.close();
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
		
		addTestClientToDatabase(client);
		UUID client_id = client.getId();
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
		assertThat(clientInDB.getId()).isEqualTo(client_id);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveClientBtn'")
	public void testRemoveClientBtnWhenClientExistsWithAnExistingReservationShouldRemove() {
		addTestClientToDatabase(client);
		UUID client_id = client.getId();
		updateClientList();
		reservation.setClientId(client_id);
		addTestReservationToDatabase(reservation);
		updateReservationList();
		
		clientList.selectItem(0);
		
		removeClientBtn.click();
		
		pause(untilClientListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllClientsFromDatabase()).doesNotContain(client);
		assertThat(readAllReservationsFromDatabase())
			.filteredOn(r -> Objects.equals(r.getClientId(), client_id)).isEmpty();
	}

	@Test
	@DisplayName("Integration tests for 'AddReservationBtn'")
	public void testAddReservationBtnWhenReservationIsNewAndAssociatedClientExistsShouldInsert() {
		addTestClientToDatabase(client);
		updateClientList();
		reservation.setClientId(client.getId());
		
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
		
		addTestReservationToDatabase(reservation);
		UUID reservation_id = reservation.getId();
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
		assertThat(reservationInDB.getId()).isEqualTo(reservation_id);
	}

	@Test
	@DisplayName("Integration tests for 'RemoveReservationBtn'")
	public void testRemoveReservationBtnWhenReservationExistsShouldRemove() {
		addTestReservationToDatabase(reservation);
		updateReservationList();
		
		reservationList.selectItem(0);
		
		removeReservationBtn.click();
		
		pause(untilReservationListContainsNothing, timeout(TIMEOUT));
		
		assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
	}


	// List updater
	private void updateClientList() {
		GuiActionRunner.execute(() -> servedPostgresBookingPresenter.allClients());
	}

	private void updateReservationList() {
		GuiActionRunner.execute(() -> servedPostgresBookingPresenter.allReservations());
	}

	// database modifiers
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
}
