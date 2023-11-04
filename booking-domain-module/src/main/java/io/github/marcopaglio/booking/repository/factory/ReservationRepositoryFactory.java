package io.github.marcopaglio.booking.repository.factory;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.repository.postgres.ReservationPostgresRepository;
import jakarta.persistence.EntityManager;

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
	 * @param databaseName				the name of the database in which the repository works.
	 * @return							a new {@code ReservationMongoRepository}
	 * 									for facing the MongoDB database.
	 * @throws IllegalArgumentException	if at least {@code mongoClient} or {@code session} is null.
	 */
	public ReservationMongoRepository createReservationRepository(MongoClient mongoClient,
			ClientSession session, String databaseName) throws IllegalArgumentException {
		if (mongoClient == null)
			throw new IllegalArgumentException(
					"Cannot create a ReservationMongoRepository from a null Mongo client.");
		if (session == null)
			throw new IllegalArgumentException(
					"Cannot create a ReservationMongoRepository from a null Mongo client session.");
		
		return new ReservationMongoRepository(mongoClient, session, databaseName);
	}

	/**
	 * Creates a new repository for Reservation entities using PostgresSQL and a JPA provider.
	 * 
	 * @param em						the entity manager using PostgreSQL database.
	 * @return							a new {@code ReservationPostgresRepository}
	 * 									for facing the PostgreSQL database.
	 * @throws IllegalArgumentException	if {@code em} is null.
	 */
	public ReservationPostgresRepository createReservationRepository(EntityManager em) {
		if (em == null)
			throw new IllegalArgumentException(
					"Cannot create a ReservationPostgresRepository from a null Entity Manager.");
		
		return new ReservationPostgresRepository(em);
	}

}
