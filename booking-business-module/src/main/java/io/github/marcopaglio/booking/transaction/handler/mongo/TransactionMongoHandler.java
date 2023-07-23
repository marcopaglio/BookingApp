package io.github.marcopaglio.booking.transaction.handler.mongo;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;

import io.github.marcopaglio.booking.transaction.handler.TransactionHandler;

/**
 * An implementation for using MongoDB transactions.
 */
public class TransactionMongoHandler implements TransactionHandler {

	/**
	 * MongoDB handler for starting, committing and aborting transactions.
	 */
	private ClientSession session;

	/**
	 * Provides custom options to transactions.
	 */
	private TransactionOptions txnOptions;

	/**
	 * Creates a handler for MongoDB transactions using the session opened with MongoDB
	 * and some custom optional transaction options.
	 * 
	 * @param session		the session opened with MongoDB.
	 * @param txnOptions	the optional transaction options.
	 */
	public TransactionMongoHandler(ClientSession session, TransactionOptions txnOptions) {
		this.session = session;
		this.txnOptions = txnOptions;
	}

	/**
	 * Starts a new MongoDB transaction via the session and using options if provided.
	 * 
	 * @throws IllegalStateException	if {@code session} has already an active transaction.
	 */
	@Override
	public void startTransaction() throws IllegalStateException {
		if (session.hasActiveTransaction())
			throw new IllegalStateException("Transaction is already in progress.");
		
		if (txnOptions == null)
			session.startTransaction();
		else
			session.startTransaction(txnOptions);
	}

	/**
	 * Commits changes of the active MongoDB transaction.
	 * 
	 * @throws IllegalStateException	if {@code session} has no active transaction.
	 */
	@Override
	public void commitTransaction() throws IllegalStateException {
		if (!session.hasActiveTransaction())
			throw new IllegalStateException("There is no transaction started.");
		
		session.commitTransaction();
	}

	/**
	 * Rolls back changes of the active MongoDB transaction.
	 * 
	 * @throws IllegalStateException	if {@code session} has no active transaction.
	 */
	@Override
	public void rollbackTransaction() throws IllegalStateException {
		if (!session.hasActiveTransaction())
			throw new IllegalStateException("There is no transaction started.");
		
		session.abortTransaction();
	}
}
