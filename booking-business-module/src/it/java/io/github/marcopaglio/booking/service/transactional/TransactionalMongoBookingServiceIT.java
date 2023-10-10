package io.github.marcopaglio.booking.service.transactional;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.repository.mongo.MongoRepository.BOOKING_DB_NAME;
import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

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
		@DisplayName("Tests for 'findAllClients'")
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
	
		public void addTestClientToDatabase(Client client, UUID id) {
			client.setId(id);
			clientCollection.insertOne(client);
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
		@DisplayName("Tests for 'findAllReservations'")
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
	
		public void addTestReservationToDatabase(Reservation reservation, UUID id) {
			reservation.setId(id);
			reservationCollection.insertOne(reservation);
		}
	}

}
