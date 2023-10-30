package io.github.marcopaglio.booking.app.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
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
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.assertj.swing.junit.runner.GUITestRunner;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class MongoBookingSwingAppE2E extends BookingSwingAppE2E {
	private static final String DBMS = "MONGO";

	private static String mongoHost = System.getProperty("mongo.host", "localhost");
	private static int mongoPort = Integer.parseInt(System.getProperty("mongo.port", "27017"));

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<Client> clientCollection;
	private static MongoCollection<Reservation> reservationCollection;

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
		addTestReservationToDatabase(A_CLIENT_UUID, A_DATE, A_RESERVATION_UUID);
		
		// start the Swing application
		application("io.github.marcopaglio.booking.app.swing.BookingSwingApp")
			.withArgs(
					"--dbms=" + DBMS,
					"--host=" + mongoHost,
					"--port=" + mongoPort
			).start();
		
		super.onSetUp();
	}

	@AfterClass
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Override
	protected void addTestClientToDatabase(String name, String surname, UUID id) {
		Client client = new Client(name, surname);
		client.setId(id);
		ClientSession session = mongoClient.startSession();
		clientCollection.createIndex(session,
				Indexes.descending(FIRSTNAME_DB, LASTNAME_DB), new IndexOptions().unique(true));
		clientCollection.insertOne(session, client);
		session.close();
	}

	@Override
	protected void removeTestClientFromDatabase(String name, String surname) {
		ClientSession session = mongoClient.startSession();
		clientCollection.deleteOne(session, Filters.and(
						Filters.eq(FIRSTNAME_DB, name),
						Filters.eq(LASTNAME_DB, surname)));
		session.close();
	}

	@Override
	protected void addTestReservationToDatabase(UUID clientId, String date, UUID id) {
		Reservation reservation = new Reservation(clientId,  LocalDate.parse(date));
		reservation.setId(id);
		ClientSession session = mongoClient.startSession();
		reservationCollection.createIndex(session,
				Indexes.descending(DATE_DB), new IndexOptions().unique(true));
		reservationCollection.insertOne(session, reservation);
		session.close();
	}

	@Override
	protected void removeTestReservationFromDatabase(String date) {
		ClientSession session = mongoClient.startSession();
		reservationCollection.deleteOne(session, Filters.eq(DATE_DB, LocalDate.parse(date)));
		session.close();
	}
}