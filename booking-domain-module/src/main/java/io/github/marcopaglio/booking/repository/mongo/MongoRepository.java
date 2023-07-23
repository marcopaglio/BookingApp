package io.github.marcopaglio.booking.repository.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;

/**
 * Facade of repository layer for using with MongoDB database.
 */
public abstract class MongoRepository<T> {

	/**
	 * Collection of entities of type T used by the repository layer.
	 */
	protected MongoCollection<T> collection;

	/**
	 * Session used to communicate with MongoDB database.
	 */
	protected ClientSession session;

	/**
	 * Sets the collection of entities of type T and the session used by the repository layer.
	 * 
	 * @param collection	the {@code MongoCollection} of type {@code T} to set.
	 * @param session		the {@code ClientSession} to set.
	 */
	protected MongoRepository(MongoCollection<T> collection, ClientSession session) {
		super();
		this.collection = collection;
		this.session = session;
	}

	/**
	 * Retrieves the collection of entities of type T used by the repository layer.
	 * 
	 * @return	the {@code MongoCollection} of type {@code T} used by the repository.
	 */
	public final MongoCollection<T> getCollection() {
		return collection;
	}

	/**
	 * Retrieves the session used by the repository layer.
	 * 
	 * @return	the {@code ClientSession} used by the repository.
	 */
	public final ClientSession getSession() {
		return session;
	}

	/**
	 * Generates a message for the violation of uniqueness constraints by an operation.
	 * 
	 * @param operation	the name of the operation that violates the constraint.
	 */
	protected String uniquenessConstraintViolationMsg(String operation) {
		return "The " + operation + " violates uniqueness constraints.";
	}
}
