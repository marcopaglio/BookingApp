package io.github.marcopaglio.booking.transaction.manager.mongo;

import static io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository.BOOKING_DB_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
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
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.handler.mongo.TransactionMongoHandler;

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

	private static final TransactionOptions txnOptions = TransactionOptions.builder()
			.readPreference(ReadPreference.primary())
			.readConcern(ReadConcern.LOCAL)
			.writeConcern(WriteConcern.MAJORITY)
			.build(); //TODO change

	private static MongoClient mongoClient;
	private static MongoDatabase database;
	
	private static ClientSession session;

	@Mock
	private static TransactionMongoHandler transactionMongoHandler;

	@Mock
	private static ClientMongoRepository clientMongoRepository;

	@Mock
	private static ReservationMongoRepository reservationMongoRepository;

	@Mock
	private static TransactionHandlerFactory transactionHandlerFactory;

	@Mock
	private ClientRepositoryFactory clientRepositoryFactory;

	@Mock
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionMongoManager transactionManager;

	@BeforeAll
	public static void setupServer() throws Exception {
		mongoClient = MongoClients.create(mongo.getConnectionString());
		
		database = mongoClient.getDatabase(BOOKING_DB_NAME);
	}

	@BeforeEach
	void setUp() throws Exception {
		// make sure we always start with a clean database
		database.drop();
		
		session = mongoClient.startSession();
		
		transactionManager = new TransactionMongoManager(mongoClient, transactionHandlerFactory,
				clientRepositoryFactory, reservationRepositoryFactory);
		
		// stubbing
		when(transactionHandlerFactory.createTransactionHandler(mongoClient, txnOptions))
		.thenReturn(transactionMongoHandler);
		when(transactionMongoHandler.getSession()).thenReturn(session);
	}

	@AfterEach
	void closeSession() throws Exception {
		session.close();
	}

	@AfterAll
	public static void closeClient() throws Exception {
		mongoClient.close();
	}

	@Nested
	@DisplayName("Using ClientTransactionCode")
	class ClientTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(clientRepositoryFactory.createClientRepository(mongoClient, session))
				.thenReturn(clientMongoRepository);
		}

		@Test
		@DisplayName("Code calls ClientRepository's method")
		void testDoInTransactionWhenCallsAMethodOfClientRepositoryShouldApplyAndReturn() {
			ClientTransactionCode<List<Client>> code =
					(ClientRepository clientRepository) -> clientRepository.findAll();
			List<Client> listOfClients = Arrays.asList(A_CLIENT);
			
			when(clientMongoRepository.findAll()).thenReturn(listOfClients);
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfClients);
			
			InOrder inOrder = Mockito.inOrder(transactionMongoHandler, clientMongoRepository);
			
			inOrder.verify(transactionMongoHandler).startTransaction();
			inOrder.verify(clientMongoRepository).findAll();
			inOrder.verify(transactionMongoHandler).commitTransaction();
			
			verifyNoMoreInteractions(transactionMongoHandler, clientMongoRepository);
		}

		@Test
		@DisplayName("Code on ClientRepository throws IllegalArgumentException")
		void testDoInTransactionWhenClientRepositoryThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
				clientRepository.delete(null);
				return null;
			};
			
			doThrow(new IllegalArgumentException()).when(clientMongoRepository).delete(null);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Client> code = (ClientRepository clientRepository) -> 
				clientRepository.save(A_CLIENT);
				
			doThrow(new NotNullConstraintViolationException())
				.when(clientMongoRepository).save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsUniquenessConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Client> code = (ClientRepository clientRepository) -> 
				clientRepository.save(A_CLIENT);
			
			doThrow(new UniquenessConstraintViolationException())
				.when(clientMongoRepository).save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldRollBackAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
				throw runtimeException;
			};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}
	}

	@Nested
	@DisplayName("Using ReservationTransactionCode")
	class ReservationTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(reservationRepositoryFactory.createReservationRepository(mongoClient, session))
				.thenReturn(reservationMongoRepository);
		}

		@Test
		@DisplayName("Code calls ReservationRepository's method")
		void testDoInTransactionWhenCallsAMethodOfReservationRepositoryShouldApplyAndReturn() {
			ReservationTransactionCode<List<Reservation>> code = 
					(ReservationRepository reservationRepository) -> reservationRepository.findAll();
			List<Reservation> listOfReservations = Arrays.asList(A_RESERVATION);
			
			when(reservationMongoRepository.findAll()).thenReturn(listOfReservations);
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfReservations);
			
			InOrder inOrder = Mockito.inOrder(transactionMongoHandler, reservationMongoRepository);
			
			inOrder.verify(transactionMongoHandler).startTransaction();
			inOrder.verify(reservationMongoRepository).findAll();
			inOrder.verify(transactionMongoHandler).commitTransaction();
			
			verifyNoMoreInteractions(transactionMongoHandler, reservationMongoRepository);
		}

		@Test
		@DisplayName("Code on ReservationRepository throws IllegalArgumentException")
		void testDoInTransactionWhenReservationRepositoryThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
				reservationRepository.delete(null);
				return null;
			};
			
			doThrow(new IllegalArgumentException()).when(reservationMongoRepository).delete(null);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Reservation> code = 
					(ReservationRepository reservationRepository) -> 
						reservationRepository.save(A_RESERVATION);
			
			doThrow(new NotNullConstraintViolationException())
				.when(reservationMongoRepository).save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsUniquenessConstraintViolationExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Reservation> code =
					(ReservationRepository reservationRepository) -> 
						reservationRepository.save(A_RESERVATION);
						
			doThrow(new UniquenessConstraintViolationException())
				.when(reservationMongoRepository).save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
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
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}
	}

	@Nested
	@DisplayName("Using ClientReservationTransactionCode")
	class ClientReservationTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(clientRepositoryFactory.createClientRepository(mongoClient, session))
				.thenReturn(clientMongoRepository);
			when(reservationRepositoryFactory.createReservationRepository(mongoClient, session))
				.thenReturn(reservationMongoRepository);
		}

		@Test
		@DisplayName("Code calls both ClientRepository's and ReservationRepository's methods")
		void testDoInTransactionWhenCallsMethodsOfClientAndReservationRepositoriesShouldApplyAndReturn() {
			ClientReservationTransactionCode<List<Reservation>> code = 
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.findAll();
					return reservationRepository.findAll();
				};
			List<Reservation> listOfReservations = Arrays.asList(A_RESERVATION);
			
			when(reservationMongoRepository.findAll()).thenReturn(listOfReservations);
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfReservations);
			
			InOrder inOrder = Mockito.inOrder(
					transactionMongoHandler, clientMongoRepository, reservationMongoRepository);
			
			inOrder.verify(transactionMongoHandler).startTransaction();
			inOrder.verify(clientMongoRepository).findAll();
			inOrder.verify(reservationMongoRepository).findAll();
			inOrder.verify(transactionMongoHandler).commitTransaction();
			
			verifyNoMoreInteractions(transactionMongoHandler, clientMongoRepository, reservationMongoRepository);
		}

		@Test
		@DisplayName("Code on ClientRepository throws IllegalArgumentException")
		void testDoInTransactionWhenClientRepositoryThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					reservationRepository.delete(A_RESERVATION);
					clientRepository.delete(null);
					return null;
				};
			
			doThrow(new IllegalArgumentException()).when(clientMongoRepository).delete(null);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws IllegalArgumentException")
		void testDoInTransactionWhenReservationRepositoryThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.save(A_CLIENT);
					reservationRepository.save(null);
					return null;
				};
					
			doThrow(new IllegalArgumentException()).when(reservationMongoRepository).save(null);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.save(A_CLIENT);
					reservationRepository.save(A_RESERVATION);
					return null;
				};
			
			doThrow(new NotNullConstraintViolationException())
				.when(clientMongoRepository).save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws NotNullConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.save(A_CLIENT);
					reservationRepository.save(A_RESERVATION);
					return null;
				};
					
			doThrow(new NotNullConstraintViolationException())
				.when(reservationMongoRepository).save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of not-null constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ClientRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenClientRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.save(A_CLIENT);
					reservationRepository.save(A_RESERVATION);
					return null;
				};
			
			doThrow(new UniquenessConstraintViolationException())
				.when(clientMongoRepository).save(A_CLIENT);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}

		@Test
		@DisplayName("Code on ReservationRepository throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenReservationRepositoryThrowsUniquenessConstraintViolationExceptionShouldAbortAndThrow() {
			ClientReservationTransactionCode<Object> code =
					(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
						clientRepository.save(A_CLIENT);
						reservationRepository.save(A_RESERVATION);
						return null;
					};
					
			doThrow(new UniquenessConstraintViolationException())
				.when(reservationMongoRepository).save(A_RESERVATION);
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to violation of uniqueness constraint(s).");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
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
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
		}
	}
}
