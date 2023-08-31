package io.github.marcopaglio.booking.transaction.handler;

import io.github.marcopaglio.booking.annotation.Generated;

/**
 * Provides methods for using transactions in the booking application.
 *
 * @param <T>	the type of transactions handler.
 */
public abstract class TransactionHandler<T> {

	/**
	 * The handler for starting, committing and aborting transactions.
	 */
	protected T handler;

	/**
	 * Constructs a handler for using transactions.
	 * 
	 * @param handler	the handler used for transactions.
	 */
	protected TransactionHandler(T handler) {
		super();
		this.handler = handler;
	}

	/**
	 * Retrieves the handler of type T used for using transactions.
	 * 
	 * @return	the transaction handler of type {@code T}.
	 */
	@Generated
	public final T getHandler() {
		return handler;
	}

	/**
	 * Starts a new transaction, if one isn't already active.
	 */
	public abstract void startTransaction();

	/**
	 * Commits changes of the active transaction.
	 */
	public abstract void commitTransaction();

	/**
	 * Rolls back changes of the active transaction.
	 */
	public abstract void rollbackTransaction();

	/**
	 * Indicates whether a transaction is active on this handler.
	 * 
	 * @return	{@code true} if there is an active transaction on the handler;
	 * 			{@code false} otherwise.
	 */
	public abstract boolean hasActiveTransaction();
}
