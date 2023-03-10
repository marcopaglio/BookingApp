package io.github.marcopaglio.booking.transaction.manager;

import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;

/*
 * An interface for handing transactions in booking application.
 */
public interface TransactionManager {

	/*
	 * Executes the ClientRepository's method in a single transaction.
	 */
	<R> R doInTransaction(ClientTransactionCode<R> code);

	/*
	 * Executes the ReservationRepository's method in a single transaction.
	 */
	<R> R doInTransaction(ReservationTransactionCode<R> code);

	/*
	 * Executes both ClientRepository's and ReservationRepository's
	 * methods in a single transaction.
	 */
	<R> R doInTransaction(ClientReservationTransactionCode<R> code);
}
