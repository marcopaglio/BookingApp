package io.github.marcopaglio.booking.transaction.handler.postgres;

import io.github.marcopaglio.booking.transaction.handler.TransactionHandler;
import jakarta.persistence.EntityManager;

/**
 * An implementation for using PostgreSQL transactions.
 */
public class TransactionPostgresHandler implements TransactionHandler {

	/**
	 * PostgreSQL handler for starting, committing and aborting transactions.
	 */
	private EntityManager em;

	/**
	 * Creates a handler for PostgreSQL transactions using an entity manager
	 * to interact with the PostgreSQL database. 
	 * @param em
	 */
	public TransactionPostgresHandler(EntityManager em) {
		this.em = em;
	}

	/**
	 * Starts a new PostgreSQL transaction via the entity manager.
	 * 
	 * @throws IllegalStateException	if {@code em} has already an active transaction.
	 */
	@Override
	public void startTransaction() throws IllegalStateException {
		if (em.getTransaction().isActive())
			throw new IllegalStateException("Transaction is already in progress.");
		
		em.getTransaction().begin();
	}

	/**
	 * Commits changes of the active PostgreSQL transaction via the entity manager.
	 * 
	 * @throws IllegalStateException	if {@code em} has no active transaction.
	 */
	@Override
	public void commitTransaction() throws IllegalStateException {
		if (!em.getTransaction().isActive())
			throw new IllegalStateException("There is no transaction started.");
		
		em.getTransaction().commit();
	}

	/**
	 * Rolls back changes of the active PostgreSQL transaction via the entity manager.
	 * 
	 * @throws IllegalStateException	if {@code em} has no active transaction.
	 */
	@Override
	public void rollbackTransaction() throws IllegalStateException {
		if (!em.getTransaction().isActive())
			throw new IllegalStateException("There is no transaction started.");
		
		em.getTransaction().rollback();
	}

	/**
	 * Indicates whether a transaction is active on this entity manager.
	 * 
	 * @return	{@code true} if there is an active transaction on this handler;
	 * 			{@code false} otherwise.
	 */
	@Override
	public boolean hasActiveTransaction() {
		return em.getTransaction().isActive();
	}
}
