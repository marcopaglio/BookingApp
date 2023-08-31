package io.github.marcopaglio.booking.transaction.manager.postgres;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.handler.postgres.TransactionPostgresHandler;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * An implementation for managing code executed on PostgreSQL within transactions.
 */
public class TransactionPostgresManager implements TransactionManager {
	/**
	 * Specifies that the reason the transaction fails is the passing of an invalid argument.
	 */
	private static final String INVALID_ARGUMENT = "invalid argument(s) passed";

	/**
	 * Specifies that the reason the transaction fails is an update failure.
	 */
	private static final String UPDATE_FAILURE = "an update failure";

	/**
	 * Specifies that the reason the transaction fails is a not-null constraint violation.
	 */
	private static final String VIOLATION_OF_NOT_NULL_CONSTRAINT = "violation of not-null constraint(s)";

	/**
	 * Specifies that the reason the transaction fails is a uniqueness constraint violation.
	 */
	private static final String VIOLATION_OF_UNIQUENESS_CONSTRAINT = "violation of uniqueness constraint(s)";

	/**
	 * Used for executing code on {@code ClientRepository} and/or {@code ReservationRepository}
	 * into transactions.
	 * Particularly, it allows to create entity manager, transactions and repositories.
	 */
	private EntityManagerFactory emf;

	/**
	 * Used for creation of {@code EntityManager} instances.
	 */
	private TransactionHandlerFactory transactionHandlerFactory;

	/**
	 * Used for creation of {@code ClientPostgresRepository} instances.
	 */
	private ClientRepositoryFactory clientRepositoryFactory;

	/**
	 * Used for creation of {@code ReservationPostgresRepository} instances.
	 */
	private ReservationRepositoryFactory reservationRepositoryFactory;

	/**
	 * Constructs a manager for applying code that uses entity repositories 
	 * using PostgreSQL transactions.
	 * 
	 * @param emf							the entity manager factory for the PostgreSQL database.
	 * @param transactionHandlerFactory		the factory for create instances
	 * 										of {@code EntityManager}.
	 * @param clientRepositoryFactory		the factory to create instances
	 * 										of {@code ClientPostgresRepository}.
	 * @param reservationRepositoryFactory	the factory to create instances
	 * 										of {@code ReservationPostgresRepository}.
	 */
	public TransactionPostgresManager(EntityManagerFactory emf, TransactionHandlerFactory transactionHandlerFactory,
			ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		this.emf = emf;
		this.transactionHandlerFactory = transactionHandlerFactory;
		this.clientRepositoryFactory = clientRepositoryFactory;
		this.reservationRepositoryFactory = reservationRepositoryFactory;
	}

	/**
	 * Executes code that involves the {@code ClientRepository}'s method(s) on PostgreSQL
	 * in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		try {
			sessionHandler.startTransaction();
			R toBeReturned = code.apply(
					clientRepositoryFactory.createClientRepository(sessionHandler.getEntityManager()));
			sessionHandler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			sessionHandler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves the {@code ReservationRepository}'s method(s) on PostgreSQL
	 * in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		try {
			sessionHandler.startTransaction();
			R toBeReturned = code.apply(
					reservationRepositoryFactory.createReservationRepository(
							sessionHandler.getEntityManager()));
			sessionHandler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			sessionHandler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves both {@code ClientRepository}'s and {@code ReservationRepository}'s
	 * methods on PostgreSQL in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		try {
			sessionHandler.startTransaction();
			R toBeReturned = code.apply(
					clientRepositoryFactory.createClientRepository(
							sessionHandler.getEntityManager()),
					reservationRepositoryFactory.createReservationRepository(
							sessionHandler.getEntityManager()));
			sessionHandler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			sessionHandler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			sessionHandler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Generates a message for the failure of the transaction.
	 * 
	 * @param reason	the cause of the failure.
	 * @return			a {@code String} message about the failure.
	 */
	private String transactionFailureMsg(String reason) {
		return "Transaction fails due to " + reason + ".";
	}
}
