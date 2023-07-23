package io.github.marcopaglio.booking.repository.factory;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;

/**
 * A factory of repositories for Reservation entities.
 */
public class ReservationRepositoryFactory {

	/**
	 * Empty constructor.
	 */
	public ReservationRepositoryFactory() {
		super();
	}

	/**
	 * Creates a new repository for Reservation entities using a MongoDB client and session.
	 * 
	 * @param mongoClient				the client using the MongoDB database.
	 * @param session					the session in which database operations are performed.
	 * @return							a new {@code ReservationMongoRepository}
	 * 									for facing the MongoDB database.
	 * @throws IllegalArgumentException	if at least {@code mongoClient} or {@code session} is null.
	 */
	public ReservationRepository createReservationRepository(MongoClient mongoClient, ClientSession session)
			throws IllegalArgumentException {
		if (mongoClient == null)
			throw new IllegalArgumentException(
					"Cannot create a ReservationMongoRepository from a null Mongo client.");
		if (session == null)
			throw new IllegalArgumentException(
					"Cannot create a ReservationMongoRepository from a null Mongo client session.");
		
		return new ReservationMongoRepository(mongoClient, session);
	}

}