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

	private static final UUID A_CLIENT_UUID = UUID.fromString("78bce42b-1d28-4c37-b0a2-3287d6a829ca");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("864f7928-049b-4c2a-bd87-9e52ca16afc5");
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
		EntityManager em = emf.createEntityManager();
		// make sure we always start with a clean database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + CLIENT_TABLE_DB + "," + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		em.close();
		
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
		void initClients() throws Exception {
			client = new Client(A_FIRSTNAME, A_LASTNAME);
			another_client = new Client(ANOTHER_FIRSTNAME, ANOTHER_LASTNAME);
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
				addTestClientToDatabase(client);
				addTestClientToDatabase(another_client);
				
				assertThat(service.findAllClients())
					.isEqualTo(Arrays.asList(client, another_client));
			}
		}

		private void addTestClientToDatabase(Client client) {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			em.persist(client);
			em.getTransaction().commit();
			em.close();
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
		@DisplayName("Tests for 'findAllReservations'")
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

		private void addTestReservationToDatabase(Reservation reservation) {
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();
			em.persist(reservation);
			em.getTransaction().commit();
			em.close();
		}
	}
}
