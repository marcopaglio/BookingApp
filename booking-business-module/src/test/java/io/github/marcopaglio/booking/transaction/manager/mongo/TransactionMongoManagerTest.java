package io.github.marcopaglio.booking.transaction.manager.mongo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.BOOKING_DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;

@DisplayName("Tests for TransactionMongoManager class")
@ExtendWith(MockitoExtension.class)
@Testcontainers
class TransactionMongoManagerTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);

	private static final UUID A_CLIENT_UUID = UUID.fromString("89567459-db55-4cd1-a01e-dc94c86e69fc");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, A_LOCALDATE);

	@Container
	private static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.7");

	private static MongoClient mongoClient;
	private static ClientSession session;
	private ClientSession spiedSession; //TODO: rimuovere se la sessione si apre e chiude in Each
	private static MongoDatabase database;

	@Mock
	private ClientMongoRepository clientRepository;

	@Mock
	private ReservationMongoRepository reservationRepository;

	private TransactionMongoManager transactionManager;

	@BeforeAll
	public static void setupServer() throws Exception {
		mongoClient = getClient(mongo.getConnectionString());
		//mongoClient = MongoClients.create(mongo.getConnectionString());
		
		session = mongoClient.startSession();
		
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
		
		spiedSession = spy(session);
		
		transactionManager = new TransactionMongoManager(spiedSession, clientRepository, reservationRepository);
	}

	@AfterEach
	void closeSession() {
		//clientSession.close();
	}

	@AfterAll
	public static void shutdownServer() throws Exception {
		session.close();
		mongoClient.close();
	}

	@Nested
	@DisplayName("Using ClientTransactionCode")
	class ClientTransactionCodeTest {

		@Test
		@DisplayName("Code calls ClientRepository's method")
		void testDoInTransactionWhenCallsAMethodOfClientRepositoryShouldApplyAndReturn() {
			List<Client> listOfClients = Arrays.asList(A_CLIENT);
			when(clientRepository.findAll()).thenReturn(listOfClients);
			
			ClientTransactionCode<List<Client>> code = (ClientRepository clientRepository) ->
					clientRepository.findAll();
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfClients);
			
			InOrder inOrder = Mockito.inOrder(spiedSession, clientRepository);
			
			inOrder.verify(spiedSession).startTransaction();
			inOrder.verify(clientRepository).findAll();
			inOrder.verify(spiedSession).commitTransaction();
			
			assertThat(spiedSession.hasActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("Code on ClientRepository throws IllegalArgumentException")
		void testDoInTransactionWhenClientRepositoryThrowsIllegalArgumentExceptionShouldAbortAndThrow() {
			doThrow(new IllegalArgumentException()).when(clientRepository).delete(null);

			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
				clientRepository.delete(null);
				return null;
			};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsNotNullConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new NotNullConstraintViolationException())
				.when(clientRepository).save(isA(Client.class));
			
			ClientTransactionCode<Client> code = (ClientRepository clientRepository) -> 
				clientRepository.save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new UniquenessConstraintViolationException())
				.when(clientRepository).save(isA(Client.class));
			
			ClientTransactionCode<Client> code = (ClientRepository clientRepository) -> 
				clientRepository.save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldAbortAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
				throw runtimeException;
			};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}
	}

	@Nested
	@DisplayName("Using ReservationTransactionCode")
	class ReservationTransactionCodeTest {

		@Test
		@DisplayName("Code calls ReservationRepository's method")
		void testDoInTransactionWhenCallsAMethodOfReservationRepositoryShouldApplyAndReturn() {
			List<Reservation> listOfReservations = Arrays.asList(A_RESERVATION);
			when(reservationRepository.findAll()).thenReturn(listOfReservations);
			
			ReservationTransactionCode<List<Reservation>> code = 
					(ReservationRepository reservationRepository) -> reservationRepository.findAll();
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfReservations);
			
			InOrder inOrder = Mockito.inOrder(spiedSession, reservationRepository);
			
			inOrder.verify(spiedSession).startTransaction();
			inOrder.verify(reservationRepository).findAll();
			inOrder.verify(spiedSession).commitTransaction();
			
			assertThat(spiedSession.hasActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws IllegalArgumentException")
		void testDoInTransactionWhenReservationRepositoryThrowsIllegalArgumentExceptionShouldAbortAndThrow() {
			doThrow(new IllegalArgumentException()).when(reservationRepository).delete(null);

			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
				reservationRepository.delete(null);
				return null;
			};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsNotNullConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new NotNullConstraintViolationException())
				.when(reservationRepository).save(isA(Reservation.class));
			
			ReservationTransactionCode<Reservation> code = 
					(ReservationRepository reservationRepository) -> 
						reservationRepository.save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new UniquenessConstraintViolationException())
				.when(reservationRepository).save(isA(Reservation.class));
			
			ReservationTransactionCode<Reservation> code =
					(ReservationRepository reservationRepository) -> 
						reservationRepository.save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldAbortAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
				throw runtimeException;
			};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}
	}

	@Nested
	@DisplayName("Using ClientReservationTransactionCode")
	class ClientReservationTransactionCodeTest {

		@Test
		@DisplayName("Code calls both ClientRepository's and ReservationRepository's methods")
		void testDoInTransactionWhenCallsMethodsOfClientAndReservationRepositoriesShouldApplyAndReturn() {
			List<Reservation> listOfReservations = Arrays.asList(A_RESERVATION);
			when(reservationRepository.findAll()).thenReturn(listOfReservations);
			
			ClientReservationTransactionCode<List<Reservation>> code = 
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.findAll();
						return reservationRepository.findAll();
					};
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfReservations);
			
			InOrder inOrder = Mockito.inOrder(spiedSession, clientRepository, reservationRepository);
			
			inOrder.verify(spiedSession).startTransaction();
			inOrder.verify(clientRepository).findAll();
			inOrder.verify(reservationRepository).findAll();
			inOrder.verify(spiedSession).commitTransaction();
			
			assertThat(spiedSession.hasActiveTransaction()).isFalse();
		}

		@Test
		@DisplayName("Code on ClientRepository throws IllegalArgumentException")
		void testDoInTransactionWhenClientRepositoryThrowsIllegalArgumentExceptionShouldAbortAndThrow() {
			// default stubbing for reservationRepository.delete(reservation)
			doThrow(new IllegalArgumentException()).when(clientRepository).delete(null);

			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						reservationRepository.delete(A_RESERVATION);
						clientRepository.delete(null);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws IllegalArgumentException")
		void testDoInTransactionWhenReservationRepositoryThrowsIllegalArgumentExceptionShouldAbortAndThrow() {
			doThrow(new IllegalArgumentException()).when(reservationRepository).save(null);

			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(null);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsNotNullConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new NotNullConstraintViolationException())
				.when(clientRepository).save(isA(Client.class));
			
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(A_RESERVATION);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsNotNullConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new NotNullConstraintViolationException())
				.when(reservationRepository).save(isA(Reservation.class));
			
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(A_RESERVATION);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new UniquenessConstraintViolationException())
				.when(clientRepository).save(isA(Client.class));
			
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(A_RESERVATION);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			doThrow(new UniquenessConstraintViolationException())
				.when(reservationRepository).save(isA(Reservation.class));
			
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(A_RESERVATION);
						return null;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldAbortAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						throw runtimeException;
					};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(spiedSession, never()).commitTransaction();
			verify(spiedSession).abortTransaction();
		}
	}
}
