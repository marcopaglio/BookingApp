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

	private ClientSession clientSession;
	private ClientMongoRepository clientRepository;
	private ReservationMongoRepository reservationRepository;

	public TransactionMongoManager(ClientSession clientSession, ClientMongoRepository clientRepository,
			ReservationMongoRepository reservationRepository) {
		this.clientSession = clientSession;
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
	 * @throws RuntimeException		if {@code} throws a {@code RuntimeException} other than
	 * 								{@code IllegalArgumentException},
	 * 								{@code NotNullConstraintViolationException} and
	 * 								{@code UniquenessConstraintViolationException}.
	 */
	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code)
			throws TransactionException, RuntimeException {
		try {
			clientSession.startTransaction();
			R toBeReturned = code.apply(clientRepository);
			clientSession.commitTransaction();
			return toBeReturned;
		} catch(IllegalArgumentException e) {
			clientSession.abortTransaction();
			throw new TransactionException(transactionFailureMsg("invalid argument(s) passed"));
		} catch(NotNullConstraintViolationException e) {
			clientSession.abortTransaction();
			throw new TransactionException(transactionFailureMsg("violation of not-null constraint(s)"));
		} catch(UniquenessConstraintViolationException e) {
			clientSession.abortTransaction();
			throw new TransactionException(transactionFailureMsg("violation of uniqueness constraint(s)"));
		} catch(RuntimeException e) {
			clientSession.abortTransaction();
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

	/**
	 * Executes code that involves the {@code ReservationRepository}'s method(s) on MongoDB
	 * in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 */
	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Executes code that involves both {@code ClientRepository}'s and {@code ReservationRepository}'s
	 * methods on MongoDB in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 */
	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) {
		// TODO Auto-generated method stub
		return null;
	}

}
