package io.github.marcopaglio.booking.transaction.handler.factory;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

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
	 * Creates a new session for handling transactions using a MongoDB client.
	 * 
	 * @param mongoClient				the client using the MongoDB database.
	 * @return							a new {@code ClientSession} for creating transactions.
	 * @throws IllegalArgumentException	if {@code mongoClient} is null.
	 */
	public ClientSession createTransactionHandler(MongoClient mongoClient)
			throws IllegalArgumentException {
		if (mongoClient == null)
			throw new IllegalArgumentException("Cannot create a ClientSession from a null Mongo client.");
		
		return mongoClient.startSession();
	}
}
