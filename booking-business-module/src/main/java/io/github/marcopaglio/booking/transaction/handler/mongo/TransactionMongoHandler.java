package io.github.marcopaglio.booking.transaction.handler.mongo;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;

import io.github.marcopaglio.booking.transaction.handler.TransactionHandler;

/**
 * An implementation of {@code TransactionHandler} for using MongoDB transactions
 * via {@code ClientSession}.
 */
public class TransactionMongoHandler extends TransactionHandler<ClientSession> {

	/**
	 * Provides custom options to transactions.
	 */
	private TransactionOptions txnOptions;

	/**
	 * Constructs a handler for MongoDB transactions using the session opened with MongoDB
	 * and some custom optional transaction options.
	 * 
	 * @param session		the session opened with MongoDB.
	 * @param txnOptions	the optional transaction options.
	 */
	public TransactionMongoHandler(ClientSession session, TransactionOptions txnOptions) {
		super(session);
		this.txnOptions = txnOptions;
	}

	/**
	 * Starts a new MongoDB transaction via the session and using options if provided.
	 * 
	 * @throws IllegalStateException	if {@code session} has already an active transaction.
	 */
	@Override
	public void startTransaction() throws IllegalStateException {
		if (handler.hasActiveTransaction())
			throw new IllegalStateException("Transaction is already in progress.");
		
		if (txnOptions == null)
			handler.startTransaction();
		else
			handler.startTransaction(txnOptions);
	}

	/**
	 * Commits changes of the active MongoDB transaction via the session.
	 * 
	 * @throws IllegalStateException	if {@code session} has no active transaction.
	 */
	@Override
	public void commitTransaction() throws IllegalStateException {
		if (!handler.hasActiveTransaction())
			throw new IllegalStateException("There is no transaction started.");
		
		handler.commitTransaction();
	}

	/**
	 * Rolls back changes of the active MongoDB transaction via the session.
	 * 
	 * @throws IllegalStateException	if {@code session} has no active transaction.
	 */
	@Override
	public void rollbackTransaction() throws IllegalStateException {
		if (!handler.hasActiveTransaction())
			throw new IllegalStateException("There is no transaction started.");
		
		handler.abortTransaction();
	}

	/**
	 * Indicates whether a transaction is active on this session.
	 * 
	 * @return	{@code true} if there is an active transaction on the session;
	 * 			{@code false} otherwise.
	 */
	@Override
	public boolean hasActiveTransaction() {
		return handler.hasActiveTransaction();
	}
}
