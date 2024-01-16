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
	 * Starts a new MongoDB transaction via the session and using options if provided,
	 * if one isn't already active.
	 */
	@Override
	public void startTransaction() {
		if (!hasActiveTransaction()) {
			if (txnOptions == null)
				handler.startTransaction();
			else
				handler.startTransaction(txnOptions);
		}
	}

	/**
	 * Commits changes of the active MongoDB transaction via the session.
	 */
	@Override
	public void commitTransaction() {
		if (hasActiveTransaction())
			handler.commitTransaction();
	}

	/**
	 * Rolls back changes of the active MongoDB transaction via the session.
	 * Note: an already committed transaction cannot be rolled back.
	 */
	@Override
	public void rollbackTransaction() {
		if (hasActiveTransaction())
			handler.abortTransaction();
	}

	/**
	 * Closes the session, if still open.
	 */
	@Override
	public void closeHandler() {
		handler.close();
	}

	/**
	 * Indicates whether a transaction is active on this session.
	 * 
	 * @return	{@code true} if there is an active transaction on the session;
	 * 			{@code false} otherwise.
	 */
	private boolean hasActiveTransaction() {
		return handler.hasActiveTransaction();
	}
}