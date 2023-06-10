package io.github.marcopaglio.booking.repository.mongo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.BOOKING_DB_NAME;
import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.CLIENT_COLLECTION_NAME;

class ClientMongoRepositoryTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME, A_CLIENT_UUID);

	private static MongoServer server;
	private static String connectionString;
	private static MongoClientSettings settings;

	private MongoClient mongoClient;
	private MongoCollection<Client> clientCollection;

	private ClientMongoRepository clientRepository;

	@BeforeAll
	public static void setupServer() {
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
	}

	@BeforeEach
	void setUp() throws Exception {
		mongoClient = MongoClients.create(settings);
		
		clientRepository = new ClientMongoRepository(mongoClient);
		MongoDatabase database = mongoClient.getDatabase(BOOKING_DB_NAME);
		
		// make sure we always start with a clean database
		database.drop();
		
		// get a MongoCollection suited for your POJO class
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
		Client ada = new Client("Ada", "Byron", A_CLIENT_UUID);
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
	/*
	@Nested
	@DisplayName("Tests for 'save'")
	class saveTest {

		/*@Test
		void testSaveWhenClientIsNullShouldThrow() {
			assertThatThrownBy(
					() -> clientRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot be null.");
		}

		@Test
		void testSaveWhenClientIsNewShouldCreateAndReturn() {
			
			assertThat(clientRepository.save(A_CLIENT)).isEqualTo(A_CLIENT);
			
			assertThat(readAllClientsFromDatabase()).containsExactly(A_CLIENT);
		}
		
		 private List<Client> readAllClientsFromDatabase() {
			 return StreamSupport
					.stream(clientCollection.find().spliterator(), false)
					.map(d -> new Client(d.getString("firstName"), d.getString("lastName"),
							UUID.fromString(d.getString("_id"))))
					.collect(Collectors.toList());
		}

    void testSimpleInsertQuery() throws Exception {
        assertThat(clientCollection.countDocuments()).isZero();

        // creates the database and collection in memory and insert the object
        Document obj = new Document("_id", 1).append("key", "value");
        clientCollection.insertOne(obj);

        assertThat(clientCollection.countDocuments()).isEqualTo(1L);
        assertThat(clientCollection.find().first()).isEqualTo(obj);
    }
	}*/


}
