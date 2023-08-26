package io.github.marcopaglio.booking.repository.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository.BOOKING_DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.time.LocalDate;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
import io.github.marcopaglio.booking.model.Reservation;

@DisplayName("Tests for ReservationMongoRepository class")
@Testcontainers
class ReservationMongoRepositoryTest {
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	private static final UUID A_RESERVATION_UUID = UUID.fromString("e77ce09c-bf58-4691-b69b-11851f359b99");
	
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-12-22");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.fromString("86738f20-f90d-4f9a-8d02-4b9537b4bab5");
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.fromString("5cca7a16-d2d6-4be2-ba6b-bcdd0871dc24");

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private ClientSession session;
	private MongoCollection<Reservation> reservationCollection;

	private ReservationMongoRepository reservationRepository;

	@BeforeAll
	static void setupServer() throws Exception {
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
		// make sure we always start with a clean database
		database.drop();
		
		// start a new session for communicating with the DB
		session = mongoClient.startSession();
		
		// repository creation after drop because it removes configurations on collections
		reservationRepository = new ReservationMongoRepository(mongoClient, session);
		
		// get a MongoCollection suited for your POJO class
		reservationCollection = reservationRepository.getCollection();
	}

	@AfterEach
	void closeSession() throws Exception {
		session.close();
	}

	@AfterAll
	static void closeClient() throws Exception {
		mongoClient.close();
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
	@DisplayName("Tests that read the database")
	class ReadDBTest {
		private Reservation reservation;
		private Reservation another_reservation;

		@BeforeEach
		void resetReservationId() throws Exception {
			reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			another_reservation = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
		}

		@Nested
		@DisplayName("Tests for 'findAll'")
		class FindAllTest {

			@Test
			@DisplayName("Database is empty")
			void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
				assertThat(reservationRepository.findAll()).isEmpty();
			}

			@Test
			@DisplayName("Database is not empty")
			void testFindAllWhenDatabaseIsNotEmptyShouldReturnReservationsAsList() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
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
				assertThat(reservationRepository.findByClient(ANOTHER_CLIENT_UUID)).isEmpty();
			}

			@Test
			@DisplayName("Single associated reservation")
			void testFindByClientWhenThereIsASingleAssociatedReservationShouldReturnTheReservationAsList() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
				assertThat(reservationRepository.findByClient(A_CLIENT_UUID))
					.containsExactly(reservation);
			}

