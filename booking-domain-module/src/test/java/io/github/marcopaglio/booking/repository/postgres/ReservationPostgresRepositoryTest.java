package io.github.marcopaglio.booking.repository.postgres;

import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.model.Reservation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@DisplayName("Tests for ReservationPostegresRepository class")
@Testcontainers
class ReservationPostgresRepositoryTest {
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final UUID A_CLIENT_UUID = UUID.fromString("5c4d31a2-be04-4156-aa15-f86e7a916999");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-12-22");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("6f4261e2-2d5e-4ada-93f6-67dc7e7b6358");
	private static final UUID A_RESERVATION_UUID = UUID.fromString("d96af73a-efbc-45d7-a013-15e4f0c3a8fd");

	@Container
	private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
		.withDatabaseName("ReservationPostgresRepositoryTest_db")
		.withUsername("postgres-test")
		.withPassword("postgres-test");

	private static EntityManagerFactory emf;
	private EntityManager em;

	private ReservationPostgresRepository reservationRepository;

	@BeforeAll
	static void setupServer() throws Exception {
		System.setProperty("db.port", postgreSQLContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", postgreSQLContainer.getDatabaseName());
		
		emf = Persistence.createEntityManagerFactory("postgres-test");
	}

	@BeforeEach
	void setUp() throws Exception {
		// start a new EM for communicating with the DB
		em = emf.createEntityManager();
		
		// make sure we always start with a clean database
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE TABLE " + RESERVATION_TABLE_DB).executeUpdate();
		em.getTransaction().commit();
		
		// repository creation
		reservationRepository = new ReservationPostgresRepository(em);
	}

	@AfterEach
	void closeHandler() throws Exception {
		em.close();
	}

	@AfterAll
	static void closeClient() throws Exception {
		emf.close();
	}

	@Nested
	@DisplayName("Null inputs on methods")
	class NullInputTest {

		@Test
		@DisplayName("Null clientId on 'findByClient'")
		void testFindByClientWhenClientIdIsNullShouldReturnEmptyList() {
			assertThat(reservationRepository.findByClient(null)).isEmpty();
		}

		@Test
		@DisplayName("Null id on 'findById'")
		void testFindByIdWhenIdIsNullShouldReturnOptionalOfEmpty() {
			assertThat(reservationRepository.findById(null)).isEmpty();
		}

		@Test
		@DisplayName("Null date on 'findByDate'")
		void testFindByDateWhenDateIsNullShouldReturnOptionalOfEmpty() {
			assertThat(reservationRepository.findByDate(null)).isEmpty();
		}

		@Test
		@DisplayName("Null reservation on 'save'")
		void testSaveWhenReservationIsNullShouldThrow() {
			assertThatThrownBy(() -> reservationRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to save cannot be null.");
		}

		@Test
		@DisplayName("Null reservation on 'delete'")
		void testDeleteWhenReservationIsNullShouldThrow() {
			assertThatThrownBy(() -> reservationRepository.delete(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to delete cannot be null.");
		}
	}

	@Nested
	@DisplayName("Using entities")
	class UsingEntitiesTest {
		private Reservation reservation;
		private Reservation another_reservation;

		@BeforeEach
		void initReservations() throws Exception {
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
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
					assertThat(reservationRepository.findAll()).isEmpty();
				}

				@Test
				@DisplayName("Database has been filled in the same context")
				void testFindAllWhenDatabaseHasBeenFilledInTheSameContextShouldReturnReservationsAsList() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					assertThat(reservationRepository.findAll())
						.containsExactlyInAnyOrder(reservation, another_reservation);
				}

				@Test
				@DisplayName("Database has been filled in another context")
				void testFindAllWhenDatabaseHasBeenFilledInAnotherContextShouldReturnReservationsAsList() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					addTestReservationToDatabaseInAnotherContext(another_reservation);
					
					assertThat(reservationRepository.findAll())
						.containsExactlyInAnyOrder(reservation, another_reservation);
				}
			}

			@Nested
			@DisplayName("Tests for 'findByClient'")
			class FindByClientTest {

				@Test
				@DisplayName("No associated reservations")
				void testFindByClientWhenThereAreNoAssociatedReservationsShouldReturnEmptyList() {
					assertThat(reservationRepository.findByClient(A_CLIENT_UUID)).isEmpty();
				}

				@Test
				@DisplayName("Single associated reservation added in the same context")
				void testFindByClientWhenThereIsASingleAssociatedReservationAddedInTheSameContextShouldReturnTheReservationAsList() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					assertThat(reservationRepository.findByClient(A_CLIENT_UUID))
						.containsExactly(reservation);
				}

				@Test
				@DisplayName("Single associated reservation added in another context")
				void testFindByClientWhenThereIsASingleAssociatedReservationAddedInAnotherContextShouldReturnTheReservationAsList() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					addTestReservationToDatabaseInAnotherContext(another_reservation);
					
					assertThat(reservationRepository.findByClient(A_CLIENT_UUID))
						.containsExactly(reservation);
				}

				@Test
				@DisplayName("Several associated reservation added in the same context")
				void testFindByClientWhenThereAreSeveralAssociatedReservationsAddedInTheSameContextShouldReturnReservationsAsList() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					another_reservation.setClientId(A_CLIENT_UUID);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					assertThat(reservationRepository.findByClient(A_CLIENT_UUID))
						.containsExactly(reservation, another_reservation);
				}

