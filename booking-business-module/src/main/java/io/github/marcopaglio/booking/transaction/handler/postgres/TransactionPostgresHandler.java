package io.github.marcopaglio.booking.transaction.handler.postgres;

import io.github.marcopaglio.booking.transaction.handler.TransactionHandler;
import jakarta.persistence.EntityManager;

/**
 * An implementation of {@code TransactionHandler} for using PostgreSQL transactions
 * via {@code EntityManager}.
 */
public class TransactionPostgresHandler extends TransactionHandler<EntityManager> {

	/**
	 * Constructs a handler for PostgreSQL transactions using an entity manager
	 * to interact with the PostgreSQL database. 
	 * 
	 * @param em	the entity manager used to interact with the persistence provider.
	 */
	public TransactionPostgresHandler(EntityManager em) {
		super(em);
	}

	/**
	 * Starts a new PostgreSQL transaction via the entity manager, if one isn't already active.
	 */
	@Override
	public void startTransaction() {
		if (!handler.getTransaction().isActive())
			handler.getTransaction().begin();
	}

	/**
	 * Commits changes of the active PostgreSQL transaction via the entity manager.
	 */
	@Override
	public void commitTransaction() {
		if (handler.getTransaction().isActive())
			handler.getTransaction().commit();
	}

	/**
	 * Rolls back changes of the active PostgreSQL transaction via the entity manager.
	 */
	@Override
	public void rollbackTransaction() {
		if (handler.getTransaction().isActive())
			handler.getTransaction().rollback();
	}

	/**
	 * Indicates whether a transaction is active on this entity manager.
	 * 
	 * @return	{@code true} if there is an active transaction on the entity manager;
	 * 			{@code false} otherwise.
	 */
	@Override
	public boolean hasActiveTransaction() {
		return handler.getTransaction().isActive();
	}
}
