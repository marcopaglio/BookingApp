package io.github.marcopaglio.booking.repository.mongo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.github.marcopaglio.booking.exception.UpdateFailureException;
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

@DisplayName("Tests for ClientMongoRepository class")
@Testcontainers
class ClientMongoRepositoryTest {
	private static final String ID_FIELD = "id";
	private static final String LASTNAME_FIELD = "lastName";
	private static final String FIRSTNAME_FIELD = "firstName";

	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("03005056-fa48-408e-ba3d-29d2e5d7f683");

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static final String BOOKING_DB_NAME = "ClientMongoRepositoryTest_db";

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private ClientSession session;
	private MongoCollection<Client> clientCollection;

	private ClientMongoRepository clientRepository;

	@BeforeAll
	public static void setupServer() throws Exception {
		mongoClient = getClient(mongo.getConnectionString());
		
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
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
		// start a new session for communicating with the DB
		session = mongoClient.startSession();
		
		// make sure we always start with a clean database
		database.drop();
		
		// repository creation after drop because it removes configurations on collections
		clientRepository = new ClientMongoRepository(mongoClient, session, BOOKING_DB_NAME);
		
		// get a MongoCollection suited for your POJO class
		clientCollection = clientRepository.getCollection();
	}

	@AfterEach
	void closeHandler() throws Exception {
		session.close();
	}

	@AfterAll
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Null inputs on methods")
	class NullInputTest {