			@Test
			@DisplayName("Several associated reservation")
			void testFindByClientWhenThereAreSeveralAssociatedReservationsShouldReturnReservationsAsList() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				another_reservation.setClientId(A_CLIENT_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
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
			@DisplayName("Reservation is in database")
			void testFindByIdWhenReservationIsInDatabaseShouldReturnOptionalOfReservation() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
				assertThat(reservationRepository.findById(A_RESERVATION_UUID))
					.isEqualTo(Optional.of(reservation));
			}
		}

		@Nested
		@DisplayName("Tests for 'findByDate'")
		class FindByDate {

			@Test
			@DisplayName("Reservation is not in database")
			void testFindByDateWhenReservationIsNotInDatabaseShouldReturnOptionalOfEmpty() {
				assertThat(reservationRepository.findByDate(ANOTHER_LOCALDATE)).isEmpty();
			}

			@Test
			@DisplayName("Reservation is in database")
			void testFindByDateWhenReservationIsInDatabaseShouldReturnOptionalOfReservation() {
				addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
				addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
				
				assertThat(reservationRepository.findByDate(A_LOCALDATE))
					.isEqualTo(Optional.of(reservation));
			}
		}

		@Nested
		@DisplayName("Tests that modify the database")
		class ModifyDBTest {

			@Nested
			@DisplayName("Tests for 'save'")
			class SaveTest {

				@Test
				@DisplayName("Reservation with null clientId")
				void testSaveWhenReservationHasNullClientIdShouldNotInsertAndThrow() {
					reservation.setClientId(null);
					
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save must have a not-null client.");
					
					assertThat(readAllReservationsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("Reservation with null date")
				void testSaveWhenReservationHasNullDateShouldNotInsertAndThrow() {
					reservation.setDate(null);
					
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(NotNullConstraintViolationException.class)
						.hasMessage("Reservation to save must have a not-null date.");
					
					assertThat(readAllReservationsFromDatabase()).isEmpty();
				}

				@Test
				@DisplayName("New reservation is valid")
				void testSaveWhenNewReservationIsValidShouldInsertAndReturnTheReservationWithId() {
					Reservation returnedReservation = reservationRepository.save(reservation);
					
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(returnedReservation).isEqualTo(reservation);
					assertThat(reservationsInDB.get(0).getId()).isNotNull();
					assertThat(returnedReservation.getId()).isNotNull();
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(returnedReservation.getId());
				}

				@Test
				@DisplayName("New reservation generates id collision")
				void testSaveWhenNewReservationGeneratesAnIdCollisionShouldNotInsertAndThrow() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					Reservation spied_reservation = spy(another_reservation);
					// sets same id
					doAnswer( invocation -> {
						((Reservation) invocation.getMock()).setId(A_RESERVATION_UUID);
						return null;
					}).when(spied_reservation).setId(not(eq(A_RESERVATION_UUID)));
					
					assertThatThrownBy(() -> reservationRepository.save(spied_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(spied_reservation);
				}

				@Test
				@DisplayName("New reservation generates date collision")
				void testSaveWhenNewReservationGeneratesADateCollisionShouldNotInsertAndThrow() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					another_reservation.setDate(A_LOCALDATE);
					Reservation spied_reservation = spy(another_reservation);
					// sets different id
					doAnswer(invocation -> {
						((Reservation) invocation.getMock()).setId(ANOTHER_RESERVATION_UUID);
						return null;
					}).when(spied_reservation).setId(A_RESERVATION_UUID);
					
					assertThatThrownBy(() -> reservationRepository.save(spied_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
				}

				@Test
				@DisplayName("New reservation has same client")
				void testSaveWhenNewReservationHasSameClientShouldNotThrowAndInsert() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					another_reservation.setClientId(A_CLIENT_UUID);
					Reservation spied_reservation = spy(another_reservation);
					// sets different id
					doAnswer(invocation -> {
						((Reservation) invocation.getMock()).setId(ANOTHER_RESERVATION_UUID);
						return null;
					}).when(spied_reservation).setId(A_RESERVATION_UUID);
					
					assertThatNoException().isThrownBy(
							() -> reservationRepository.save(spied_reservation));
					
					assertThat(readAllReservationsFromDatabase())
						.containsExactlyInAnyOrder(reservation, spied_reservation);
				}

				@Test
				@DisplayName("Updated reservation is valid")
				void testSaveWhenUpdatedReservationIsValidShouldUpdateAndReturnWithoutChangingId() {
					// populate DB
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					// update
					reservation.setClientId(ANOTHER_CLIENT_UUID);
					reservation.setDate(ANOTHER_LOCALDATE);
					
					Reservation returnedReservation = reservationRepository.save(reservation);
					
					// verify
					List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
					assertThat(reservationsInDB).containsExactly(reservation);
					assertThat(returnedReservation).isEqualTo(reservation);
					assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(returnedReservation.getClientId()).isEqualTo(ANOTHER_CLIENT_UUID);
					assertThat(reservationsInDB.get(0).getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(returnedReservation.getDate()).isEqualTo(ANOTHER_LOCALDATE);
					assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
					assertThat(returnedReservation.getId()).isEqualTo(A_RESERVATION_UUID);
				}

				@Test
				@DisplayName("Updated reservation is no longer present in database")
				void testSaveWhenUpdatedReservationIsNotInDatabaseShouldThrowAndNotInsert() {
					reservation.setId(A_RESERVATION_UUID);
					
					assertThatThrownBy(() -> reservationRepository.save(reservation))
						.isInstanceOf(UpdateFailureException.class)
						.hasMessage("Reservation to update is not longer present in the repository.");
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("Updated reservation generates date collision")
				void testSaveWhenUpdatedReservationGeneratesDateCollisionShouldNotUpdateAndThrow() {
					// populate DB
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					addTestReservationToDatabase(another_reservation, ANOTHER_RESERVATION_UUID);
					
					// update
					another_reservation.setDate(A_LOCALDATE);
					
					assertThatThrownBy(() -> reservationRepository.save(another_reservation))
						.isInstanceOf(UniquenessConstraintViolationException.class)
						.hasMessage("Reservation to save violates uniqueness constraints.");
					
					Set<LocalDate> datesInDB = new HashSet<>();
					readAllReservationsFromDatabase().forEach((r) -> datesInDB.add(r.getDate()));
					assertThat(datesInDB).contains(ANOTHER_LOCALDATE);
				}
			}

			@Nested
			@DisplayName("Tests for 'delete'")
			class DeleteTest {

				@Test
				@DisplayName("Reservation is in database")
				void testDeleteWhenReservationIsInDatabaseShouldRemove() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					reservationRepository.delete(reservation);
					
					assertThat(readAllReservationsFromDatabase()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("Reservation has never been inserted")
				void testDeleteWhenReservationHasNeverBeenInsertedInDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					another_reservation.setId(ANOTHER_RESERVATION_UUID);
					
					assertThatNoException().isThrownBy(
							() -> reservationRepository.delete(another_reservation));
					
					assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
				}
				
				@Test
				@DisplayName("Reservation has already been removed")
				void testDeleteWhenReservationHasAlreadyBeenRemovedFromDatabaseShouldNotRemoveAnythingAndNotThrow() {
					addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
					
					assertThatNoException().isThrownBy(
							() -> reservationRepository.delete(another_reservation));
					
					assertThat(readAllReservationsFromDatabase()).containsExactly(reservation);
				}
			}

			private List<Reservation> readAllReservationsFromDatabase() {
				return StreamSupport
						.stream(reservationCollection.find().spliterator(), false)
						.collect(Collectors.toList());
			}
		}

		private void addTestReservationToDatabase(Reservation reservation, UUID id) {
			reservation.setId(id);
			reservationCollection.insertOne(reservation);
		}
	}

}
