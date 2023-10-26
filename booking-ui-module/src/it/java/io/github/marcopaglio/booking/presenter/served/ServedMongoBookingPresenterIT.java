package io.github.marcopaglio.booking.presenter.served;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;

@DisplayName("Integration tests for ServedBookingPresenter and MongoDB")
class ServedMongoBookingPresenterIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final UUID A_CLIENT_UUID = UUID.fromString("03ee257d-f06d-47e9-8ef0-78b18ee03fe9");
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("7b565e00-59cd-4de8-b70a-a08842317d5b");

	private static final String A_DATE = "2023-04-24";
	private static final LocalDate A_LOCALDATE = LocalDate.parse(A_DATE);
	private static final UUID A_RESERVATION_UUID = UUID.fromString("a2014dc9-7f77-4aa2-a3ce-0559736a7670");
	private static final String ANOTHER_DATE = "2023-09-05";
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse(ANOTHER_DATE);
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.fromString("f9e3dd0c-c3ff-4d4f-a3d1-108fcb3a697d");

	private static String mongoHost = System.getProperty("mongo.host", "localhost");
	private static int mongoPort = Integer.parseInt(System.getProperty("mongo.port", "27017"));

	private static MongoClient mongoClient;

	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

	private TransactionMongoManager transactionMongoManager;
	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionalBookingService transactionalBookingService;

	@Mock
	private BookingView view;

	@Mock
	private ClientValidator clientValidator;

	@Mock
	private ReservationValidator reservationValidator;

	private ServedBookingPresenter presenter;

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	private AutoCloseable closeable;

	@BeforeAll
	static void setupClient() throws Exception {
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

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionMongoManager = new TransactionMongoManager(mongoClient, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		transactionalBookingService = new TransactionalBookingService(transactionMongoManager);
		
		presenter = new ServedBookingPresenter(view, transactionalBookingService,
				clientValidator, reservationValidator);
		
		// make sure we always start with a clean database
		database.drop();
		
		client = new Client(A_FIRSTNAME, A_LASTNAME);
		another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@AfterAll
	static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Integration tests for 'allClients'")
	class AllClientsIT {

		@Test
		@DisplayName("No clients in repository")
		void testAllClientsWhenThereAreNoClientsInRepositoryShouldCallViewWithEmptyList() {
			presenter.allClients();
			
			verify(view).showAllClients(Collections.emptyList());
		}

		@Test
		@DisplayName("Several clients in repository")
		void testAllClientsWhenThereAreSeveralClientsInRepositoryShouldCallViewWithClientsAsList() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
			
			presenter.allClients();
			
			verify(view).showAllClients(Arrays.asList(client, another_client));
		}
	}

	@Nested 
	@DisplayName("Integration tests for 'allReservations'")
	class AllReservationsIT {

		@Test
		@DisplayName("No reservations in repository")
		void testAllReservationsWhenThereAreNoReservationsInRepositoryShouldCallViewWithEmptyList() {
			presenter.allReservations();
			
			verify(view).showAllReservations(Collections.emptyList());
		}

		@Test
		@DisplayName("Several reservations in repository")
		void testAllReservationsWhenThereAreSeveralReservationsInRepositoryShouldCallViewWithReservationsAsList() {
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
			
			presenter.allReservations();
			
			verify(view).showAllReservations(Arrays.asList(reservation, another_reservation));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'deleteClient'")
	class DeleteClientIT {

		@Test
		@DisplayName("Client is in repository")
		void testDeleteClientWhenClientIsInRepositoryShouldRemoveAndNotifyView() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
			addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
			
			presenter.deleteClient(client);
			
			verify(view).showAllReservations(Arrays.asList(another_reservation));
			verify(view).clientRemoved(client);
			assertThat(readAllReservationsFromDatabase())
				.filteredOn(r -> Objects.equals(r.getClientId(), A_CLIENT_UUID)).isEmpty();
			assertThat(readAllClientsFromDatabase())
				.doesNotContain(client)
				.containsExactly(another_client);
		}

		@Test
		@DisplayName("Client is not in repository")
		void testDeleteClientWhenClientIsNotInRepositoryShouldShowErrorAndUpdateView() {
			client.setId(A_CLIENT_UUID);
			
			presenter.deleteClient(client);
			
			verify(view).showOperationError(client.toString() + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}
	}

	@Nested
	@DisplayName("Integration tests for 'deleteReservation'")
	class DeleteReservationIT {

		@Test
		@DisplayName("Reservation is in repository")
		void testDeleteReservationWhenReservationIsInRepositoryShouldRemoveAndNotifyView() {
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
			
			presenter.deleteReservation(reservation);
			
			verify(view).reservationRemoved(reservation);
			
			assertThat(readAllReservationsFromDatabase())
				.doesNotContain(reservation)
				.containsExactly(another_reservation);
		}

		@Test
		@DisplayName("Reservation is not in repository")
		void testDeleteReservationWhenReservationIsNotInRepositoryShouldShowErrorAndUpdateView() {
			reservation.setId(A_RESERVATION_UUID);
			
			presenter.deleteReservation(reservation);
			
			verify(view).showOperationError(reservation.toString() + " no longer exists.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Collections.emptyList());
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addClient'")
	class AddClientIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(clientValidator.validateFirstName(A_FIRSTNAME)).thenReturn(A_FIRSTNAME);
			when(clientValidator.validateLastName(A_LASTNAME)).thenReturn(A_LASTNAME);
		}

		@Test
		@DisplayName("Client is new")
		void testAddClientWhenClientIsNewShouldValidateItAndInsertAndNotifyView() {
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			verify(clientValidator).validateFirstName(A_FIRSTNAME);
			verify(clientValidator).validateLastName(A_LASTNAME);
			verify(view).clientAdded(client);
			
			assertThat(readAllClientsFromDatabase()).containsExactly(client);
		}

		@Test
		@DisplayName("Client is not new")
		void testAddClientWhenClientIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			presenter.addClient(A_FIRSTNAME, A_LASTNAME);
			
			verify(view).showOperationError("A client named " + A_FIRSTNAME
					+ " " + A_LASTNAME + " has already been made.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Arrays.asList(client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'addReservation'")
	class AddReservationIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(reservationValidator.validateClientId(A_CLIENT_UUID))
				.thenReturn(A_CLIENT_UUID);
			when(reservationValidator.validateDate(A_DATE)).thenReturn(A_LOCALDATE);
		}

		@Test
		@DisplayName("Reservation is new")
		void testAddReservationWhenReservationIsNewShouldValidateItAndInsertAndNotifyView() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			presenter.addReservation(client, A_DATE);
			
			verify(reservationValidator).validateClientId(A_CLIENT_UUID);
			verify(reservationValidator).validateDate(A_DATE);
			verify(view).reservationAdded(reservation);
			
			assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
		}

		@Test
		@DisplayName("Reservation is not new")
		void testAddReservationWhenReservationIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			presenter.addReservation(client, A_DATE);
			
			verify(view).showOperationError(
					"A reservation on " + A_DATE + " has already been made.");
			verify(view).showAllReservations(Arrays.asList(reservation));
			verify(view).showAllClients(Arrays.asList(client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'renameClient'")
	class RenameClientIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(clientValidator.validateFirstName(ANOTHER_FIRSTNAME))
				.thenReturn(ANOTHER_FIRSTNAME);
			when(clientValidator.validateLastName(ANOTHER_LASTNAME))
				.thenReturn(ANOTHER_LASTNAME);
		}

		@Test
		@DisplayName("Renamed client is new")
		void testRenameClientWhenRenamedClientIsNewShouldValidateItAndRenameAndNotifyView() {
			Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			verify(clientValidator).validateFirstName(ANOTHER_FIRSTNAME);
			verify(clientValidator).validateLastName(ANOTHER_LASTNAME);
			verify(view).clientRenamed(client, renamedClient);
			
			assertThat(readAllClientsFromDatabase())
				.contains(renamedClient)
				.doesNotContain(client);
		}

		@Test
		@DisplayName("Renamed client is not new")
		void testRenameClientWhenRenamedClientIsNotNewShouldShowErrorAndUpdateView() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
			
			presenter.renameClient(client, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			
			verify(view).showOperationError("A client named " + ANOTHER_FIRSTNAME
					+ " " + ANOTHER_LASTNAME + " has already been made.");
			verify(view).showAllReservations(Collections.emptyList());
			verify(view).showAllClients(Arrays.asList(client, another_client));
		}
	}

	@Nested
	@DisplayName("Integration tests for 'rescheduleReservation'")
	class RescheduleReservationIT {

		@BeforeEach
		void stubbingValidator() throws Exception {
			when(reservationValidator.validateDate(ANOTHER_DATE))
				.thenReturn(ANOTHER_LOCALDATE);
		}

		@Test
		@DisplayName("Rescheduled reservation is new")
		void testRescheduleReservationWhenRescheduledReservationIsNewShouldValidateItAndRescheduleAndNotifyView() {
			Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			verify(reservationValidator).validateDate(ANOTHER_DATE);
			verify(view).reservationRescheduled(reservation, rescheduledReservation);
			
			assertThat(readAllReservationsFromDatabase())
				.contains(rescheduledReservation)
				.doesNotContain(reservation);
		}

		@Test
		@DisplayName("Rescheduled reservation is not new")
		void testRescheduleReservationWhenRescheduledReservationIsNotNewShouldShowErrorAndUpdateView() {
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
			
			presenter.rescheduleReservation(reservation, ANOTHER_DATE);
			
			verify(view).showOperationError(
					"A reservation on " + ANOTHER_DATE + " has already been made.");
			// updateAll
			verify(view).showAllReservations(Arrays.asList(reservation, another_reservation));
			verify(view).showAllClients(Collections.emptyList());
		}
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
