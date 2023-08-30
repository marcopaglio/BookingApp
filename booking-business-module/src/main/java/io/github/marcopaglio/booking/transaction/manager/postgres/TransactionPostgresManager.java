package io.github.marcopaglio.booking.transaction.manager.postgres;

import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

public class TransactionPostgresManager implements TransactionManager {

	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException {
		// TODO Auto-generated method stub
		return null;
	}

}
