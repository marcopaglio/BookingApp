package io.github.marcopaglio.booking.transaction.manager.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.MongoCommandException;
import com.mongodb.ServerAddress;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
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
class TransactionMongoManagerTest {
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);

	private static final UUID A_CLIENT_UUID = UUID.fromString("89567459-db55-4cd1-a01e-dc94c86e69fc");
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2022-12-22");
	private static final Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, A_LOCALDATE);

	private static final String INVALID_ARGUMENT_ERROR_MSG = "Transaction fails due to invalid argument(s) passed.";
	private static final String UPDATE_FAILURE_ERROR_MSG = "Transaction fails due to an update failure.";
	private static final String VIOLATION_OF_NOT_NULL_CONSTRAINT_ERROR_MSG = "Transaction fails due to violation of not-null constraint(s).";
	private static final String VIOLATION_OF_UNIQUENESS_CONSTRAINT_ERROR_MSG = "Transaction fails due to violation of uniqueness constraint(s).";
	private static final String COMMIT_FAILURE_ERROR_MSG = "Transaction fails due to a commitment failure.";

	private static final String BOOKING_DB_NAME = "TransactionMongoManager_db";
	private MongoClient mongoClient;
	private ClientSession session;

	@Mock
	private TransactionMongoHandler transactionMongoHandler;

	@Mock
	private ClientMongoRepository clientMongoRepository;

	@Mock
	private ReservationMongoRepository reservationMongoRepository;

	@Mock
	private TransactionHandlerFactory transactionHandlerFactory;

	@Mock
	private ClientRepositoryFactory clientRepositoryFactory;

	@Mock
	private ReservationRepositoryFactory reservationRepositoryFactory;

	private TransactionMongoManager transactionManager;

	@BeforeEach
	void setUp() throws Exception {
		transactionManager = new TransactionMongoManager(mongoClient, BOOKING_DB_NAME,
				transactionHandlerFactory, clientRepositoryFactory, reservationRepositoryFactory);
		
		// stubbing
		when(transactionHandlerFactory.createTransactionHandler(same(mongoClient), isA(TransactionOptions.class)))
			.thenReturn(transactionMongoHandler);
		when(transactionMongoHandler.getHandler()).thenReturn(session);
	}

	@Nested
	@DisplayName("Using ClientTransactionCode")
	class ClientTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(clientRepositoryFactory.createClientRepository(mongoClient, session, BOOKING_DB_NAME))
				.thenReturn(clientMongoRepository);
		}

		@Test
		@DisplayName("Code calls ClientRepository's method")
		void testDoInTransactionWhenCallsAMethodShouldApplyAndReturn() {
			ClientTransactionCode<List<Client>> code =
					(ClientRepository clientRepository) -> clientRepository.findAll();
			
			List<Client> listOfClients = Arrays.asList(A_CLIENT);
			when(clientMongoRepository.findAll()).thenReturn(listOfClients);
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfClients);
			
			InOrder inOrder = Mockito.inOrder(transactionMongoHandler, clientMongoRepository);
			
			inOrder.verify(transactionMongoHandler).startTransaction();
			inOrder.verify(clientMongoRepository).findAll();
			inOrder.verify(transactionMongoHandler).commitTransaction();
			inOrder.verify(transactionMongoHandler).closeHandler();
			
			verifyNoMoreInteractions(clientMongoRepository);
		}

		@Test
		@DisplayName("Code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
					throw new IllegalArgumentException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Transaction fails due to invalid argument(s) passed.");
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UpdateFailureException")
		void testDoInTransactionWhenCodeThrowsUpdateFailureExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
					throw new UpdateFailureException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(UPDATE_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws NotNullConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
					throw new NotNullConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_NOT_NULL_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsUniquenessConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code = (ClientRepository clientRepository) -> {
					throw new UniquenessConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_UNIQUENESS_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
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
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Commit fails")
		void testDoInTransactionWhenCommitFailsShouldRollBackAndThrow() {
			ClientTransactionCode<Object> code =
					(ClientRepository clientRepository) -> clientRepository.findAll();
			
			doThrow(new MongoCommandException(new BsonDocument(), new ServerAddress()))
				.when(transactionMongoHandler).commitTransaction();
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(COMMIT_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).commitTransaction();
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler).closeHandler();
		}
	}

	@Nested
	@DisplayName("Using ReservationTransactionCode")
	class ReservationTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(reservationRepositoryFactory.createReservationRepository(mongoClient, session, BOOKING_DB_NAME))
				.thenReturn(reservationMongoRepository);
		}

		@Test
		@DisplayName("Code calls ReservationRepository's method")
		void testDoInTransactionWhenCallsAMethodShouldApplyAndReturn() {
			ReservationTransactionCode<List<Reservation>> code = 
					(ReservationRepository reservationRepository) -> reservationRepository.findAll();
			
			List<Reservation> listOfReservations = Arrays.asList(A_RESERVATION);
			when(reservationMongoRepository.findAll()).thenReturn(listOfReservations);
			
			assertThat(transactionManager.doInTransaction(code)).isEqualTo(listOfReservations);
			
			InOrder inOrder = Mockito.inOrder(transactionMongoHandler, reservationMongoRepository);
			
			inOrder.verify(transactionMongoHandler).startTransaction();
			inOrder.verify(reservationMongoRepository).findAll();
			inOrder.verify(transactionMongoHandler).commitTransaction();
			inOrder.verify(transactionMongoHandler).closeHandler();
			
			verifyNoMoreInteractions(reservationMongoRepository);
		}

		@Test
		@DisplayName("Code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
					throw new IllegalArgumentException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(INVALID_ARGUMENT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UpdateFailureException")
		void testDoInTransactionWhenCodeThrowsUpdateFailureExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
					throw new UpdateFailureException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(UPDATE_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws NotNullConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
					throw new NotNullConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_NOT_NULL_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsUniquenessConstraintViolationExceptionShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
					throw new UniquenessConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_UNIQUENESS_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldRollBackAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ReservationTransactionCode<Object> code = (ReservationRepository reservationRepository) -> {
					throw runtimeException;
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Commit fails")
		void testDoInTransactionWhenCommitFailsShouldRollBackAndThrow() {
			ReservationTransactionCode<Object> code = 
					(ReservationRepository reservationRepository) -> reservationRepository.findAll();
			
			doThrow(new MongoCommandException(new BsonDocument(), new ServerAddress()))
				.when(transactionMongoHandler).commitTransaction();
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(COMMIT_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).commitTransaction();
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler).closeHandler();
		}
	}

	@Nested
	@DisplayName("Using ClientReservationTransactionCode")
	class ClientReservationTransactionCodeTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(clientRepositoryFactory.createClientRepository(mongoClient, session, BOOKING_DB_NAME))
				.thenReturn(clientMongoRepository);
			when(reservationRepositoryFactory.createReservationRepository(mongoClient, session, BOOKING_DB_NAME))
				.thenReturn(reservationMongoRepository);
		}

		@Test
		@DisplayName("Code calls both ClientRepository's and ReservationRepository's methods")
		void testDoInTransactionWhenCodeCallsMethodsShouldApplyAndReturn() {
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
			inOrder.verify(transactionMongoHandler).closeHandler();
			
			verifyNoMoreInteractions(clientMongoRepository, reservationMongoRepository);
		}

		@Test
		@DisplayName("Code throws IllegalArgumentException")
		void testDoInTransactionWhenCodeThrowsIllegalArgumentExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					throw new IllegalArgumentException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(INVALID_ARGUMENT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UpdateFailureException")
		void testDoInTransactionWhenCodeThrowsUpdateFailureExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					throw new UpdateFailureException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(UPDATE_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws NotNullConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsNotNullConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					throw new NotNullConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_NOT_NULL_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws UniquenessConstraintViolationException")
		void testDoInTransactionWhenCodeThrowsUniquenessConstraintViolationExceptionShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					throw new UniquenessConstraintViolationException();
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(VIOLATION_OF_UNIQUENESS_CONSTRAINT_ERROR_MSG);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Code throws others RuntimeException")
		void testDoInTransactionWhenCodeThrowsOthersRuntimeExceptionsShouldRollBackAndRethrow() {
			RuntimeException runtimeException = new RuntimeException();
			ClientReservationTransactionCode<Object> code =
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					throw runtimeException;
				};
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isEqualTo(runtimeException);
			
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler, never()).commitTransaction();
			verify(transactionMongoHandler).closeHandler();
		}

		@Test
		@DisplayName("Commit fails")
		void testDoInTransactionWhenCommitFailsShouldRollBackAndThrow() {
			ClientReservationTransactionCode<Object> code = 
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					clientRepository.findAll();
					reservationRepository.findAll();
					return null;
				};
			
			doThrow(new MongoCommandException(new BsonDocument(), new ServerAddress()))
				.when(transactionMongoHandler).commitTransaction();
			
			assertThatThrownBy(() -> transactionManager.doInTransaction(code))
				.isInstanceOf(TransactionException.class)
				.hasMessage(COMMIT_FAILURE_ERROR_MSG);
			
			verify(transactionMongoHandler).commitTransaction();
			verify(transactionMongoHandler).rollbackTransaction();
			verify(transactionMongoHandler).closeHandler();
		}
	}
}