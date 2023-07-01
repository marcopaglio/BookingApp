package io.github.marcopaglio.booking.repository.mongo;

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
	 * Sets the collection of entities of type T used by the repository layer.
	 * 
	 * @param collection	the {@code MongoCollection} of type {@code T} to set.
	 */
	protected MongoRepository(MongoCollection<T> collection) {
		super();
		this.collection = collection;
	}

	/**
	 * Retrieves the collection of entities of type T used by the repository layer.
	 * 
	 * @return collection	the {@code MongoCollection} of type {@code T} used by the repository.
	 */
	public final MongoCollection<T> getCollection() {
		return collection;
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
