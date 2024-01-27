package io.github.marcopaglio.booking.repository.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;

/**
 * Facade of repository layer for using with MongoDB database.
 *
 * @param <T>	the entity type managed by the repository.
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
}