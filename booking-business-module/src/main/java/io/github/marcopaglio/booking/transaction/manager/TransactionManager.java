package io.github.marcopaglio.booking.transaction.manager;

import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;

/**
 * An interface for handing transactions in the booking application.
 */
public interface TransactionManager {

	/**
	 * Executes code that involves the {@code ClientRepository}'s method(s) in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 */
	<R> R doInTransaction(ClientTransactionCode<R> code);

	/**
	 * Executes code that involves the {@code ReservationRepository}'s method(s) in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 */
	<R> R doInTransaction(ReservationTransactionCode<R> code);

	/**
	 * Executes code that involves both {@code ClientRepository}'s and {@code ReservationRepository}'s
	 * methods in a single transaction.
	 * 
	 * @param <R>	the returned type of executed code.
	 * @param code	the code to execute.
	 * @return		something depending on execution code.
	 */
	<R> R doInTransaction(ClientReservationTransactionCode<R> code);
}
