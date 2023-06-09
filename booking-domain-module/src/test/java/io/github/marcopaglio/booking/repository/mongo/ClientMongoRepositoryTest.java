package io.github.marcopaglio.booking.repository.mongo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.UUID;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.github.marcopaglio.booking.model.Client;

import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.BOOKING_DB_NAME;
import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.CLIENT_COLLECTION_NAME;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.UuidRepresentation.JAVA_LEGACY;
import static org.bson.codecs.pojo.Conventions.SET_PRIVATE_FIELDS_CONVENTION;


class ClientMongoRepositoryTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.randomUUID(); //TODO change to fromSTring
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);


	private static MongoServer server;
	private static String connectionString;
	
	CodecRegistry pojoCodecRegistry;

	private MongoClient mongoClient;
	private MongoCollection<Client> clientCollection;

	private ClientMongoRepository clientRepository;

	@BeforeAll
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		
		// bind on a random local port
		connectionString = server.bindAndGetConnectionString();
	}

	@BeforeEach
	void setUp() throws Exception {
		// define the CodecProvider for POJO classes
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.conventions(Arrays.asList(SET_PRIVATE_FIELDS_CONVENTION))
				.automatic(true)
				.build();
		
		// define the CodecRegistry the codecs and other related information
		CodecRegistry pojoCodecRegistry =
				fromRegistries(getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider));
		
		// configure the MongoClient for using the CodecRegistry
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(connectionString))
				.uuidRepresentation(JAVA_LEGACY) // FIXME check se questo o STANDARD
				.codecRegistry(pojoCodecRegistry)
				.build();
		mongoClient = MongoClients.create(settings);
		
		clientRepository = new ClientMongoRepository(mongoClient);
		MongoDatabase database = mongoClient.getDatabase(BOOKING_DB_NAME);
		
		// make sure we always start with a clean database
		database.drop();
		
		// pass your POJO class for getting a MongoCollection instance
		clientCollection = database.getCollection(CLIENT_COLLECTION_NAME, Client.class);
	}

	@AfterEach
	void tearDown() throws Exception {
		mongoClient.close();
	}

	@AfterAll
	public static void shutdownServer() {
		server.shutdown();
	}

	@Test
	void testCollectionIsEmpty() {
		assertThat(clientCollection.countDocuments()).isZero();

		// make a document and insert it
		Client ada = new Client("Ada", "Byron", UUID.randomUUID());
		System.out.println("Original Person Model: " + ada);
		clientCollection.insertOne(ada);

		// Person will now have an ObjectId
		System.out.println("Mutated Person Model: " + ada);
		assertThat(clientCollection.countDocuments()).isEqualTo(1L);

		// get it (since it's the only one in there since we dropped the rest earlier on)
		Client somebody = clientCollection.find().first();
		System.out.println("Retrieved client: " + somebody);
		
		assertThat(somebody).isEqualTo(ada);
	}
}
