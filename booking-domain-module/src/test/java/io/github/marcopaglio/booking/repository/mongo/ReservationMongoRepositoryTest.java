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
import io.github.marcopaglio.booking.model.Reservation;

class ReservationMongoRepositoryTest {
	private static final UUID A_CLIENT_UUID = UUID.fromString("5a583373-c1b4-4913-82b6-5ea76fb1b1be");
	private static final UUID ANOTHER_CLIENT_UUID = UUID.randomUUID();
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final LocalDate ANOTHER_LOCALDATE = LocalDate.parse("2023-12-22");

	private static final UUID A_RESERVATION_UUID = UUID.randomUUID();
	private static final UUID ANOTHER_RESERVATION_UUID = UUID.randomUUID();

	private static MongoServer server;
	private static MongoClient mongoClient;
	private static MongoDatabase database;
	
	private MongoCollection<Reservation> reservationCollection;
	private ReservationMongoRepository reservationRepository;

	@BeforeAll
	static void setupServer() throws Exception {
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
		reservationRepository = new ReservationMongoRepository(mongoClient);
		
		// get a MongoCollection suited for your POJO class
		reservationCollection = reservationRepository.getCollection();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@AfterAll
	static void shutdownServer() throws Exception {
		mongoClient.close();
		server.shutdown();
	}

	@Nested
	@DisplayName("Tests for 'save'")
	class SaveTest {

		@Test
		@DisplayName("Null reservation")
		void testSaveWhenReservationIsNullShouldThrow() {
			assertThatThrownBy(() -> reservationRepository.save(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to save cannot be null.");
		}

		@Test
		@DisplayName("Reservation with null clientId")
		void testSaveWhenReservationHasNullClientIdShouldNotInsertAndThrow() {
			assertThat(readAllReservationsFromDatabase()).isEmpty();
			Reservation nullClientIdReservation = new Reservation(null, A_LOCALDATE);
			
			assertThatThrownBy(() -> reservationRepository.save(nullClientIdReservation))
				.isInstanceOf(NotNullConstraintViolationException.class)
				.hasMessage("Reservation to save must have a not-null client.");
			
			assertThat(readAllReservationsFromDatabase()).isEmpty();
		}

		@Test
		@DisplayName("Reservation with null date")
		void testSaveWhenReservationHasNullDateShouldNotInsertAndThrow() {
			assertThat(readAllReservationsFromDatabase()).isEmpty();
			Reservation nullDateReservation = new Reservation(A_CLIENT_UUID, null);
			
			assertThatThrownBy(() -> reservationRepository.save(nullDateReservation))
				.isInstanceOf(NotNullConstraintViolationException.class)
				.hasMessage("Reservation to save must have a not-null date.");
			
			assertThat(readAllReservationsFromDatabase()).isEmpty();
		}

		@Test
		@DisplayName("New reservation is valid")
		void testSaveWhenNewReservationIsValidShouldInsertAndReturnTheReservationWithId() {
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			assertThat(reservation.getId()).isNull();
			assertThat(readAllReservationsFromDatabase()).isEmpty();
			
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
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			Reservation spied_reservation = spy(new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE));
			// sets same id
			doAnswer( invocation -> {
				((Reservation) invocation.getMock()).setId(A_RESERVATION_UUID);
				return null;
			}).when(spied_reservation).setId(not(eq(A_RESERVATION_UUID)));
			
			assertThatThrownBy(() -> reservationRepository.save(spied_reservation))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("Reservation [date=" + ANOTHER_LOCALDATE.toString() +
						"] to insert violates uniqueness constraints.");
			
			assertThat(readAllReservationsFromDatabase()).doesNotContain(spied_reservation);
		}

		@Test
		@DisplayName("New reservation generates date collision")
		void testSaveWhenNewReservationGeneratesADateCollisionShouldNotInsertAndThrow() {
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			Reservation spied_reservation = spy(new Reservation(ANOTHER_CLIENT_UUID, A_LOCALDATE));
			// sets different id
			doAnswer(invocation -> {
				((Reservation) invocation.getMock()).setId(ANOTHER_RESERVATION_UUID);
				return null;
			}).when(spied_reservation).setId(A_RESERVATION_UUID);
			
			assertThatThrownBy(() -> reservationRepository.save(spied_reservation))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("Reservation [date=" + A_LOCALDATE.toString()
						+ "] to insert violates uniqueness constraints.");
			
			List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
			assertThat(reservationsInDB).containsExactly(reservation);
			assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
		}

		@Test
		@DisplayName("New reservation has same client")
		void testSaveWhenNewReservationHasSameClientShouldNotThrowAndInsert() {
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			Reservation spied_reservation = spy(new Reservation(A_CLIENT_UUID, ANOTHER_LOCALDATE));
			// sets different id
			doAnswer(invocation -> {
				((Reservation) invocation.getMock()).setId(ANOTHER_RESERVATION_UUID);
				return null;
			}).when(spied_reservation).setId(A_RESERVATION_UUID);
			
			assertThatNoException().isThrownBy(() -> reservationRepository.save(spied_reservation));
			
			assertThat(readAllReservationsFromDatabase()).containsExactlyInAnyOrder(reservation, spied_reservation);
		}

		@Test
		@DisplayName("Updated reservation is valid")
		void testSaveWhenUpdatedReservationIsValidShouldUpdateAndReturnWithoutChangingId() {
			// populate DB
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			
			// verify state before
			List<Reservation> reservationsInDB = readAllReservationsFromDatabase();
			assertThat(reservationsInDB).containsExactly(reservation);
			assertThat(reservationsInDB.get(0).getClientId()).isEqualTo(A_CLIENT_UUID);
			assertThat(reservationsInDB.get(0).getDate()).isEqualTo(A_LOCALDATE);
			assertThat(reservationsInDB.get(0).getId()).isEqualTo(A_RESERVATION_UUID);
			
			// update
			reservation.setClientId(ANOTHER_CLIENT_UUID);
			reservation.setDate(ANOTHER_LOCALDATE);
			
			Reservation returnedReservation = reservationRepository.save(reservation);
			
			// verify state after
			reservationsInDB = readAllReservationsFromDatabase();
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
		void testSaveWhenUpdatedReservationIsNotInDatabaseShouldNotThrow() {
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			reservation.setId(A_RESERVATION_UUID);
			
			assertThatNoException().isThrownBy(() -> reservationRepository.save(reservation));
		}

		@Test
		@DisplayName("Updated reservation generates date collision")
		void testSaveWhenUpdatedReservationGeneratesDateCollisionShouldNotUpdateAndThrow() {
			// populate DB
			Reservation reservation = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
			addTestReservationToDatabase(reservation, A_RESERVATION_UUID);
			Reservation reservationToUpdate = new Reservation(ANOTHER_CLIENT_UUID, ANOTHER_LOCALDATE);
			addTestReservationToDatabase(reservationToUpdate, ANOTHER_RESERVATION_UUID);
			
			// update
			reservationToUpdate.setDate(A_LOCALDATE);
			
			assertThatThrownBy(() -> reservationRepository.save(reservationToUpdate))
				.isInstanceOf(UniquenessConstraintViolationException.class)
				.hasMessage("Reservation [date=" + A_LOCALDATE.toString() +
						"] to update violates uniqueness constraints.");
			
			Set<LocalDate> datesInDB = new HashSet<>();
			readAllReservationsFromDatabase().forEach((r) -> datesInDB.add(r.getDate()));
			assertThat(datesInDB).contains(ANOTHER_LOCALDATE);
		}

		private List<Reservation> readAllReservationsFromDatabase() {
			return StreamSupport
					.stream(reservationCollection.find().spliterator(), false)
					.collect(Collectors.toList());
		}
		
		private void addTestReservationToDatabase(Reservation reservation, UUID id) {
			reservation.setId(id);
			reservationCollection.insertOne(reservation);
		}
	}
	/*@Test
	void test() {
		assertThat(reservationCollection.countDocuments()).isZero();

		// make a document and insert it
		Reservation ada = new Reservation(A_CLIENT_UUID, A_LOCALDATE);
		ada.setId(A_CLIENT_UUID);
		System.out.println("Original Reservation Model: " + ada);
		reservationCollection.insertOne(ada);

		// Person will now have an ObjectId
		System.out.println("Mutated Reservation Model: " + ada);
		assertThat(reservationCollection.countDocuments()).isEqualTo(1L);
		
		Reservation oda = new Reservation(ANOTHER_CLIENT_UUID, A_LOCALDATE);
		oda.setId(ANOTHER_CLIENT_UUID);
		System.out.println("Original Reservation Model: " + oda);
		assertThatThrownBy(() -> reservationCollection.insertOne(oda)).isInstanceOf(MongoWriteException.class);

		// get it (since it's the only one in there since we dropped the rest earlier on)
		Reservation something = reservationCollection.find().first();
		System.out.println("Retrieved Reservation: " + something);
		
		assertThat(something).isEqualTo(ada);
	}*/
}
