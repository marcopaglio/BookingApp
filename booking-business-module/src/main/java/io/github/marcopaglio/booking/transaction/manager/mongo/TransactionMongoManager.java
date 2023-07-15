package io.github.marcopaglio.booking.transaction.manager.mongo;

import com.mongodb.client.ClientSession;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/**
 * An implementation for managing code executed on MongoDB within transactions.
 */
public class TransactionMongoManager implements TransactionManager {
	/**
	 * Specifies that the reason the transaction fails is the passing of an invalid argument.
	 */
	private static final String INVALID_ARGUMENT = "invalid argument(s) passed";

	/**
	 * Specifies that the reason the transaction fails is a not-null constraint violation.
	 */
	private static final String VIOLATION_OF_NOT_NULL_CONSTRAINT = "violation of not-null constraint(s)";

	/**
	 * Specifies that the reason the transaction fails is a uniqueness constraint violation.
	 */
	private static final String VIOLATION_OF_UNIQUENESS_CONSTRAINT = "violation of uniqueness constraint(s)";

	/**
	 * Used for communicating with MongoDB and creating transactions.
	 */
	private ClientSession session;

	/**
	 * Used for applying code to {@code ClientRepository}.
	 */
	private ClientMongoRepository clientRepository;

	/**
	 * Used for applying code to {@code ReservationRepository}.
	 */
	private ReservationMongoRepository reservationRepository;

	/**
	 * Constructs a manager for applying code that uses entity repositories 
	 * using MongoDB transactions.
	 * 
	 * @param session				the MongoDB session used for creating transactions.
	 * @param clientRepository		the repository that manages {@code Client} entities.
	 * @param reservationRepository	the repository that manages {@code Reservation} entities.
	 */
	public TransactionMongoManager(ClientSession session, ClientMongoRepository clientRepository,
			ReservationMongoRepository reservationRepository) {
		this.session = session;
		this.clientRepository = clientRepository;
		this.reservationRepository = reservationRepository;
	}

	/**
	 * Executes code that involves the {@code ClientRepository}'s method(s) on MongoDB
	 * in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException {
		try {
			session.startTransaction();
			R toBeReturned = code.apply(clientRepository);
			session.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(NotNullConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			session.abortTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves the {@code ReservationRepository}'s method(s) on MongoDB
	 * in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException {
		try {
			session.startTransaction();
			R toBeReturned = code.apply(reservationRepository);
			session.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(NotNullConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			session.abortTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves both {@code ClientRepository}'s and {@code ReservationRepository}'s
	 * methods on MongoDB in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException {
		try {
			session.startTransaction();
			R toBeReturned = code.apply(clientRepository, reservationRepository);
			session.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(NotNullConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			session.abortTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			session.abortTransaction();
			throw e;
		}
	}

	/**
	 * Generates a message for the failure of the transaction.
	 * 
	 * @param reason	the cause of the failure.
	 */
	private String transactionFailureMsg(String reason) {
		return "Transaction fails due to " + reason + ".";
	}
}
