package io.github.marcopaglio.booking.repository.mongo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
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
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private MongoCollection<Client> clientCollection;
	private ClientMongoRepository clientRepository;

	@BeforeAll
	public static void setupServer() throws Exception {
		server = new MongoServer(new MemoryBackend());
		
		mongoClient = getClient(server);
		
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
	}

	private static MongoClient getClient(MongoServer server) {
		// bind on a random local port
		String connectionString = server.bindAndGetConnectionString();
		
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

		@Test
		@DisplayName("Null id")
		void testFindByIdWhenIdIsNullShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findById(null)).isEmpty();
		}

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
			addTestClientToDatabase(ANOTHER_CLIENT, ANOTHER_CLIENT_UUID);
			
			assertThat(clientRepository.findById(ANOTHER_CLIENT_UUID))
				.isEqualTo(Optional.of(ANOTHER_CLIENT));
		}
	}

	@Nested
	@DisplayName("Tests for 'findByName'")
	class FindByNameTest {

		@Test
		@DisplayName("Null names")
		void testFindByNameWhenNamesAreNullShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findByName(null, A_LASTNAME)).isEmpty();
			
			assertThat(clientRepository.findByName(A_FIRSTNAME, null)).isEmpty();
			
			assertThat(clientRepository.findByName(null, null)).isEmpty();
		}

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
		@DisplayName("Another same name client in database")
		void testFindByNameWhenThereIsAnotherClientWithTheSameNameShouldNotMatchIt() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(A_FIRSTNAME, ANOTHER_LASTNAME))
				.isNotEqualTo(Optional.of(A_CLIENT));
		}

		@Test
		@DisplayName("Another same surname client in database")
		void testFindByNameWhenThereIsAnotherClientWithTheSameSurnameShouldNotMatchIt() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			
			assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, A_LASTNAME))
				.isNotEqualTo(Optional.of(A_CLIENT));
		}

		@Test
		@DisplayName("Client is in database")
		void testFindByNameWhenClientIsInDatabaseShouldReturnOptionalOfClient() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			addTestClientToDatabase(ANOTHER_CLIENT, ANOTHER_CLIENT_UUID);
			
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
		@DisplayName("Null client")
		void testSaveWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> clientRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot be null.");
		}

		@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
		@DisplayName("Client with null names")
		@CsvSource( value = {"'null', 'Rossi'", "'Mario', 'null'", "'null', 'null'"},
				nullValues = {"null"}
		)
		void testSaveWhenClientHasNullNamesShouldNotInsertAndThrow(String firstName, String lastName) {
			assertThat(readAllClientsFromDatabase()).isEmpty();
			Client nullNameClient = new Client(firstName, lastName);
			
			assertThatThrownBy(() -> clientRepository.save(nullNameClient))
				.isInstanceOf(NotNullConstraintViolationException.class)
				.hasMessage("Client to save must have both not-null names.");
			
			assertThat(readAllClientsFromDatabase()).isEmpty();
		}

		@Test
		@DisplayName("New client is valid")
		void testSaveWhenNewClientIsValidShouldInsertAndReturnTheClientWithId() {
			assertThat(client.getId()).isNull();
			assertThat(readAllClientsFromDatabase()).isEmpty();
			
			Client returnedClient = clientRepository.save(client);
			
			List<Client> clientsInDB = readAllClientsFromDatabase();
			assertThat(clientsInDB).containsExactly(client);
			assertThat(returnedClient).isEqualTo(client);
			assertThat(clientsInDB.get(0).getId()).isNotNull();
			assertThat(returnedClient.getId()).isNotNull();
			assertThat(clientsInDB.get(0).getId()).isEqualTo(returnedClient.getId());
		}

		@Test
		@DisplayName("New client generates id collision")
		void testSaveWhenNewClientGeneratesAnIdCollisionShouldNotInsertAndThrow() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client spied_client = spy(new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME));
			// sets same id
			doAnswer(invocation -> {
				((Client) invocation.getMock()).setId(A_CLIENT_UUID);
				return null;
			}).when(spied_client).setId(not(eq(A_CLIENT_UUID)));
			
			assertThatThrownBy(() -> clientRepository.save(spied_client))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("The insertion violates uniqueness constraints.");
			
			assertThat(readAllClientsFromDatabase()).doesNotContain(spied_client);
		}

		@Test
		@DisplayName("New client generates names collision")
		void testSaveWhenNewClientGeneratesANamesCollisionShouldNotInsertAndThrow() {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client spied_client = spy(new Client(A_FIRSTNAME, A_LASTNAME));
			// sets different id
			doAnswer(invocation -> {
				((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
				return null;
			}).when(spied_client).setId(A_CLIENT_UUID);
			
			assertThatThrownBy(() -> clientRepository.save(spied_client))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("The insertion violates uniqueness constraints.");
			
			List<Client> clientsInDB = readAllClientsFromDatabase();
			assertThat(clientsInDB).containsExactly(client);
			assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
		}

		@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
		@DisplayName("New client has some equalities")
		@CsvSource({
			"'Mario', 'De Lucia'",	// same name
			"'Maria', 'Rossi'"		// same surname
		})
		void testSaveWhenNewClientHasSomeEqualitiesShouldNotThrowAndInsert(String firstName, String lastName) {
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			Client spied_client = spy(new Client(firstName, lastName));
			// sets different id
			doAnswer(invocation -> {
				((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
				return null;
			}).when(spied_client).setId(A_CLIENT_UUID);
			
			assertThatNoException().isThrownBy(() -> clientRepository.save(spied_client));
			
			assertThat(readAllClientsFromDatabase()).containsExactlyInAnyOrder(client, spied_client);
		}

		@Test
		@DisplayName("Updated client is valid")
		void testSaveWhenUpdatedClientIsValidShouldUpdateAndReturnWithoutChangingId() {
			// populate DB
			addTestClientToDatabase(client, A_CLIENT_UUID);
			
			// verify state before
			List<Client> clientsInDB = readAllClientsFromDatabase();
			assertThat(clientsInDB).containsExactly(client);
			assertThat(clientsInDB.get(0).getFirstName()).isEqualTo(A_FIRSTNAME);
			assertThat(clientsInDB.get(0).getLastName()).isEqualTo(A_LASTNAME);
			assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
			
			// update
			client.setFirstName(ANOTHER_FIRSTNAME);
			client.setLastName(ANOTHER_LASTNAME);
			
			Client returnedClient = clientRepository.save(client);
			
			// verify state after
			clientsInDB = readAllClientsFromDatabase();
			assertThat(clientsInDB).containsExactly(client);
			assertThat(returnedClient).isEqualTo(client);
			assertThat(clientsInDB.get(0).getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
			assertThat(returnedClient.getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
			assertThat(clientsInDB.get(0).getLastName()).isEqualTo(ANOTHER_LASTNAME);
			assertThat(returnedClient.getLastName()).isEqualTo(ANOTHER_LASTNAME);
			assertThat(clientsInDB.get(0).getId()).isEqualTo(A_CLIENT_UUID);
			assertThat(returnedClient.getId()).isEqualTo(A_CLIENT_UUID);
		}

		@Test
		@DisplayName("Updated client is no longer present in database")
		void testSaveWhenUpdatedClientIsNotInDatabaseShouldNotThrow() {
			client.setId(A_CLIENT_UUID);
			
			assertThatNoException().isThrownBy(() -> clientRepository.save(client));
		}

		@Test
		@DisplayName("Updated client generates names collision")
		void testSaveWhenUpdatedClientGeneratesNamesCollisionShouldNotUpdateAndThrow() {
			// populate DB
			addTestClientToDatabase(client, A_CLIENT_UUID);
			Client clientToUpdate = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			addTestClientToDatabase(clientToUpdate, ANOTHER_CLIENT_UUID);
			
			// update
			clientToUpdate.setFirstName(A_FIRSTNAME);
			clientToUpdate.setLastName(A_LASTNAME);
			
			assertThatThrownBy(() -> clientRepository.save(clientToUpdate))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("The update violates uniqueness constraints.");
			
			Set<String> namesInDB = new HashSet<>();
			readAllClientsFromDatabase().forEach((c) -> namesInDB.add(c.getFirstName()));
			assertThat(namesInDB).contains(ANOTHER_FIRSTNAME);
			Set<String> surnamesInDB = new HashSet<>();
			readAllClientsFromDatabase().forEach((c) -> surnamesInDB.add(c.getLastName()));
			assertThat(surnamesInDB).contains(ANOTHER_LASTNAME);
		}
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
		@DisplayName("Client is in database")
		void testDeleteWhenClientIsInDatabaseShouldRemove() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			assertThat(readAllClientsFromDatabase()).contains(A_CLIENT);
			
			clientRepository.delete(A_CLIENT);
			
			assertThat(readAllClientsFromDatabase()).doesNotContain(A_CLIENT);
		}

		@Test
		@DisplayName("Client is not in database")
		void testDeleteWhenClientIsNotInDatabaseShouldNotRemoveAnythingAndNotThrow() {
			addTestClientToDatabase(A_CLIENT, A_CLIENT_UUID);
			assertThat(readAllClientsFromDatabase()).containsExactly(A_CLIENT);
			
			assertThatNoException().isThrownBy(() -> clientRepository.delete(ANOTHER_CLIENT));
			
			assertThat(readAllClientsFromDatabase()).containsExactly(A_CLIENT);
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
}
