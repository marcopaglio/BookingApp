package io.github.marcopaglio.booking.repository.factory;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.postgres.ClientPostgresRepository;
import jakarta.persistence.EntityManager;

/**
 * A factory of repositories for Client entities.
 */
public class ClientRepositoryFactory {

	/**
	 * Empty constructor.
	 */
	public ClientRepositoryFactory() {
		super();
	}

	/**
	 * Creates a new repository for Client entities using a MongoDB client and session.
	 * 
	 * @param mongoClient				the client using the MongoDB database.
	 * @param session					the session in which database operations are performed.
	 * @return							a new {@code ClientMongoRepository}
	 * 									for facing the MongoDB database.
	 * @throws IllegalArgumentException	if at least {@code mongoClient} or {@code session} is null.
	 */
	public ClientMongoRepository createClientRepository(MongoClient mongoClient, ClientSession session)
			throws IllegalArgumentException {
		if (mongoClient == null)
			throw new IllegalArgumentException(
					"Cannot create a ClientMongoRepository from a null Mongo client.");
		if (session == null)
			throw new IllegalArgumentException(
					"Cannot create a ClientMongoRepository from a null Mongo client session.");
		
		return new ClientMongoRepository(mongoClient, session);
	}

	/**
	 * Creates a new repository for Client entities using PostgresSQL and a JPA provider.
	 * 
	 * @param em						the entity manager using PostgreSQL database.
	 * @return							a new {@code ClientPostgresRepository}
	 * 									for facing the PostgreSQL database.
	 * @throws IllegalArgumentException	if {@code em} is null.
	 */
	public ClientPostgresRepository createClientRepository(EntityManager em)
			throws IllegalArgumentException {
		if (em == null)
			throw new IllegalArgumentException(
					"Cannot create a ClientPostgresRepository from a null Entity Manager.");
		
		return new ClientPostgresRepository(em);
	}
}
