package io.github.marcopaglio.booking.service.transactional;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.CLIENT_ALREADY_EXISTS_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.CLIENT_NOT_FOUND_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.RESERVATION_ALREADY_EXISTS_ERROR_MSG;
import static io.github.marcopaglio.booking.service.transactional.TransactionalBookingService.RESERVATION_NOT_FOUND_ERROR_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests for TransactionalBookingService and PostgreSQL")
class TransactionalPostgresBookingServiceIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final UUID A_CLIENT_UUID = UUID.fromString("78bce42b-1d28-4c37-b0a2-3287d6a829ca");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("864f7928-049b-4c2a-bd87-9e52ca16afc5");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-09-05");

	private static EntityManagerFactory emf;

	private TransactionHandlerFactory transactionHandlerFactory;
	private ClientRepositoryFactory clientRepositoryFactory;
	private ReservationRepositoryFactory reservationRepositoryFactory;
	private TransactionPostgresManager transactionManager;

	private TransactionalBookingService service;

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	@BeforeAll
	static void setupEmf() throws Exception {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "IntegrationTest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
	}

	@BeforeEach
	void setUp() throws Exception {
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		service = new TransactionalBookingService(transactionManager);
		
		// make sure we always start with a clean database
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
	}

	@AfterAll
	static void closeEmf() throws Exception {
		emf.close();
	}

	@Nested
	@DisplayName("Methods using only ClientPostgresRepository")
	class ClientPostgresRepositoryIT {

		@BeforeEach
		void initClients() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		}

		@Nested
		@DisplayName("Integration tests for 'findAllClients'")
		class FindAllClientsIT {

			@Test
			@DisplayName("No clients to retrieve")
			void testFindAllClientsWhenThereAreNoClientsToRetrieveShouldReturnEmptyList() {
				assertThat(service.findAllClients()).isEmpty();
			}

			@Test
			@DisplayName("Several clients to retrieve")
			void testFindAllClientsWhenThereAreSeveralClientsToRetrieveShouldReturnClientsAsList() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
				assertThat(service.findAllClients())
					.isEqualTo(Arrays.asList(client, another_client));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findClient'")
		class FindClientIT {

			@Test
			@DisplayName("Client exists")
			void testFindClientWhenClientExistsShouldReturnTheClient() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
				assertThat(service.findClient(client.getId())).isEqualTo(client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testFindClientWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findClient(A_CLIENT_UUID))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findClientNamed'")
		class FindClientNamedIT {

			@Test
			@DisplayName("Client exists")
			void testFindClientNamedWhenClientExistsShouldReturnTheClient() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
				assertThat(service.findClientNamed(A_FIRSTNAME, A_LASTNAME)).isEqualTo(client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testFindClientNamedWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findClientNamed(A_FIRSTNAME, A_LASTNAME))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'insertNewClient'")
		class InsertNewClientIT {

			@Test
			@DisplayName("Client is new")
			void testInsertNewClientWhenClientDoesNotAlreadyExistShouldInsertAndReturnWithId() {
				Client clientInDB = service.insertNewClient(client);
				
				assertThat(clientInDB).isEqualTo(client);
				assertThat(clientInDB.getId()).isNotNull();
				assertThat(readAllClientsFromDatabase()).containsExactly(client);
			}

			@Test
			@DisplayName("Client already exists")
			void testInsertNewClientWhenClientAlreadyExistsShouldNotInsertAndThrow() {
				Client existingClient = new Client(A_FIRSTNAME, A_LASTNAME);
				addTestClientToDatabase(existingClient);
				UUID existingId = existingClient.getId();
				
				assertThatThrownBy(() -> service.insertNewClient(client))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(CLIENT_ALREADY_EXISTS_ERROR_MSG);
				
				List<Client> clientsInDB = readAllClientsFromDatabase();
				assertThat(clientsInDB).containsExactly(existingClient);
				assertThat(clientsInDB.get(0).getId()).isEqualTo(existingId);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'renameClient'")
		class RenameClientIT {

			@Test
			@DisplayName("A same name client doesn't exist")
			void testRenameClientWhenThereIsNoClientWithTheSameNewNamesShouldRenameAndReturnWithoutChangingId() {
				Client renamedClient = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
				addTestClientToDatabase(client);
				UUID client_id = client.getId();
				
				Client renamedClientInDB = service
						.renameClient(client_id, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
				assertThat(renamedClientInDB).isEqualTo(renamedClient);
				assertThat(renamedClientInDB.getId()).isEqualTo(client_id);
				
				assertThat(readAllClientsFromDatabase())
					.doesNotContain(client)
					.containsExactly(renamedClient);
			}

			@Test
			@DisplayName("A same name client already exists")
			void testRenameClientWhenThereIsAlreadyAClientWithSameNewNamesShouldNotRenameAndThrow() {
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				UUID client_id = client.getId();
				
				assertThatThrownBy(() -> service.renameClient(
						client_id, ANOTHER_FIRSTNAME, ANOTHER_LASTNAME))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(CLIENT_ALREADY_EXISTS_ERROR_MSG);
				
				assertThat(readAllClientsFromDatabase())
					.containsExactlyInAnyOrder(client, another_client)
					.filteredOn((c) -> c.getId().equals(client_id)).containsOnly(client);
			}
		}
	}

	@Nested
	@DisplayName("Methods using only ReservationPostgresRepository")
	class ReservationPostgresRepositoryIT {

		@BeforeEach
		void initReservations() throws Exception {
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		@Nested
		@DisplayName("Integration tests for 'findAllReservations'")
		class FindAllReservationsIT {

			@Test
			@DisplayName("No reservations to retrieve")
			void testFindAllReservationsWhenThereAreNoReservationsToRetrieveShouldReturnEmptyList() {
				assertThat(service.findAllReservations()).isEmpty();
			}

			@Test
			@DisplayName("Several reservations to retrieve")
			void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationAsList() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				
				assertThat(service.findAllReservations())
					.isEqualTo(Arrays.asList(reservation, another_reservation));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findReservation'")
		class FindReservationIT {

			@Test
			@DisplayName("Reservation exists")
			void testFindReservationWhenReservationExistsShouldReturnTheReservation() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				
				assertThat(service.findReservation(reservation.getId())).isEqualTo(reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testFindReservationWhenReservationDoesNotExistShouldThrow() {
				UUID notPresentId = UUID.fromString("dba11377-e1b2-40ff-8ad7-b7a783b316e1");
				assertThatThrownBy(() -> service.findReservation(notPresentId))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'findReservationOn'")
		class FindReservationOnIT {

			@Test
			@DisplayName("Reservation exists")
			void testFindReservationOnWhenReservationExistsShouldReturnTheReservation() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				
				assertThat(service.findReservationOn(A_LOCALDATE)).isEqualTo(reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testFindReservationOnWhenReservationDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.findReservationOn(A_LOCALDATE))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'rescheduleReservation'")
		class RescheduleReservationIT {

			@Test
			@DisplayName("A same date reservation doesn't exist")
			void testRescheduleReservationWhenThereIsNoReservationInTheSameNewDateShouldRescheduleAndReturnWithoutChangingId() {
				Reservation rescheduledReservation = new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE);
				addTestReservationToDatabase(reservation);
				UUID reservation_id = reservation.getId();
				
				Reservation rescheduledReservationInDB =
						service.rescheduleReservation(reservation_id, ANOTHER_LOCALDATE);
				assertThat(rescheduledReservationInDB).isEqualTo(rescheduledReservation);
				assertThat(rescheduledReservationInDB.getId()).isEqualTo(reservation_id);
				
				assertThat(readAllReservationsFromDatabase())
					.doesNotContain(reservation)
					.containsExactly(rescheduledReservation);
			}

			@Test
			@DisplayName("A same date reservation already exists")
			void testRescheduleReservationWhenThereIsAlreadyAReservationInTheSameNewDateShouldNotRescheduleAndThrow() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				UUID reservation_id = reservation.getId();
				
				assertThatThrownBy(
						() -> service.rescheduleReservation(reservation_id, ANOTHER_LOCALDATE))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
				
				assertThat(readAllReservationsFromDatabase())
					.containsExactlyInAnyOrder(reservation, another_reservation)
					.filteredOn((r) -> r.getId().equals(reservation_id)).containsOnly(reservation);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'removeReservation'")
		class RemoveReservationIT {

			@Test
			@DisplayName("Reservation exists")
			void testRemoveReservationWhenReservationExistsShouldRemove() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				
				service.removeReservation(reservation.getId());
				
				assertThat(readAllReservationsFromDatabase())
					.doesNotContain(reservation)
					.containsExactly(another_reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testRemoveReservationWhenReservationDoesNotExistShouldThrow() {
				UUID notPresentId = UUID.fromString("e54a2fbd-85bc-4493-b540-5630d3e501ad");
				assertThatThrownBy(() -> service.removeReservation(notPresentId))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'removeReservationOn'")
		class RemoveReservationOnIT {

			@Test
			@DisplayName("Reservation exists")
			void testRemoveReservationOnWhenReservationExistsShouldRemove() {
				addTestReservationToDatabase(reservation);
				addTestReservationToDatabase(another_reservation);
				
				service.removeReservationOn(A_LOCALDATE);
				
				assertThat(readAllReservationsFromDatabase())
					.doesNotContain(reservation)
					.containsExactly(another_reservation);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testRemoveReservationOnWhenReservationDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.removeReservationOn(A_LOCALDATE))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(RESERVATION_NOT_FOUND_ERROR_MSG);
			}
		}
	}

	@Nested
	@DisplayName("Methods using both repositories")
	class BothRepositoriesIT {

		@BeforeEach
		void initEntities() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		@Nested
		@DisplayName("Integration tests for 'insertNewReservation'")
		class InsertNewReservationIT {

			@DisplayName("Reservation is new and client exists")
			@Test
			void testInsertNewReservationWhenReservationIsNewAndAssociatedClientExistsShouldInsertAndReturnWithId() {
				addTestClientToDatabase(client);
				reservation.setClientId(client.getId());
				
				Reservation reservationInDB = service.insertNewReservation(reservation);
				
				assertThat(reservationInDB).isEqualTo(reservation);
				assertThat(reservationInDB.getId()).isNotNull();
				assertThat(readAllReservationsFromDatabase()).containsExactly(reservationInDB);
			}

			@Test
			@DisplayName("Reservation already exists")
			void testInsertNewReservationWhenReservationAlreadyExistsShouldNotInsertAndThrow() {
				Reservation existingReservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
				addTestReservationToDatabase(existingReservation);
				UUID existingId = existingReservation.getId();
				
				assertThatThrownBy(() -> service.insertNewReservation(reservation))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
				
				List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
				assertThat(reservationsInDB).containsExactly(existingReservation);
				assertThat(reservationsInDB.get(0).getId()).isEqualTo(existingId);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'removeClient'")
		class RemoveClientIT {

			@Test
			@DisplayName("Client exists with existing reservation")
			void testRemoveClientWhenClientExistsWithAnExistingReservationShouldRemove() {
				addTestClientToDatabase(client);
				UUID client_id = client.getId();
				reservation.setClientId(client_id);
				addTestReservationToDatabase(reservation);
				addTestClientToDatabase(another_client);
				another_reservation.setClientId(another_client.getId());
				addTestReservationToDatabase(another_reservation);
				
				service.removeClient(client_id);
				
				assertThat(readAllReservationsFromDatabase())
					.filteredOn((r) -> r.getClientId() == client_id).isEmpty();
				assertThat(readAllClientsFromDatabase())
					.doesNotContain(client)
					.containsExactly(another_client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testRemoveClientWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.removeClient(A_CLIENT_UUID))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}

		@Nested
		@DisplayName("Integration tests for 'removeClientNamed'")
		class RemoveClientNamedIT {

			@Test
			@DisplayName("Client exists with existing reservation")
			void testRemoveClientNamedWhenClientExistsWithAnExistingReservationShouldRemove() {
				addTestClientToDatabase(client);
				UUID client_id = client.getId();
				reservation.setClientId(client_id);
				addTestReservationToDatabase(reservation);
				addTestClientToDatabase(another_client);
				another_reservation.setClientId(another_client.getId());
				addTestReservationToDatabase(another_reservation);
				
				service.removeClientNamed(A_FIRSTNAME, A_LASTNAME);
				
				assertThat(readAllReservationsFromDatabase())
					.filteredOn((r) -> r.getClientId() == client_id).isEmpty();
				assertThat(readAllClientsFromDatabase())
					.doesNotContain(client)
					.containsExactly(another_client);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testRemoveClientNamedWhenClientDoesNotExistShouldThrow() {
				assertThatThrownBy(() -> service.removeClientNamed(A_FIRSTNAME, A_LASTNAME))
					.isInstanceOf(InstanceNotFoundException.class)
					.hasMessage(CLIENT_NOT_FOUND_ERROR_MSG);
			}
		}
	}

	private List<Client> readAllClientsFromDatabase() {
		EntityManager em = emf.createEntityManager();
		List<Client> clientsInDB = em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
		em.close();
		return clientsInDB;
	}

	private List<Reservation> readAllReservationsFromDatabase() {
		EntityManager em = emf.createEntityManager();
		List<Reservation> reservationsInDB = em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
		em.close();
		return reservationsInDB;
	}

	private void addTestClientToDatabase(Client client) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(client);
		em.getTransaction().commit();
		em.close();
	}

	private void addTestReservationToDatabase(Reservation reservation) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(reservation);
		em.getTransaction().commit();
		em.close();
	}
}