				@Test
				@DisplayName("Several associated reservation added in another context")
				void testFindByClientWhenThereAreSeveralAssociatedReservationsAddedInAnotherContextShouldReturnReservationsAsList() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					another_reservation.setClientId(A_CLIENT_UUID);
					addTestReservationToDatabaseInAnotherContext(another_reservation);
					
					assertThat(reservationRepository.findByClient(A_CLIENT_UUID))
						.containsExactly(reservation, another_reservation);
				}
			}

			@Nested
			@DisplayName("Tests for 'findById'")
			class FindByIdTest {

				@Test
				@DisplayName("Reservation is not in database")
				void testFindByIdWhenReservationIsNotInDatabaseShouldReturnOptionalOfEmpty() {
					assertThat(reservationRepository.findById(A_RESERVATION_UUID)).isEmpty();
				}

				@Test
				@DisplayName("Reservation was added in the same context")
				void testFindByIdWhenReservationWasAddedInTheSameContextShouldReturnOptionalOfReservation() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					assertThat(reservationRepository.findById(reservation.getId()))
						.isEqualTo(Optional.of(reservation));
				}

				@Test
				@DisplayName("Reservation was added in another context")
				void testFindByIdWhenReservationWasAddedInAnotherContextShouldReturnOptionalOfReservation() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					addTestReservationToDatabaseInAnotherContext(another_reservation);
					
					assertThat(reservationRepository.findById(reservation.getId()))
						.isEqualTo(Optional.of(reservation));
				}
			}

			@Nested
			@DisplayName("Tests for 'findByDate'")
			class FindByDate {

				@Test
				@DisplayName("Reservation is not in database")
				void testFindByDateWhenReservationIsNotInDatabaseShouldReturnOptionalOfEmpty() {
					assertThat(reservationRepository.findByDate(A_LOCALDATE)).isEmpty();
				}

				@Test
				@DisplayName("Reservation was added in the same context")
				void testFindByDateWhenReservationWasAddedInTheSameContextShouldReturnOptionalOfReservation() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					assertThat(reservationRepository.findByDate(A_LOCALDATE))
						.isEqualTo(Optional.of(reservation));
				}

				@Test
				@DisplayName("Reservation was added in another context")
				void testFindByDateWhenReservationWasAddedInAnotherContextShouldReturnOptionalOfReservation() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					addTestReservationToDatabaseInAnotherContext(another_reservation);
					
					assertThat(reservationRepository.findByDate(A_LOCALDATE))
						.isEqualTo(Optional.of(reservation));
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
				@DisplayName("New reservation is valid")
				void testSaveWhenNewReservationIsValidShouldInsertAndReturnTheReservationWithId() {
					em.getTransaction().begin();
					Reservation returnedReservation = reservationRepository.save(reservation);
					em.getTransaction().commit();
					
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(returnedReservation).isEqualTo(reservation);
					assertThat(reservationsInDB.get(0).getId()).isNotNull();
					assertThat(returnedReservation.getId()).isNotNull();
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(returnedReservation.getId());
				}

				@Test
				@DisplayName("New reservation has null clientId")
				void testSaveWhenReservationHasNullClientIdShouldNotInsertAndThrow() {
					reservation.setClientId(null);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save violates not-null constraints.");
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("New reservation has null date")
				void testSaveWhenReservationHasNullDateShouldNotInsertAndThrow() {
					reservation.setDate(null);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save violates not-null constraints.");
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("New reservation generates date collision in the same context")
				void testSaveWhenNewReservationGeneratesADateCollisionInTheSameContextShouldNotInsertAndThrow() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					
					another_reservation.setDate(A_LOCALDATE);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(another_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					em.getTransaction().commit();
					
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(reservation.getId());
				}

				@Test
				@DisplayName("New reservation generates date collision in another context")
				void testSaveWhenNewReservationGeneratesADateCollisionInAnotherContextShouldNotInsertAndThrow() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					
					another_reservation.setDate(A_LOCALDATE);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(another_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					em.getTransaction().commit();
					
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(reservation.getId());
				}

				@Test
				@DisplayName("New reservation has same client")
				void testSaveWhenNewReservationHasSameClientShouldNotThrowAndInsert() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					
					another_reservation.setClientId(A_CLIENT_UUID);
					
					em.getTransaction().begin();
					assertThatNoException().isThrownBy(
							() -> reservationRepository.save(another_reservation));
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase())
						.containsExactlyInAnyOrder(reservation, another_reservation);
				}

				@Test
				@DisplayName("Updating in the same context is valid")
				void testSaveWhenUpdatingInTheSameContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestReservationToDatabaseInTheSameContext(reservation);
					UUID initialId = reservation.getId();
					
					// update
					reservation.setClientId(ANOTHER_CLIENT_UUID);
					reservation.setDate(ANOTHER_LOCALDATE);
					
					em.getTransaction().begin();
					Reservation returnedReservation = reservationRepository.save(reservation);
					em.getTransaction().commit();
					
					// verify
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(returnedReservation).isEqualTo(reservation);
					assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(returnedReservation.getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(reservationsInDB.get(0).getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(returnedReservation.getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(returnedReservation.getId()).isEqualTo(initialId);
				}

				@Test
				@DisplayName("Updating in another context is valid")
				void testSaveWhenUpdatingInAnotherContextIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestReservationToDatabaseInAnotherContext(reservation);
					UUID initialId = reservation.getId();
					
					// update
					reservation.setClientId(ANOTHER_CLIENT_UUID);
					reservation.setDate(ANOTHER_LOCALDATE);
					
					em.getTransaction().begin();
					Reservation returnedReservation = reservationRepository.save(reservation);
					em.getTransaction().commit();
					
					// verify
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(returnedReservation).isEqualTo(reservation);
					assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(returnedReservation.getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(reservationsInDB.get(0).getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(returnedReservation.getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(returnedReservation.getId()).isEqualTo(initialId);
				}

				@Test
				@DisplayName("Reservation to update is no longer present in database")
				void testSaveWhenReservationToUpdateIsNotInDatabaseShouldThrowAndNotInsert() {
					reservation.setId(A_RESERVATION_UUID);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(UpdateFailureException.class)
						.hasMessage("Reservation to update is not longer present in the repository.");
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("The updating reservation has null clientId")
				void testSaveWhenTheUpdatingReservationHasNullClientIdShouldNotUpdateAndThrow() {
					// populate DB
					addTestReservationToDatabaseInTheSameContext(reservation);
					UUID initialId = reservation.getId();
					
					// update
					reservation.setClientId(null);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save violates not-null constraints.");
					em.getTransaction().commit();
					
					// verify
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).hasSize(1);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(reservationsInDB.get(0).getDate()).isEqualTo(A_LOCALDATE);
					assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(A_CLIENT_UUID);
				}

				@Test
				@DisplayName("The updating reservation has null date")
				void testSaveWhenTheUpdatingReservationHasNullDateShouldNotUpdateAndThrow() {
					// populate DB
					addTestReservationToDatabaseInTheSameContext(reservation);
					UUID initialId = reservation.getId();
					
					// update
					reservation.setDate(null);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save violates not-null constraints.");
					em.getTransaction().commit();
					
					// verify
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).hasSize(1);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(initialId);
					assertThat(reservationsInDB.get(0).getDate()).isEqualTo(A_LOCALDATE);
					assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(A_CLIENT_UUID);
				}

				@Test
				@DisplayName("Reservation update generates date collision")
				void testSaveWhenReservationUpdateGeneratesDateCollisionShouldNotUpdateAndThrow() {
					// populate DB
					addTestReservationToDatabaseInTheSameContext(reservation);
					addTestReservationToDatabaseInTheSameContext(another_reservation);
					
					// update
					another_reservation.setDate(A_LOCALDATE);
					
					em.getTransaction().begin();
					assertThatThrownBy(() -> reservationRepository.save(another_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					em.getTransaction().commit();
					
					Set<LocalDate> datesInDB = new HashSet<>();
					readAllReservationsFromDatabase().forEach((r) -> datesInDB.add(r.getDate()));
					assertThat(datesInDB).contains(ANOTHER_LOCALDATE);
				}
			}

			@Nested
			@DisplayName("Tests for 'delete'")
			class DeleteTest {

				@Test
				@DisplayName("Reservation was added in the same context")
				void testDeleteWhenReservationWasAddedToTheDatabaseInTheSameContextShouldRemove() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					
					em.getTransaction().begin();
					reservationRepository.delete(reservation);
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("Reservation was added in another context")
				void testDeleteWhenReservationWasAddedToTheDatabaseInAnotherContextShouldRemove() {
					addTestReservationToDatabaseInAnotherContext(reservation);
					
					em.getTransaction().begin();
					reservationRepository.delete(reservation);
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("Reservation has never been inserted")
				void testDeleteWhenReservationHasNeverBeenInsertedInDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					
					em.getTransaction().begin();
					assertThatNoException().isThrownBy(
							() -> reservationRepository.delete(another_reservation));
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
				}

				@Test
				@DisplayName("Reservation has already been removed")
				void testDeleteWhenReservationHasAlreadyBeenRemovedFromDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestReservationToDatabaseInTheSameContext(reservation);
					// manually sets a different id
					UUID another_uuid;
					do {
						another_uuid = UUID.randomUUID();
					} while (another_uuid == reservation.getId());
					another_reservation.setId(another_uuid);
					
					em.getTransaction().begin();
					assertThatNoException().isThrownBy(
							() -> reservationRepository.delete(another_reservation));
					em.getTransaction().commit();
					
					assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
				}
			}

			private List<Reservation> readAllReservationsFromDatabase() {
				return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
			}
		}

		private void addTestReservationToDatabaseInTheSameContext(Reservation reservation) {
			em.getTransaction().begin();
			em.persist(reservation);
			em.getTransaction().commit();
		}

		private void addTestReservationToDatabaseInAnotherContext(Reservation reservation) {
			EntityManager another_em = emf.createEntityManager();
			another_em.getTransaction().begin();
			another_em.persist(reservation);
			another_em.getTransaction().commit();
			another_em.close();
		}
	}

}
