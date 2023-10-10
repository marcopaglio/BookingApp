package io.github.marcopaglio.booking.service.transactional;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.CLIENT_ALREADY_EXISTS_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.CLIENT_NOT_FOUND_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.RESERVATION_ALREADY_EXISTS_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.RESERVATION_NOT_FOUND_ERROR_MSG;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager;

@DisplayName("Integration tests of TransactionalBookingService and MongoDB")
class TransactionalMongoBookingServiceIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final UUID A_CLIENT_UUID = UUID.fromString("03ee257d-f06d-47e9-8ef0-78b18ee03fe9");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final UUID A_RESERVATION_UUID = UUID.fromString("a2014dc9-7f77-4aa2-a3ce-0559736a7670");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("7b565e00-59cd-4de8-b70a-a08842317d5b");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-09-05");
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.fromString("f9e3dd0c-c3ff-4d4f-a3d1-108fcb3a697d");

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

	private static TransactionHandlerFactory transactionHandlerFactory;
	private static ClientRepositoryFactory clientRepositoryFactory;
	private static ReservationRepositoryFactory reservationRepositoryFactory;
	private static TransactionMongoManager transactionManager;

	private TransactionalBookingService service;

	@BeforeAll
	static void setupCollaborators() throws Exception {
		mongoClient = getClient(System.getProperty("mongo.connectionString", "mongodb://localhost:27017"));
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
		clientCollection = database.getCollection(CLIENT_TABLE_DB, Client.class);
		reservationCollection = database.getCollection(RESERVATION_TABLE_DB, Reservation.class);
		
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionManager = new TransactionMongoManager(mongoClient, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
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
		database.drop();
		
		service = new TransactionalBookingService(transactionManager);
	}

	@AfterAll
	static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Methods using only ClientMongoRepository")
	class ClientMongoRepositoryIT {

		@BeforeEach
		void initClients() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		}

		@Nested
		@DisplayName("Integration tests for 'findAllClients'")
		class FindAllClientsIT {

			@Test
			@DisplayName("No clients to retrieve")
			void testFindAllClientsWhenThereAreNoClientsToRetrieveShouldReturnEmptyList() {
				assertThat(service.findAllClients()).isEmpty();
			}

			@Test
			@DisplayName("Several clients to retrieve")
			void testFindAllClientsWhenThereAreSeveralClientsToRetrieveShouldReturnClientsAsList() {
				addTestClientToDatabase(client, A_CLIENT_UUID);
				addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
				
				assertThat(service.findAllClients()).isEqualTo(Arrays.asList(client, another_client));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findClient'")
		class FindClientIT {

			@Test
			@DisplayName("Client exists")
			void testFindClientWhenClientExistsShouldReturnTheClient() {
				addTestClientToDatabase(client, A_CLIENT_UUID);
				
				assertThat(service.findClient(A_CLIENT_UUID)).isEqualTo(client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testFindClientWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findClient(A_CLIENT_UUID))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findClientNamed'")
		class FindClientNamedIT {

			@Test
			@DisplayName("Client exists")
			void testFindClientNamedWhenClientExistsShouldReturnTheClient() {
				addTestClientToDatabase(client, A_CLIENT_UUID);
				
				assertThat(service.findClientNamed(A_FIRSTNAME, A_LASTNAME)).isEqualTo(client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testFindClientNamedWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findClientNamed(A_FIRSTNAME, A_LASTNAME))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'insertNewClient'")
		class InsertNewClientIT {

			@Test
			@DisplayName("Client is new")
			void testInsertNewClientWhenClientDoesNotAlreadyExistShouldInsertAndReturnWithId() {
				Client clientInDB = service.insertNewClient(client);
				
				assertThat(clientInDB).isEqualTo(client);
				assertThat(clientInDB.getId()).isNotNull();
				assertThat(readAllClientsFromDatabase()).containsExactly(client);
			}

			@Test
			@DisplayName("Client already exists")
			void testInsertNewClientWhenClientAlreadyExistsShouldNotInsertAndThrow() {
				Client existingClient = new Client(A_FIRSTNAME, A_LASTNAME);
				addTestClientToDatabase(existingClient, A_CLIENT_UUID);
				
				assertThatThrownBy(() -> service.insertNewClient(client))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(CLIENT_ALREADY_EXISTS_ERROR_MSG);
				
				List<Client> clientsInDB = readAllClientsFromDatabase();
				assertThat(clientsInDB).containsExactly(existingClient);
				assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'renameClient'")
		class RenameClientIT {

			@Test
			@DisplayName("A same name client doesn't exist")
			void testRenameClientWhenThereIsNoClientWithSameNewNamesShouldRenameAndReturnWithSameId() {
				Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
				addTestClientToDatabase(client, A_CLIENT_UUID);
				
				Client renamedClientInDB = service
						.renameClient(A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
				assertThat(renamedClientInDB).isEqualTo(renamedClient);
				assertThat(renamedClientInDB.getId()).isEqualTo(A_CLIENT_UUID);
				
				assertThat(readAllClientsFromDatabase())
					.doesNotContain(client)
					.containsExactly(renamedClient);
			}

			@Test
			@DisplayName("A same name client already exists")
			void testRenameClientWhenThereIsAlreadyAClientWithSameNewNamesShouldNotRenameAndThrow() {
				addTestClientToDatabase(client, A_CLIENT_UUID);
				addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
				
				assertThatThrownBy(() -> service.renameClient(
						A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(CLIENT_ALREADY_EXISTS_ERROR_MSG);
				
				assertThat(readAllClientsFromDatabase())
					.containsExactlyInAnyOrder(client, another_client)
					.filteredOn((c) -> c.getId().equals(A_CLIENT_UUID)).containsOnly(client);
			}

			@Test
			@DisplayName("Client to rename doesn't exist")
			void testRenameClientWhenClientToRenameDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.renameClient(
						A_CLIENT_UUID, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
				
				assertThat(readAllClientsFromDatabase()).isEmpty();
			}
		}

		private List<Client> readAllClientsFromDatabase() {
			return StreamSupport
					.stream(clientCollection.find().spliterator(), false)
					.collect(Collectors.toList());
		}
	}

	@Nested
	@DisplayName("Methods using only ReservationMongoRepository")
	class ReservationMongoRepositoryIT {

		@BeforeEach
		void initReservations() throws Exception {
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		@Nested
		@DisplayName("Integration tests for 'findAllReservations'")
		class FindAllReservationsIT {

			@Test
			@DisplayName("No reservations to retrieve")
			void testFindAllReservationsWhenThereAreNoReservationsToRetrieveShouldReturnEmptyList() {
				assertThat(service.findAllReservations()).isEmpty();
			}

			@Test
			@DisplayName("Several reservations to retrieve")
			void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationAsList() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
				assertThat(service.findAllReservations())
					.isEqualTo(Arrays.asList(reservation, another_reservation));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findReservation'")
		class FindReservationIT {

			@DisplayName("Reservation exists")
			@Test
			void testFindReservationWhenReservationExistsShouldReturnTheReservation() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				
				assertThat(service.findReservation(A_RESERVATION_UUID)).isEqualTo(reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testFindReservationWhenReservationDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findReservation(A_RESERVATION_UUID))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findReservationOn'")
		class FindReservationOnIT {

			@DisplayName("Reservation exists")
			@Test
			void testFindReservationOnWhenReservationExistsShouldReturnTheReservation() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				
				assertThat(service.findReservationOn(A_LOCALDATE)).isEqualTo(reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testFindReservationOnWhenReservationDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findReservationOn(A_LOCALDATE))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'rescheduleReservation'")
		class RescheduleReservationIT {

			@Test
			@DisplayName("A same date reservation doesn't exist")
			void testRescheduleReservationWhenThereIsNoReservationInTheSameNewDateShouldRescheduleAndReturnWithSameId() {
				Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				
				Reservation rescheduledReservationInDB =
						service.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE);
				assertThat(rescheduledReservationInDB).isEqualTo(rescheduledReservation);
				assertThat(rescheduledReservationInDB.getId()).isEqualTo(A_RESERVATION_UUID);
				
				assertThat(readAllReservationsFromDatabase())
					.doesNotContain(reservation)
					.containsExactly(rescheduledReservation);
			}

			@Test
			@DisplayName("A same date reservation already exists")
			void testRescheduleReservationWhenThereIsAlreadyAReservationInTheSameNewDateShouldNotRescheduleAndThrow() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
				assertThatThrownBy(
						() -> service.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
				
				assertThat(readAllReservationsFromDatabase())
					.containsExactlyInAnyOrder(reservation, another_reservation)
					.filteredOn((r) -> r.getId().equals(A_RESERVATION_UUID)).containsOnly(reservation);
			}

			@Test
			@DisplayName("Reservation to reschedule doesn't exist")
			void testRescheduleReservationWhenReservationToRescheduleDoesNotExistShouldThrow() {
				assertThatThrownBy(
						() -> service.rescheduleReservation(A_RESERVATION_UUID, ANOTHER_LOCALDATE))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
				
				assertThat(readAllReservationsFromDatabase()).isEmpty();
			}
		}
	}

	@Nested
	@DisplayName("Methods using both repositories")
	class BothRepositoriesIT {

		@BeforeEach
		void initEntities() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		@Nested
		@DisplayName("Integration tests for 'insertNewReservation'")
		class InsertNewReservationIT {

			@DisplayName("Reservation is new and client exists")
			@Test
			void testInsertNewReservationWhenReservationIsNewAndAssociatedClientExistsShouldInsertAndReturnWithId() {
				addTestClientToDatabase(client, A_CLIENT_UUID);
				
				Reservation reservationInDB = service.insertNewReservation(reservation);
				
				assertThat(reservationInDB).isEqualTo(reservation);
				assertThat(reservationInDB.getId()).isNotNull();
				assertThat(readAllReservationsFromDatabase()).containsExactly(reservationInDB);
			}

			@Test
			@DisplayName("Reservation is new and client doesn't exist")
			void testInsertNewReservationWhenReservationIsNewAndAssociatedClientDoesNotExistShouldNotInsertAndThrow() {
				assertThatThrownBy(() -> service.insertNewReservation(reservation))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
				
				assertThat(readAllReservationsFromDatabase()).isEmpty();
			}

			@Test
			@DisplayName("Reservation already exists")
			void testInsertNewReservationWhenReservationAlreadyExistsShouldNotInsertAndThrow() {
				Reservation existingReservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
				addTestReservationToDatabase(existingReservation, A_RESERVATION_UUID);
				
				assertThatThrownBy(() -> service.insertNewReservation(reservation))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
				
				List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
				assertThat(reservationsInDB).containsExactly(reservation);
				assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
			}
		}
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
