package io.github.marcopaglio.booking.transaction.handler;

/**
 * An interface for using transactions in the booking application.
 */
public interface TransactionHandler {

	/**
	 * Starts a new transaction.
	 * 
	 * @throws IllegalStateException	if the handler has already an active transaction.
	 */
	public void startTransaction() throws IllegalStateException;

	/**
	 * Commits changes of the active transaction.
	 * 
	 * @throws IllegalStateException	if there is no active transaction.
	 */
	public void commitTransaction() throws IllegalStateException;

	/**
	 * Rolls back changes of the active transaction.
	 * 
	 * @throws IllegalStateException	if there is no active transaction.
	 */
	public void rollbackTransaction() throws IllegalStateException;

	/**
	 * Indicates whether a transaction is active on this handler.
	 * 
	 * @return	{@code true} if there is an active transaction on this handler;
	 * 			{@code false} otherwise.
	 */
	public boolean hasActiveTransaction();
}
