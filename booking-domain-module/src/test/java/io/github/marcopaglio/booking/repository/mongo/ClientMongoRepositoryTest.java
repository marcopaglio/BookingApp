package io.github.marcopaglio.booking.repository.mongo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.BOOKING_DB_NAME;

class ClientMongoRepositoryTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("03005056-fa48-408e-ba3d-29d2e5d7f683");
	private static final Client ANOTHER_CLIENT = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);

	private static MongoServer server;
	private static String connectionString;
	private static MongoClientSettings settings;
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private MongoCollection<Client> clientCollection;
	private ClientMongoRepository clientRepository;

	@BeforeAll
	public static void setupServer() throws Exception {
		server = new MongoServer(new MemoryBackend());
		
		// bind on a random local port
		connectionString = server.bindAndGetConnectionString();
		
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
		clientRepository = new ClientMongoRepository(mongoClient);
		
		// get a MongoCollection suited for your POJO class
		clientCollection = clientRepository.getCollection();
	}

	@AfterAll
	public static void shutdownServer() throws Exception {
		mongoClient.close();
		server.shutdown();
	}

	@Nested
	@DisplayName("Tests for 'findAll'")
	class FindAllTest {

		@Test
		@DisplayName("Database is empty")
		void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
			assertThat(clientRepository.findAll()).isEmpty();
		}

		@Test
		@DisplayName("Database contains a single client")
		void testFindAllWhenDatabaseContainsASingleClientShouldReturnTheClientAsList() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findAll()).containsExactly(A_CLIENT);
		}

		@Test
		@DisplayName("Database contains several clients")
		void testFindAllWhenDatabaseContainsSeveralClientsShouldReturnClientsAsList() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			addTestClientToDatabase(ANOTHER_CLIENT, ANOTHER_CLIENT_UUID);
			
			assertThat(clientRepository.findAll()).containsExactlyInAnyOrder(A_CLIENT, ANOTHER_CLIENT);
		}
	}

	@Nested
	@DisplayName("Tests for 'findById'")
	class FindByIdTest {
// TODO: id nullo?
		@Test
		@DisplayName("Database is empty")
		void testFindByIdWhenDatabaseIsEmptyShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findById(A_CLIENT_UUID)).isEmpty();
		}

		@Test
		@DisplayName("Client is not in database")
		void testFindByIdWhenClientIsNotInDatabaseShouldReturnOptionalOfEmpty() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findById(ANOTHER_CLIENT_UUID)).isEmpty();
		}

		@Test
		@DisplayName("Client is in database")
		void testFindByIdWhenClientIsInDatabaseShouldReturnOptionalOfClient() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findById(A_CLIENT_UUID)).isEqualTo(Optional.of(A_CLIENT));
		}
	}

	@Nested
	@DisplayName("Tests for 'findByName'")
	class FindByNameTest {
// TODO: name e surname nulli?
		@Test
		@DisplayName("Database is empty")
		void testFindByNameWhenDatabaseIsEmptyShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME)).isEmpty();
		}

		@Test
		@DisplayName("Client is not in database")
		void testFindByNameWhenClientIsNotInDatabaseShouldReturnOptionalOfEmpty() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME)).isEmpty();
		}

		@Test
		@DisplayName("There is another client of the same name")
		void testFindByNameWhenThereIsAnotherClientOfTheSameNameShouldReturnOptionalOfEmpty() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(A_FIRSTNAME, ANOTHER_LASTNAME)).isEmpty();
		}

		@Test
		@DisplayName("There is another client of the same surname")
		void testFindByNameWhenThereIsAnotherClientOfTheSameSurnameShouldReturnOptionalOfEmpty() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, A_LASTNAME)).isEmpty();
		}

		@Test
		@DisplayName("Client is in database")
		void testFindByNameWhenClientIsInDatabaseShouldReturnOptionalOfClient() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
				.isEqualTo(Optional.of(A_CLIENT));
		}
	}

	@Nested
	@DisplayName("Tests for 'save'")
	class SaveTest {
		private Client client;

		@BeforeEach
		void resetClientId() {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
		}

		@Test
		@DisplayName("Client is new")
		void testSaveWhenClientIsNewShouldInsertInDatabaseAndReturnClientWithId() {
			assertThat(client.getId()).isNull();
			Client returnedClient = clientRepository.save(client);
			
			assertThat(returnedClient).isEqualTo(client);
			assertThat(returnedClient.getId()).isNotNull();
			assertThat(readAllClientsFromDatabase()).containsExactly(client);
		}

		@Test
		@DisplayName("Client already has an id")
		void testSaveWhenClientAlreadyHasAnIdShouldNotChangeTheIdAndInsertInDatabase() {
			client.setId(A_CLIENT_UUID);
			
			Client returnedClient = clientRepository.save(client);
			
			assertThat(returnedClient.getId()).isEqualTo(A_CLIENT_UUID);
			assertThat(readAllClientsFromDatabase()).containsExactly(client);
		}

		@Test
		@DisplayName("Id collision in database")
		void testSaveWhenThereIsAnIdCollisionInDatabaseShouldNotInsertAndThrow() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client clientWithSameId = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			clientWithSameId.setId(A_CLIENT_UUID);
			
			assertThatThrownBy(() -> clientRepository.save(clientWithSameId))
				.isInstanceOf(InstanceAlreadyExistsException.class)
				.hasMessage("The insertion violates uniqueness constraints.");
			
			assertThat(readAllClientsFromDatabase()).doesNotContain(clientWithSameId);
		}

		@Test
		@DisplayName("Names collision in database")
		void testsaveWhenThereIsANamesCollisionInDatabaseShouldNotInsertAndThrow() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client clientWithSameNames = new Client(A_FIRSTNAME, A_LASTNAME);
			// set different id
			clientWithSameNames.setId(ANOTHER_CLIENT_UUID);
			
			assertThatThrownBy(() -> clientRepository.save(clientWithSameNames))
				.isInstanceOf(InstanceAlreadyExistsException.class)
				.hasMessage("The insertion violates uniqueness constraints.");
			
			List<Client> clientsInDB = readAllClientsFromDatabase();
			assertThat(clientsInDB).containsExactly(client);
			assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
		}

		@Test
		@DisplayName("Client with same name")
		void testsaveWhenThereIsAnotherClientWithSameNameAndDifferentSurnameAndIdInDatabaseShouldNotThrowAndInsert() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client clientWithSameName = new Client(A_FIRSTNAME, ANOTHER_LASTNAME);
			// set different id
			clientWithSameName.setId(ANOTHER_CLIENT_UUID);
			
			assertThatNoException().isThrownBy(() -> clientRepository.save(clientWithSameName));
			
			assertThat(readAllClientsFromDatabase()).containsExactlyInAnyOrder(client, clientWithSameName);
		}

		@Test
		@DisplayName("Client with same surname")
		void testsaveWhenThereIsAnotherClientWithSameSurnameAndDifferentNameAndIdInDatabaseShouldNotThrowAndInsert() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client clientWithSameSurname = new Client(ANOTHER_FIRSTNAME, A_LASTNAME);
			// set different id
			clientWithSameSurname.setId(ANOTHER_CLIENT_UUID);
			
			assertThatNoException().isThrownBy(() -> clientRepository.save(clientWithSameSurname));
			
			assertThat(readAllClientsFromDatabase()).containsExactlyInAnyOrder(client, clientWithSameSurname);
		}

		@Test
		@DisplayName("Null client")
		void testSaveWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> clientRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot be null.");
		}

		@Test
		@DisplayName("Client with null name")
		void testSaveWhenClientHasNullNameShouldNotInsertAndThrow() {
			assertThat(readAllClientsFromDatabase()).isEmpty();
			Client clientWithNullName = new Client(null, A_LASTNAME);
			
			assertThatThrownBy(() -> clientRepository.save(clientWithNullName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot have a null name.");
			
			assertThat(readAllClientsFromDatabase()).isEmpty();
		}

		@Test
		@DisplayName("Client with null surname")
		void testSaveWhenClientHasNullSurnameShouldNotInsertAndThrow() {
			assertThat(readAllClientsFromDatabase()).isEmpty();
			Client clientWithNullName = new Client(A_FIRSTNAME, null);
			
			assertThatThrownBy(() -> clientRepository.save(clientWithNullName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot have a null name.");
			
			assertThat(readAllClientsFromDatabase()).isEmpty();
		}

		@Test
		@DisplayName("Client with null both name and surname")
		void testSaveWhenClientHasNullBothNameAndSurnameShouldNotInsertAndThrow() {
			assertThat(readAllClientsFromDatabase()).isEmpty();
			Client clientWithNullName = new Client(null, null);
			
			assertThatThrownBy(() -> clientRepository.save(clientWithNullName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot have a null name.");
			
			assertThat(readAllClientsFromDatabase()).isEmpty();
		}

		@Nested
		@DisplayName("Tests for 'delete'")
		class DeleteTest {

			@Test
			@DisplayName("Null client")
			void testDeleteWhenClientIsNullShouldThrow() {
				assertThatThrownBy(() -> clientRepository.delete(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client to delete cannot be null.");
			}

			@Test
			@DisplayName("Client with id is in database")
			void testDeleteWhenClientWithIdIsInDatabaseShouldRemove() {
				assertThat(readAllClientsFromDatabase()).isEmpty();
				addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
				
				clientRepository.delete(A_CLIENT);
				
				assertThat(readAllClientsFromDatabase()).doesNotContain(A_CLIENT);
				assertThat(readAllClientsFromDatabase()).isEmpty();
			}

			@Test
			@DisplayName("Client without id is in database")
			void testDeleteWhenClientWithoutIdIsInDatabaseShouldRemove() {
				assertThat(readAllClientsFromDatabase()).isEmpty();
				addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
				
				clientRepository.delete(new Client(A_FIRSTNAME, A_LASTNAME));
				
				assertThat(readAllClientsFromDatabase()).doesNotContain(A_CLIENT);
				assertThat(readAllClientsFromDatabase()).isEmpty();
			}

			@Test
			@DisplayName("Client is not in database")
			void testDeleteWhenClientIsNotInDatabaseShouldNotThrowAndNotRemoveAnything() {
				addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
				
				clientRepository.delete(ANOTHER_CLIENT);
				
				assertThat(readAllClientsFromDatabase()).containsExactly(A_CLIENT);
			}
		}
	}

	private List<Client> readAllClientsFromDatabase() {
		return StreamSupport
				.stream(clientCollection.find().spliterator(), false)
				.collect(Collectors.toList());
	}

	private void addTestClientToDatabase(Client client, UUID id) {
		client.setId(id);
		clientCollection.insertOne(client);
	}
	/*@Test
	void testCollectionIsEmpty() {
		assertThat(clientCollection.countDocuments()).isZero();

		// make a document and insert it
		Client ada = new Client("Ada", "Byron");
		ada.setId(A_CLIENT_UUID);
		System.out.println("Original Person Model: " + ada);
		clientCollection.insertOne(ada);

		// Person will now have an ObjectId
		System.out.println("Mutated Person Model: " + ada);
		assertThat(clientCollection.countDocuments()).isEqualTo(1L);
		
		Client oda = new Client("Ada", "Byron");
		oda.setId(ANOTHER_CLIENT_UUID);
		System.out.println("Original Client Model: " + oda);
		//clientCollection.insertOne(oda);
		assertThatThrownBy(() -> clientCollection.insertOne(oda)).isInstanceOf(MongoWriteException.class);

		// get it (since it's the only one in there since we dropped the rest earlier on)
		Client somebody = clientCollection.find().first();
		System.out.println("Retrieved client: " + somebody);
		
		assertThat(somebody).isEqualTo(ada);
	}
	
	@Test
	void testCollectionIsEmpty2() {
		assertThat(clientCollection.countDocuments()).isZero();

		// make a document and insert it
		Client ada = new Client("Ada", "Byron");
		ada.setId(A_CLIENT_UUID);
		System.out.println("Original Person Model: " + ada);
		clientCollection.insertOne(ada);

		// Person will now have an ObjectId
		System.out.println("Mutated Person Model: " + ada);
		assertThat(clientCollection.countDocuments()).isEqualTo(1L);
		
		Client oda = new Client("Ada", "Byron");
		oda.setId(ANOTHER_CLIENT_UUID);
		System.out.println("Original Client Model: " + oda);
		assertThatThrownBy(() -> clientCollection.insertOne(oda)).isInstanceOf(MongoWriteException.class);

		// get it (since it's the only one in there since we dropped the rest earlier on)
		Client somebody = clientCollection.find().first();
		System.out.println("Retrieved client: " + somebody);
		
		assertThat(somebody).isEqualTo(ada);
	}*/
}
