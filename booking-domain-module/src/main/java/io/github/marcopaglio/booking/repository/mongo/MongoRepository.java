package io.github.marcopaglio.booking.repository.mongo;

import com.mongodb.client.MongoCollection;

public abstract class MongoRepository<T> {
	
	private MongoCollection<T> collection;

	protected MongoRepository(MongoCollection<T> collection) {
		super();
		this.collection = collection;
	}

	/**
	 * @return the collection
	 */
	public final MongoCollection<T> getCollection() {
		return collection;
	}

}
