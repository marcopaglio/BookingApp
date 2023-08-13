package io.github.marcopaglio.booking.repository.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;

@DisplayName("Tests for ClientPostegresRepository class")
@Testcontainers
class ClientPostgresRepositoryTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final UUID A_CLIENT_UUID = UUID.fromString("95021d62-9787-44e7-87ff-3edc17761b71");
	
	private static final String ANOTHER_FIRSTNAME = "Maria";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("2a942c02-642d-4cc1-b199-c11f6405bba2");

	//@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
		.withDatabaseName("ClientPostgresRepositoryTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private ClientPostgresRepository clientRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		postgreSQLContainer.start();
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", postgreSQLContainer.getDatabaseName());
		
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}

	@BeforeEach
	void setUp() throws Exception {
		// start a new EM for communicating with the DB
		em = emf.createEntityManager();
		
		clientRepository = new ClientPostgresRepository(em);
		
		// make sure we always start with a clean database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
	}

	@AfterEach
	void tearDown() throws Exception {
		em.clear();
		em.close();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		emf.close();
		postgreSQLContainer.stop();
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
	@DisplayName("Tests that read the database")
	class ReadDBTest {
		private Client client;
		private Client another_client;

		@BeforeEach
		void resetClientId() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
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
			@DisplayName("Database has been filled in the same context")
			void testFindAllWhenDatabaseHasBeenFilledInTheSameContextShouldReturnClientsAsList() {
				addTestClientToDatabaseInTheSameContext(client);
				addTestClientToDatabaseInTheSameContext(another_client);
				
				assertThat(clientRepository.findAll())
					.containsExactlyInAnyOrder(client, another_client);
			}

			@Test
			@DisplayName("Database has been filled in another context")
			void testFindAllWhenDatabaseHasBeenFilledInAnotherContextShouldReturnClientsAsList() {
				addTestClientToDatabaseInAnotherContext(client);
				addTestClientToDatabaseInAnotherContext(another_client);
				
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
				addTestClientToDatabaseInTheSameContext(client);
				addTestClientToDatabaseInTheSameContext(another_client);
				
				assertThat(clientRepository.findById(another_client.getId()))
					.isEqualTo(Optional.of(another_client));
			}

			@Test
			@DisplayName("Client was added in another context")
			void testFindByIdWhenClientWasAddedToTheDatabaseInAnotherContextShouldReturnOptionalOfClient() {
				addTestClientToDatabaseInAnotherContext(client);
				addTestClientToDatabaseInAnotherContext(another_client);
				
				assertThat(clientRepository.findById(another_client.getId()))
					.isEqualTo(Optional.of(another_client));
			}
		}

		@Nested
		@DisplayName("Tests for 'findByName'")
		class FindByNameTest {

			@Test
			@DisplayName("Client is not in database")
			void testFindByNameWhenClientIsNotInDatabaseShouldReturnOptionalOfEmpty() {
				assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME)).isEmpty();
			}

			@Test
			@DisplayName("Client was added in the same context")
			void testFindByNameWhenClientWasAddedToTheDatabaseInTheSameContextShouldReturnOptionalOfClient() {
				addTestClientToDatabaseInTheSameContext(client);
				addTestClientToDatabaseInTheSameContext(another_client);
				
				assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(Optional.of(client));
			}

			@Test
			@DisplayName("Client was added in another context")
			void testFindByNameWhenClientWasAddedToTheDatabaseInAnotherContextShouldReturnOptionalOfClient() {
				addTestClientToDatabaseInAnotherContext(client);
				addTestClientToDatabaseInAnotherContext(another_client);
				
				assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(Optional.of(client));
			}

			@Test
			@DisplayName("Another same name client in database")
			void testFindByNameWhenThereIsAnotherClientWithTheSameNameShouldNotMatchIt() {
				addTestClientToDatabaseInTheSameContext(client);
				
				assertThat(clientRepository.findByName(A_FIRSTNAME, ANOTHER_LASTNAME))
					.isNotEqualTo(Optional.of(client));
			}

			@Test
			@DisplayName("Another same surname client in database")
			void testFindByNameWhenThereIsAnotherClientWithTheSameSurnameShouldNotMatchIt() {
				addTestClientToDatabaseInTheSameContext(client);
				
				assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, A_LASTNAME))
					.isNotEqualTo(Optional.of(client));
			}
		}

		@Nested
		@DisplayName("Tests that modify the database")
		class ModifyDBTest {

			@Nested
			@DisplayName("Tests for 'save'")
			class SaveTest {

				@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
				@DisplayName("Client with null names")
				@CsvSource( value = {"'null', 'Rossi'", "'Mario', 'null'", "'null', 'null'"},
						nullValues = {"null"}
				)
				void testSaveWhenClientHasNullNamesShouldNotInsertAndThrow(
						String firstName, String lastName) {
					client.setFirstName(firstName);
					client.setLastName(lastName);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> clientRepository.save(client))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Client to save must have both not-null names.");
					em.getTransaction().commit();
					
					assertThat(readAllClientsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("New client is valid")
				void testSaveWhenNewClientIsValidShouldInsertAndReturnTheClientWithId() {
					em.getTransaction().begin();
					Client returnedClient = clientRepository.save(client);
					em.getTransaction().commit();
					
					List<Client> clientsInDB = readAllClientsFromDatabase();
					assertThat(clientsInDB).containsExactly(client);
					assertThat(returnedClient).isEqualTo(client);
					assertThat(clientsInDB.get(0).getId()).isNotNull();
					assertThat(returnedClient.getId()).isNotNull();
					assertThat(clientsInDB.get(0).getId()).isEqualTo(returnedClient.getId());
				}
/*
				@Test
				@DisplayName("New client generates names collision")
				void testSaveWhenNewClientGeneratesANamesCollisionShouldNotInsertAndThrow() {
					addTestClientToDatabase(client);
					
					another_client.setFirstName(client.getFirstName());
					another_client.setLastName(client.getLastName());
					
					assertThatThrownBy(() -> {
						em.getTransaction().begin();
						clientRepository.save(another_client);
						em.getTransaction().commit();
					}).isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("The insertion violates uniqueness constraints.");
					
					
					List<Client> clientsInDB = readAllClientsFromDatabase();
					assertThat(clientsInDB).containsExactly(client);
					assertThat(clientsInDB.get(0).getId()).isEqualTo(client.getId());
				}

				@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
				@DisplayName("New client has some equalities")
				@CsvSource({
					"'Mario', 'De Lucia'",	// same name
					"'Maria', 'Rossi'"		// same surname
				})
				void testSaveWhenNewClientHasSomeEqualitiesShouldNotThrowAndInsert(
						String firstName, String lastName) {
					addTestClientToDatabase(client, A_CLIENT_UUID);
					
					another_client.setFirstName(firstName);
					another_client.setLastName(lastName);
					Client spied_client = spy(another_client);
					// sets different id
					doAnswer(invocation -> {
						((Client) invocation.getMock()).setId(ANOTHER_CLIENT_UUID);
						return null;
					}).when(spied_client).setId(A_CLIENT_UUID);
					
					assertThatNoException().isThrownBy(() -> clientRepository.save(spied_client));
					
					assertThat(readAllClientsFromDatabase())
						.containsExactlyInAnyOrder(client, spied_client);
				}
*/
				@Test
				@DisplayName("Updating in the same context is valid")
				void testSaveWhenUpdatingInTheSameContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestClientToDatabaseInTheSameContext(client);
					UUID initialId = client.getId();
					
					// update
					client.setFirstName(ANOTHER_FIRSTNAME);
					client.setLastName(ANOTHER_LASTNAME);
					
					em.getTransaction().begin();
					Client returnedClient = clientRepository.save(client);
					em.getTransaction().commit();
					
					// verify
					List<Client> clientsInDB = readAllClientsFromDatabase();
					assertThat(clientsInDB).containsExactly(client);
					assertThat(returnedClient).isEqualTo(client);
					assertThat(clientsInDB.get(0).getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
					assertThat(returnedClient.getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
					assertThat(clientsInDB.get(0).getLastName()).isEqualTo(ANOTHER_LASTNAME);
					assertThat(returnedClient.getLastName()).isEqualTo(ANOTHER_LASTNAME);
					assertThat(clientsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(returnedClient.getId()).isEqualTo(initialId);
				}

				@Test
				@DisplayName("Updating in another context is valid")
				void testSaveWhenUpdatingInAnotherContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestClientToDatabaseInAnotherContext(client);
					UUID initialId = client.getId();
					
					// update
					client.setFirstName(ANOTHER_FIRSTNAME);
					client.setLastName(ANOTHER_LASTNAME);
					
					em.getTransaction().begin();
					Client returnedClient = clientRepository.save(client);
					em.getTransaction().commit();
					
					// verify
					List<Client> clientsInDB = readAllClientsFromDatabase();
					assertThat(clientsInDB).containsExactly(client);
					assertThat(returnedClient).isEqualTo(client);
					assertThat(clientsInDB.get(0).getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
					assertThat(returnedClient.getFirstName()).isEqualTo(ANOTHER_FIRSTNAME);
					assertThat(clientsInDB.get(0).getLastName()).isEqualTo(ANOTHER_LASTNAME);
					assertThat(returnedClient.getLastName()).isEqualTo(ANOTHER_LASTNAME);
					assertThat(clientsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(returnedClient.getId()).isEqualTo(initialId);
				}
/*
				@Test
				@DisplayName("Updated client is no longer present in database")
				void testSaveWhenUpdatedClientIsNotInDatabaseShouldNotInsertAndNotThrow() {
					client.setId(A_CLIENT_UUID);
					
					assertThatNoException().isThrownBy(() -> clientRepository.save(client));
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@Test
				@DisplayName("Updated client generates names collision")
				void testSaveWhenUpdatedClientGeneratesNamesCollisionShouldNotUpdateAndThrow() {
					// populate DB
					addTestClientToDatabase(client, A_CLIENT_UUID);
					addTestClientToDatabase(another_client, ANOTHER_CLIENT_UUID);
					
					// update
					another_client.setFirstName(A_FIRSTNAME);
					another_client.setLastName(A_LASTNAME);
					
					assertThatThrownBy(() -> clientRepository.save(another_client))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("The update violates uniqueness constraints.");
					
					Set<String> namesInDB = new HashSet<>();
					readAllClientsFromDatabase().forEach((c) -> namesInDB.add(c.getFirstName()));
					assertThat(namesInDB).contains(ANOTHER_FIRSTNAME);
					Set<String> surnamesInDB = new HashSet<>();
					readAllClientsFromDatabase().forEach((c) -> surnamesInDB.add(c.getLastName()));
					assertThat(surnamesInDB).contains(ANOTHER_LASTNAME);
				}*/
			}

			@Nested
			@DisplayName("Tests for 'delete'")
			class DeleteTest {

				@Test
				@DisplayName("Client was added in the same context")
				void testDeleteWhenClientWasAddedToTheDatabaseInTheSameContextShouldRemove() {
					addTestClientToDatabaseInTheSameContext(client);
					
					em.getTransaction().begin();
					clientRepository.delete(client);
					em.getTransaction().commit();
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@Test
				@DisplayName("Client was added to database in another context")
				void testDeleteWhenClientWasAddedToTheDatabaseInAnotherContextShouldRemove() {
					addTestClientToDatabaseInAnotherContext(client);
					
					em.getTransaction().begin();
					clientRepository.delete(client);
					em.getTransaction().commit();
					
					assertThat(readAllClientsFromDatabase()).doesNotContain(client);
				}

				@Test
				@DisplayName("Client has never been inserted")
				void testDeleteWhenClientHasNeverBeenInsertedInDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestClientToDatabaseInTheSameContext(client);
					
					em.getTransaction().begin();
					assertThatNoException().isThrownBy(() -> clientRepository.delete(another_client));
					em.getTransaction().commit();
					
					assertThat(readAllClientsFromDatabase()).containsExactly(client);
				}

				@Test
				@DisplayName("Client has already been removed")
				void testDeleteWhenClientHasAlreadyBeenRemovedFromDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestClientToDatabaseInTheSameContext(client);
					UUID another_uuid;
					do {
						another_uuid = UUID.randomUUID();
					} while (another_uuid == client.getId());
					another_client.setId(another_uuid);
					
					em.getTransaction().begin();
					assertThatNoException().isThrownBy(() -> clientRepository.delete(another_client));
					em.getTransaction().commit();
					
					assertThat(readAllClientsFromDatabase()).containsExactly(client);
				}
			}

			private List<Client> readAllClientsFromDatabase() {
				return em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
			}
		}

		private void addTestClientToDatabaseInTheSameContext(Client client) {
			em.getTransaction().begin();
			em.persist(client);
			em.getTransaction().commit();
		}

		private void addTestClientToDatabaseInAnotherContext(Client client) {
			EntityManager anotherEm = emf.createEntityManager();
			anotherEm.getTransaction().begin();
			anotherEm.persist(client);
			anotherEm.getTransaction().commit();
			anotherEm.clear();
			anotherEm.close();
		}
	}
}
