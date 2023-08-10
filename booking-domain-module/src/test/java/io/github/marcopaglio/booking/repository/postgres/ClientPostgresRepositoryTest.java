package io.github.marcopaglio.booking.repository.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
		.withDatabaseName("ClientPostgresRepositoryTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private ClientPostgresRepository clientRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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
			@DisplayName("Database is not empty")
			void testFindAllWhenDatabaseIsNotEmptyShouldReturnClientsAsList() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
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
			@DisplayName("Client is in database")
			void testFindByIdWhenClientIsInDatabaseShouldReturnOptionalOfClient() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
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
			@DisplayName("Another same name client in database")
			void testFindByNameWhenThereIsAnotherClientWithTheSameNameShouldNotMatchIt() {
				addTestClientToDatabase(client);
				
				assertThat(clientRepository.findByName(A_FIRSTNAME, ANOTHER_LASTNAME))
					.isNotEqualTo(Optional.of(client));
			}

			@Test
			@DisplayName("Another same surname client in database")
			void testFindByNameWhenThereIsAnotherClientWithTheSameSurnameShouldNotMatchIt() {
				addTestClientToDatabase(client);
				
				assertThat(clientRepository.findByName(ANOTHER_FIRSTNAME, A_LASTNAME))
					.isNotEqualTo(Optional.of(client));
			}

			@Test
			@DisplayName("Client is in database")
			void testFindByNameWhenClientIsInDatabaseShouldReturnOptionalOfClient() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
				assertThat(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
					.isEqualTo(Optional.of(client));
			}
		}
		
		private void addTestClientToDatabase(Client client) {
			em.getTransaction().begin();
			em.persist(client);
			em.getTransaction().commit();
		}
	}
}
