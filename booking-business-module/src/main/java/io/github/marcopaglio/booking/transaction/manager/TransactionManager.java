package io.github.marcopaglio.booking.transaction.manager;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.handler.TransactionHandler;

/**
 * Provides methods for managing transactions in the booking application.
 */
public abstract class TransactionManager {
	/**
	 * Specifies that the reason the transaction fails is the passing of an invalid argument.
	 */
	public static final String INVALID_ARGUMENT = "invalid argument(s) passed";

	/**
	 * Specifies that the reason the transaction fails is an update failure.
	 */
	public static final String UPDATE_FAILURE = "an update failure";

	/**
	 * Specifies that the reason the transaction fails is a not-null constraint violation.
	 */
	public static final String VIOLATION_OF_NOT_NULL_CONSTRAINT = "violation of not-null constraint(s)";

	/**
	 * Specifies that the reason the transaction fails is a uniqueness constraint violation.
	 */
	public static final String VIOLATION_OF_UNIQUENESS_CONSTRAINT = "violation of uniqueness constraint(s)";

	/**
	 * Prepares to execution of code that involves the {@code ClientRepository}'s method(s)
	 * in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws a {@code RuntimeException}
	 * 								due to database inconsistency.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	public abstract <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException, RuntimeException;

	/**
	 * Prepares to execution of code that involves the {@code ReservationRepository}'s method(s)
	 * in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws a {@code RuntimeException}
	 * 								due to database inconsistency.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	public abstract <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException, RuntimeException;

	/**
	 * Prepares to execution of code that involves both {@code ClientRepository}'s and
	 * {@code ReservationRepository}'s methods in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws a {@code RuntimeException}
	 * 								due to database inconsistency.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	public abstract <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException, RuntimeException;

	/**
	 * Executes code that involves the {@code ClientRepository}'s method(s)
	 * in a single transaction managed by a {@code TransactionHandler}.
	 * 
	 * @param <R>						the returned type of executed code.
	 * @param code						the code to execute.
	 * @param handler					the handler of the transaction.
	 * @param clientRepository			a repository of {@code Client} entities
	 * 									used by the code.
	 * @return							something depending on execution code.
	 * @throws TransactionException		if {@code code} throws {@code IllegalArgumentException},
	 * 									{@code UpdateFailureException},
	 * 									{@code NotNullConstraintViolationException} or
	 * 									{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException			if an unexpected {@code RuntimeException} occurs.
	 */
	protected <R> R executeInTransaction(ClientTransactionCode<R> code,
			TransactionHandler<?> handler, ClientRepository clientRepository)
			throws TransactionException, RuntimeException {
		try {
			handler.startTransaction();
			R toBeReturned = code.apply(clientRepository);
			handler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			handler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves the {@code ReservationRepository}'s method(s)
	 * in a single transaction managed by a {@code TransactionHandler}.
	 * 
	 * @param <R>						the returned type of executed code.
	 * @param code						the code to execute.
	 * @param handler					the handler of the transaction.
	 * @param reservationRepository		a repository of {@code Reservation} entities
	 * 									used by the code.
	 * @return							something depending on execution code.
	 * @throws TransactionException		if {@code code} throws {@code IllegalArgumentException},
	 * 									{@code UpdateFailureException},
	 * 									{@code NotNullConstraintViolationException} or
	 * 									{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException			if an unexpected {@code RuntimeException} occurs.
	 */
	protected <R> R executeInTransaction(ReservationTransactionCode<R> code, TransactionHandler<?> handler,
			ReservationRepository reservationRepository)
			throws TransactionException, RuntimeException {
		try {
			handler.startTransaction();
			R toBeReturned = code.apply(reservationRepository);
			handler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			handler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Executes code that involves both {@code ClientRepository}'s and {@code ReservationRepository}'s
	 * methods in a single transaction managed by a {@code TransactionHandler}.
	 * 
	 * @param <R>						the returned type of executed code.
	 * @param code						the code to execute.
	 * @param handler					the handler of the transaction.
	 * @param clientRepository			a repository of {@code Client} entities
	 * 									used by the code.
	 * @param reservationRepository		a repository of {@code Reservation} entities
	 * 									used by the code.
	 * @return							something depending on execution code.
	 * @throws TransactionException		if {@code code} throws {@code IllegalArgumentException},
	 * 									{@code UpdateFailureException},
	 * 									{@code NotNullConstraintViolationException} or
	 * 									{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException			if an unexpected {@code RuntimeException} occurs.
	 */
	protected <R> R executeInTransaction(ClientReservationTransactionCode<R> code,
			TransactionHandler<?> handler, ClientRepository clientRepository,
			ReservationRepository reservationRepository)
			throws TransactionException, RuntimeException {
		try {
			handler.startTransaction();
			R toBeReturned = code.apply(clientRepository, reservationRepository);
			handler.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(INVALID_ARGUMENT));
		} catch(UpdateFailureException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(UPDATE_FAILURE));
		} catch(NotNullConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_NOT_NULL_CONSTRAINT));
		} catch(UniquenessConstraintViolationException e) {
			handler.rollbackTransaction();
			throw new TransactionException(transactionFailureMsg(VIOLATION_OF_UNIQUENESS_CONSTRAINT));
		} catch(RuntimeException e) {
			handler.rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Generates a message for the failure of the transaction.
	 * 
	 * @param reason	the cause of the failure.
	 * @return			a {@code String} message about the failure.
	 */
	protected String transactionFailureMsg(String reason) {
		return "Transaction fails due to " + reason + ".";
	}
}