		@Test
		@DisplayName("Null id on 'findById'")
		void testFindByIdWhenIdIsNullShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findById(null)).isEmpty();
		}

		@Test
		@DisplayName("Null names on 'findByName'")
		void testFindByNameWhenNamesAreNullShouldReturnOptionalOfEmpty() {
			assertThat(clientRepository.findByName(null, A_LASTNAME)).isEmpty();
			
			assertThat(clientRepository.findByName(A_FIRSTNAME, null)).isEmpty();
			
			assertThat(clientRepository.findByName(null, null)).isEmpty();
		}

		@Test
		@DisplayName("Null client on 'save'")
		void testSaveWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> clientRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to save cannot be null.");
		}

		@Test
		@DisplayName("Null client on 'delete'")
		void testDeleteWhenClientIsNullShouldThrow() {
			assertThatThrownBy(() -> clientRepository.delete(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to delete cannot be null.");
		}
	}

	@Nested
	@DisplayName("Using entities")
	class UsingEntitiesTest {
		private Client client;
		private Client another_client;

		@BeforeEach
		void initClients() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		}

		@Nested
		@DisplayName("Tests that read the database")
		class ReadDBTest {

			@Nested
			@DisplayName("Tests for 'findAll'")
			class FindAllTest {

				@Test
				@DisplayName("Database is empty")
				void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
					assertThat(clientRepository.findAll()).isEmpty();
				}

				@Test
				@DisplayName("Database has been filled in the same context")
				void testFindAllWhenDatabaseHasBeenFilledInTheSameContextShouldReturnClientsAsList() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInTheSameContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findAll())
						.containsExactlyInAnyOrder(client, another_client);
				}

				@Test
				@DisplayName("Database has been filled in another context")
				void testFindAllWhenDatabaseHasBeenFilledInAnotherContextShouldReturnClientsAsList() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInAnotherContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findAll())
						.containsExactlyInAnyOrder(client, another_client);
				}
			}

			@Nested
			@DisplayName("Tests for 'findById'")
			class FindByIdTest {

				@Test
				@DisplayName("Client is not in database")
				void testFindByIdWhenClientIsNotInDatabaseShouldReturnOptionalOfEmpty() {
					assertThat(clientRepository.findById(A_CLIENT_UUID)).isEmpty();
				}

				@Test
				@DisplayName("Client was added in the same context")
				void testFindByIdWhenClientWasAddedToTheDatabaseInTheSameContextShouldReturnOptionalOfClient() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInTheSameContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findById(A_CLIENT_UUID))
						.isEqualTo(Optional.of(client));
				}

				@Test
				@DisplayName("Client was added in another context")
				void testFindByIdWhenClientWasAddedToTheDatabaseInAnotherContextShouldReturnOptionalOfClient() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInAnotherContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findById(A_CLIENT_UUID))
						.isEqualTo(Optional.of(client));
				}
			}

			@Nested
			@DisplayName("Tests for 'findByName'")
			class FindByNameTest {

				@Test
				@DisplayName("Client is not in database")
				void testFindByNameWhenClientIsNotInDatabaseShouldReturnOptionalOfEmpty() {
					assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME)).isEmpty();
				}

				@Test
				@DisplayName("Client was added in the same context")
				void testFindByNameWhenClientWasAddedToTheDatabaseInTheSameContextShouldReturnOptionalOfClient() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInTheSameContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(Optional.of(client));
				}

				@Test
				@DisplayName("Client was added in another context")
				void testFindByNameWhenClientWasAddedToTheDatabaseInAnotherContextShouldReturnOptionalOfClient() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInAnotherContext(another_client, ANOTHER_CLIENT_UUID);
					
					assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(Optional.of(client));
				}

				@Test
				@DisplayName("Another same name client in database")
				void testFindByNameWhenThereIsAnotherClientWithTheSameNameShouldNotMatchIt() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					assertThat(clientRepository.findByName(A_FIRSTNAME, ANOTHER_LASTNAME))
						.isNotEqualTo(Optional.of(client));
				}

				@Test
				@DisplayName("Another same surname client in database")
				void testFindByNameWhenThereIsAnotherClientWithTheSameSurnameShouldNotMatchIt() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, A_LASTNAME))
						.isNotEqualTo(Optional.of(client));
				}
			}
		}

		@Nested
		@DisplayName("Tests that modify the database")
		class ModifyDBTest {

			@Nested
			@DisplayName("Tests for 'save'")
			class SaveTest {

				@Test
				@DisplayName("New client is valid")
				void testSaveWhenNewClientIsValidShouldInsertAndReturnTheClientWithId() {
					Client returnedClient = clientRepository.save(client);
					
					assertThat(returnedClient).isEqualTo(client)
						.extracting(Client::getId).isNotNull();
					assertThat(readAllClientsFromDatabase())
						.singleElement().isEqualTo(client)
							.extracting(Client::getId).isEqualTo(returnedClient.getId());
				}

				@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
				@DisplayName("New client has null names")
				@CsvSource( value = {"'null', 'Rossi'", "'Mario', 'null'", "'null', 'null'"},
						nullValues = {"null"}
				)
				void testSaveWhenNewClientHasNullNamesShouldNotInsertAndThrow(
						String firstName, String lastName) {
					client.setFirstName(firstName);
					client.setLastName(lastName);
					
					assertThatThrownBy(() -> clientRepository.save(client))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Client to save violates not-null constraints.");
					
					assertThat(readAllClientsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("New client generates id collision in the same context")
				void testSaveWhenNewClientGeneratesAnIdCollisionInTheSameContextShouldNotInsertAndThrow() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					Client spied_client = spy(another_client);
					// set same id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(A_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(not(eq(A_CLIENT_UUID)));
					
					assertThatThrownBy(() -> clientRepository.save(spied_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Client to save violates uniqueness constraints.");
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(spied_client);
				}

				@Test
				@DisplayName("New client generates id collision in another context")
				void testSaveWhenNewClientGeneratesAnIdCollisionInAnotherContextShouldNotInsertAndThrow() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					
					Client spied_client = spy(another_client);
					// set same id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(A_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(not(eq(A_CLIENT_UUID)));
					
					assertThatThrownBy(() -> clientRepository.save(spied_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Client to save violates uniqueness constraints.");
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(spied_client);
				}

				@Test
				@DisplayName("New client generates names collision in the same context")
				void testSaveWhenNewClientGeneratesANamesCollisionInTheSameContextShouldNotInsertAndThrow() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					another_client.setFirstName(client.getFirstName());
					another_client.setLastName(client.getLastName());
					Client spied_client = spy(another_client);
					// set different id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(A_CLIENT_UUID);
					
					assertThatThrownBy(() -> clientRepository.save(spied_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Client to save violates uniqueness constraints.");
					
					assertThat(readAllClientsFromDatabase())
						.singleElement().isEqualTo(client)
							.extracting(Client::getId).isEqualTo(A_CLIENT_UUID);
				}

				@Test
				@DisplayName("New client generates names collision in another context")
				void testSaveWhenNewClientGeneratesANamesCollisionInAnotherContextShouldNotInsertAndThrow() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					
					another_client.setFirstName(client.getFirstName());
					another_client.setLastName(client.getLastName());
					Client spied_client = spy(another_client);
					// set different id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(A_CLIENT_UUID);
					
					assertThatThrownBy(() -> clientRepository.save(spied_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Client to save violates uniqueness constraints.");
					
					assertThat(readAllClientsFromDatabase())
						.singleElement().isEqualTo(client)
							.extracting(Client::getId).isEqualTo(A_CLIENT_UUID);
				}

				@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
				@DisplayName("New client has some equalities")
				@CsvSource({
					"'Mario', 'De Lucia'",	// same name
					"'Maria', 'Rossi'"		// same surname
				})
				void testSaveWhenNewClientHasSomeEqualitiesShouldNotThrowAndInsert(
						String firstName, String lastName) {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					another_client.setFirstName(firstName);
					another_client.setLastName(lastName);
					Client spied_client = spy(another_client);
					// set different id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(A_CLIENT_UUID);
					
					assertThatNoException().isThrownBy(() -> clientRepository.save(spied_client));
					
					assertThat(readAllClientsFromDatabase())
						.containsExactlyInAnyOrder(client, spied_client);
				}

				@Test
				@DisplayName("Updating in the same context is valid")
				void testSaveWhenUpdatingInTheSameContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					// update
					client.setFirstName(ANOTHER_FIRSTNAME);
					client.setLastName(ANOTHER_LASTNAME);
					
					Client returnedClient = clientRepository.save(client);
					
					// verify
					assertThat(returnedClient).isEqualTo(client)
						.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
						.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME)
						.hasFieldOrPropertyWithValue(ID_FIELD, A_CLIENT_UUID);
					assertThat(readAllClientsFromDatabase())
						.singleElement().isEqualTo(client)
							.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
							.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME)
							.hasFieldOrPropertyWithValue(ID_FIELD, A_CLIENT_UUID);
				}

				@Test
				@DisplayName("Updating in another context is valid")
				void testSaveWhenUpdatingInAnotherContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					
					// update
					client.setFirstName(ANOTHER_FIRSTNAME);
					client.setLastName(ANOTHER_LASTNAME);
					
					Client returnedClient = clientRepository.save(client);
					
					// verify
					assertThat(returnedClient).isEqualTo(client)
						.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
						.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME)
						.hasFieldOrPropertyWithValue(ID_FIELD, A_CLIENT_UUID);
					assertThat(readAllClientsFromDatabase())
						.singleElement().isEqualTo(client)
							.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
							.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME)
							.hasFieldOrPropertyWithValue(ID_FIELD, A_CLIENT_UUID);
				}

				@Test
				@DisplayName("Client to update is no longer present in database")
				void testSaveWhenClientToUpdateIsNotInDatabaseShouldThrowAndNotInsert() {
					client.setId(A_CLIENT_UUID);
					
					assertThatThrownBy(() -> clientRepository.save(client))
						.isInstanceOf(UpdateFailureException.class)
						.hasMessage("Client to update is not longer present in the repository.");
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
				@DisplayName("The updating client has null names")
				@CsvSource( value = {"'null', 'Rossi'", "'Mario', 'null'", "'null', 'null'"},
						nullValues = {"null"}
				)
				void testSaveWhenTheUpdatingClientHasNullNamesShouldNotUpdateAndThrow(
						String firstName, String lastName) {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					// update
					client.setFirstName(firstName);
					client.setLastName(lastName);
					
					assertThatThrownBy(() -> clientRepository.save(client))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Client to save violates not-null constraints.");
					
					// verify
					assertThat(readAllClientsFromDatabase())
						.filteredOn(c -> Objects.equals(c.getId(), A_CLIENT_UUID))
						.singleElement()
							.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, A_FIRSTNAME)
							.hasFieldOrPropertyWithValue(LASTNAME_FIELD, A_LASTNAME);
				}

				@Test
				@DisplayName("Client update generates names collision")
				void testSaveWhenClientUpdateGeneratesNamesCollisionShouldNotUpdateAndThrow() {
					// populate DB
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					addTestClientToDatabaseInTheSameContext(another_client, ANOTHER_CLIENT_UUID);
					
					// update
					another_client.setFirstName(client.getFirstName());
					another_client.setLastName(client.getLastName());
					
					assertThatThrownBy(() -> clientRepository.save(another_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Client to save violates uniqueness constraints.");
					
					assertThat(readAllClientsFromDatabase())
						.filteredOn(c -> Objects.equals(c.getId(), ANOTHER_CLIENT_UUID))
						.singleElement()
							.hasFieldOrPropertyWithValue(FIRSTNAME_FIELD, ANOTHER_FIRSTNAME)
							.hasFieldOrPropertyWithValue(LASTNAME_FIELD, ANOTHER_LASTNAME);
				}
			}

			@Nested
			@DisplayName("Tests for 'delete'")
			class DeleteTest {

				@Test
				@DisplayName("Client was added in the same context")
				void testDeleteWhenClientWasAddedToTheDatabaseInTheSameContextShouldRemove() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					clientRepository.delete(client);
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@Test
				@DisplayName("Client was added in another context")
				void testDeleteWhenClientWasAddedToTheDatabaseInAnotherContextShouldRemove() {
					addTestClientToDatabaseInAnotherContext(client, A_CLIENT_UUID);
					
					clientRepository.delete(client);
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@Test
				@DisplayName("Client has never been inserted")
				void testDeleteWhenClientHasNeverBeenInsertedInDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					
					assertThatNoException().isThrownBy(() -> clientRepository.delete(another_client));
					
					assertThat(readAllClientsFromDatabase()).containsExactly(client);
				}

				@Test
				@DisplayName("Client has already been removed")
				void testDeleteWhenClientHasAlreadyBeenRemovedFromDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestClientToDatabaseInTheSameContext(client, A_CLIENT_UUID);
					another_client.setId(ANOTHER_CLIENT_UUID);
					
					assertThatNoException().isThrownBy(() -> clientRepository.delete(another_client));
					
					assertThat(readAllClientsFromDatabase()).containsExactly(client);
				}
			}

			private List<Client> readAllClientsFromDatabase() {
				return StreamSupport
						.stream(clientCollection.find().spliterator(), false)
						.toList();
			}
		}

		private void addTestClientToDatabaseInTheSameContext(Client client, UUID id) {
			client.setId(id);
			clientCollection.insertOne(session, client);
		}

		private void addTestClientToDatabaseInAnotherContext(Client client, UUID id) {
			client.setId(id);
			ClientSession another_session = mongoClient.startSession();
			clientCollection.insertOne(another_session, client);
			another_session.close();
		}
	}
}