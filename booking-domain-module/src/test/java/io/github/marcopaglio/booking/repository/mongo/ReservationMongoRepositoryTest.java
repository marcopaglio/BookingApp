package io.github.marcopaglio.booking.repository.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository.BOOKING_DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.github.marcopaglio.booking.model.Reservation;

class ReservationMongoRepositoryTest {
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.randomUUID();
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-12-22");

	private static MongoServer server;
	private static String connectionString;
	private static MongoClientSettings settings;
	private static MongoClient mongoClient;
	private static MongoDatabase database;
	
	private MongoCollection<Reservation> reservationCollection;
	private ReservationMongoRepository reservationRepository;

	@BeforeAll
	static void setupServer() throws Exception {
		server = new MongoServer(new MemoryBackend());
		
		// bind on a random local port
		connectionString = server.bindAndGetConnectionString();
		
		// define the CodecProvider for POJO classes
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.conventions(Arrays.asList(ANNOTATION_CONVENTION))
				.automatic(true)
				.build();
		
		// define the CodecRegistry as codecs and other related information
		CodecRegistry pojoCodecRegistry =
				fromRegistries(getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider));
		
		// configure the MongoClient for using the CodecRegistry
		settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(connectionString))
				.uuidRepresentation(STANDARD)
				.codecRegistry(pojoCodecRegistry)
				.build();
		mongoClient = MongoClients.create(settings);
		
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
	}

	@BeforeEach
	void setUp() throws Exception {
		// make sure we always start with a clean database
		database.drop();
		
		// repository creation after drop because it removes configurations on collections
		reservationRepository = new ReservationMongoRepository(mongoClient);
		
		// get a MongoCollection suited for your POJO class
		reservationCollection = reservationRepository.getCollection();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@AfterAll
	static void shutdownServer() throws Exception {
		mongoClient.close();
		server.shutdown();
	}

	@Test
	void test() {
		assertThat(reservationCollection.countDocuments()).isZero();

		// make a document and insert it
		Reservation ada = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		System.out.println("Original Reservation Model: " + ada);
		reservationCollection.insertOne(ada);

		// Person will now have an ObjectId
		System.out.println("Mutated Reservation Model: " + ada);
		assertThat(reservationCollection.countDocuments()).isEqualTo(1L);
		
		Reservation oda = new Reservation(ANOTHER_CLIENT_UUID, A_LOCALDATE);
		System.out.println("Original Reservation Model: " + oda);
		assertThatThrownBy(() -> reservationCollection.insertOne(oda)).isInstanceOf(MongoWriteException.class);

		// get it (since it's the only one in there since we dropped the rest earlier on)
		Reservation something = reservationCollection.find().first();
		System.out.println("Retrieved Reservation: " + something);
		
		assertThat(something).isEqualTo(ada);
	}

}
