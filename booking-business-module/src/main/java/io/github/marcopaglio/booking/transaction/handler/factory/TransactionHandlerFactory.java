package io.github.marcopaglio.booking.transaction.handler.factory;

import com.mongodb.TransactionOptions;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.transaction.handler.mongo.TransactionMongoHandler;
import io.github.marcopaglio.booking.transaction.handler.postgres.TransactionPostgresHandler;
import jakarta.persistence.EntityManagerFactory;

/**
 * A factory of transaction handlers.
 */
public class TransactionHandlerFactory {

	/**
	 * Empty constructor.
	 */
	public TransactionHandlerFactory() {
		super();
	}

	/**
	 * Creates a new session for handling MongoDB transactions using a client.
	 * 
	 * @param mongoClient				the client using the MongoDB database.
	 * @param txnOptions				the options used in the transaction.
	 * @return							a new {@code ClientSession} for creating transactions.
	 * @throws IllegalArgumentException	if {@code mongoClient} is null.
	 */
	public TransactionMongoHandler createTransactionHandler(MongoClient mongoClient, TransactionOptions txnOptions)
			throws IllegalArgumentException {
		if (mongoClient == null)
			throw new IllegalArgumentException("Cannot create a TransactionMongoHandler from a null MongoDB client.");
		
		return new TransactionMongoHandler(mongoClient.startSession(), txnOptions);
	}

	/**
	 * Creates a new entity manager for handling PostgreSQL transactions using
	 * an EntityManagerFactory.
	 * 
	 * @param emf						the entity manager factory for interacting with
	 * 									the PostgreSQL database.
	 * @return							a new {@code TransactionPostgresHandler} for
	 * 									creating transactions.
	 * @throws IllegalArgumentException	if {@code emf} is null.
	 */
	public TransactionPostgresHandler createTransactionHandler(EntityManagerFactory emf) {
		if (emf == null)
			throw new IllegalArgumentException("Cannot create a TransactionPostgresHandler from a null EntityManagerFactory.");
		
		return new TransactionPostgresHandler(emf.createEntityManager());
	}
}