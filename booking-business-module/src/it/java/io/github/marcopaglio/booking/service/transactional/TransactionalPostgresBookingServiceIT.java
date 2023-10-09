package io.github.marcopaglio.booking.service.transactional;

import static io.github.marcopaglio.booking.model.Client.CLIENT_TABLE_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Integration tests of TransactionalBookingService and PostgreSQL")
class TransactionalPostgresBookingServiceIT {
	private static final String A_LASTNAME = "Rossi";
	private static final String A_FIRSTNAME = "Mario";
	private static final String ANOTHER_LASTNAME = "De Lucia";
	private static final String ANOTHER_FIRSTNAME = "Maria";

	private static final UUID A_CLIENT_UUID = UUID.fromString("bc49bffa-0766-4e5d-90af-d8a6ef516df4");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("c01a64c2-73e6-4b70-808f-00f9bd82571d");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-09-05");

	private Client client, another_client;
	private Reservation reservation, another_reservation;

	private static EntityManagerFactory emf;
	private static TransactionHandlerFactory transactionHandlerFactory;
	private static ClientRepositoryFactory clientRepositoryFactory;
	private static ReservationRepositoryFactory reservationRepositoryFactory;
	private static TransactionPostgresManager transactionManager;

	private TransactionalBookingService service;

	@BeforeAll
	static void setupCollaborators() throws Exception {
		System.setProperty("db.port", System.getProperty("postgres.port", "5432"));
		System.setProperty("db.name", System.getProperty("postgres.name", "IntegrationTest_db"));
		emf = Persistence.createEntityManagerFactory("postgres-it");
		
		transactionHandlerFactory = new TransactionHandlerFactory();
		clientRepositoryFactory = new ClientRepositoryFactory();
		reservationRepositoryFactory = new ReservationRepositoryFactory();
		transactionManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
	}

	@BeforeEach
	void setUp() throws Exception {
		service = new TransactionalBookingService(transactionManager);
	}

	@AfterAll
	static void closeEmf() throws Exception {
		emf.close();
	}

	@Nested
	@DisplayName("Methods using only ClientPostgresRepository")
	class ClientPostgresRepositoryIT {

		@BeforeEach
		void setupDatabase() throws Exception {
			EntityManager em = emf.createEntityManager();
			
			// make sure we always start with a clean database
			em.getTransaction().begin();
			em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB).executeUpdate();
			em.getTransaction().commit();
			
			em.close();
		}

		@Nested
		@DisplayName("Tests for 'findAllClients'")
		class FindAllClientsIT {

			@Test
			@DisplayName("No clients to retrieve")
			void testFindAllClientsWhenThereAreNoClientsToRetrieveShouldReturnEmptyList() {
				assertThat(service.findAllClients()).isEmpty();
			}

			@Test
			@DisplayName("Several clients to retrieve")
			void testFindAllClientsWhenThereAreSeveralClientsToRetrieveShouldReturnClientsAsList() {
				populateClientDatabase();
				
				assertThat(service.findAllClients())
					.isEqualTo(Arrays.asList(client, another_client));
			}
		}

		private void populateClientDatabase() {
			initClients();
			saveClients();
		}

		private void initClients() {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
		}

		private void saveClients() {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			em.persist(client);
			em.persist(another_client);
			em.getTransaction().commit();
			em.close();
		}
	}

	@Nested
	@DisplayName("Methods using only ReservationPostgresRepository")
	class ReservationPostgresRepositoryIT {

		@BeforeEach
		void setupDatabase() throws Exception {
			EntityManager em = emf.createEntityManager();
			
			// make sure we always start with a clean database
			em.getTransaction().begin();
			em.createNativeQuery("TRUNCATE TABLE " + RESERVATION_TABLE_DB).executeUpdate();
			em.getTransaction().commit();
			
			em.close();
		}

		@Nested
		@DisplayName("Tests for 'findAllReservations'")
		class FindAllReservationsIT {

			@Test
			@DisplayName("No reservations to retrieve")
			void testFindAllReservationsWhenThereAreNoReservationsToRetrieveShouldReturnEmptyList() {
				System.out.println(UUID.randomUUID());
				System.out.println(UUID.randomUUID());
				
				assertThat(service.findAllReservations()).isEmpty();
			}

			@Test
			@DisplayName("Several reservations to retrieve")
			void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationAsList() {
				populateReservationDatabase();
				
				assertThat(service.findAllReservations())
					.isEqualTo(Arrays.asList(reservation, another_reservation));
			}
		}

		private void populateReservationDatabase() {
			initReservations();
			saveReservations();
		}

		private void initReservations() {
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		private void saveReservations() {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			em.persist(reservation);
			em.persist(another_reservation);
			em.getTransaction().commit();
			em.close();
		}
	}
}
