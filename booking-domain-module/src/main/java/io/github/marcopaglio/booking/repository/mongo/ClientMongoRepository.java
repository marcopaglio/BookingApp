package io.github.marcopaglio.booking.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.MongoWriteException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;

import static io.github.marcopaglio.booking.model.BaseEntity.ID_DB;
import static io.github.marcopaglio.booking.model.Client.FIRSTNAME_DB;
import static io.github.marcopaglio.booking.model.Client.LASTNAME_DB;

/**
 * Implementation of repository layer through MongoDB for Client entities of the booking application.
 */
public class ClientMongoRepository extends MongoRepository<Client> implements ClientRepository {
	/**
	 * Name of the database in which the repository works.
	 */
	public static final String BOOKING_DB_NAME = "booking_db";

	/**
	 * Name of the collection managed by the repository.
	 */
	public static final String CLIENT_COLLECTION_NAME = "booking_client";

	/**
	 * Constructs a repository layer for Client entities using MongoDB database. 
	 * The construction generates and configures a collection for using by the repository.
	 * 
	 * @param client	the {@code MongoClient} used to retrieve the collection.
	 * @param session	the {@code ClientSession} used to communicate with MongoDB database.
	 */
	public ClientMongoRepository(MongoClient client, ClientSession session) {
		super(client
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(CLIENT_COLLECTION_NAME, Client.class),
				session);
		
		// configuration
		collection.createIndex(session, Indexes.descending(FIRSTNAME_DB, LASTNAME_DB), 
				new IndexOptions().unique(true));
	}

	/**
	 * Retrieves all the clients from the MongoDB database in a list.
	 * 
	 * @return	the {@code List} of {@code Client}s found in the repository.
	 */
	@Override
	public List<Client> findAll() {
		return StreamSupport
				.stream(collection.find(session).spliterator(), false)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the unique client with the specified identifier from the MongoDB database if exists.
	 * 
	 * @param id	the identifier of the client to find.
	 * @return		an {@code Optional} contained the {@code Client} identified by {@code id} if exists;
	 * 				an {@code Optional} empty if it doesn't exist.
	 */
	@Override
	public Optional<Client> findById(UUID id) {
		Client client = collection.find(session, Filters.eq(ID_DB, id)).first();
		
		if (client != null)
			return Optional.of(client);
		return Optional.empty();
	}

	/**
	 * Retrieves the unique client with the specified name and surname
	 * from the MongoDB database if exists.
	 * 
	 * @param firstName					the name of the client to find.
	 * @param lastName					the surname of the client to find.
	 * @return							an {@code Optional} contained the {@code Client}
	 * 									named {@code firstName} and {@code lastName} if exists;
	 * 									an {@code Optional} empty if it doesn't exist.
	 */
	@Override
	public Optional<Client> findByName(String firstName, String lastName) {
		Client client = collection.find(session, Filters.and(
					Filters.eq(FIRSTNAME_DB, firstName),
					Filters.eq(LASTNAME_DB, lastName)
				)).first();
		
		if (client != null)
			return Optional.of(client);
		return Optional.empty();
	}

	/**
	 * Inserts a new Client in the MongoDB database or saves changes of an existing one.
	 * Note: a Client without an identifier is considered to be entered,
	 * while with the identifier it will be updated.
	 *
	 * @param client									the Client to save.
	 * @return											the {@code Client} saved.
	 * @throws IllegalArgumentException					if {@code client} is null.
	 * @throws UpdateFailureException					if you try to save changes of a no longer
	 * 													existing client.
	 * @throws NotNullConstraintViolationException		if {@code firstName} or {@code lastName}
	 * 													of {@code client} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code [firstName, lastName]}
	 * 													of {@code client} to save are already present.
	 */
	@Override
	public Client save(Client client) throws IllegalArgumentException, UpdateFailureException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		
		if (client.getFirstName() == null || client.getLastName() == null)
			throw new NotNullConstraintViolationException("Client to save must have both not-null names.");
		
		try {
			if(client.getId() == null) {
				client.setId(UUID.randomUUID());
				collection.insertOne(session, client);
			} else {
				replaceIfFound(client);
			}
		} catch(MongoWriteException e) {
			throw new UniquenessConstraintViolationException(
					"Client to save violates uniqueness constraints.");
		}
		return client;
	}

	/**
	 * Replace the Client with the same id in the MongoDB database.
	 * 
	 * @param client					the replacement client.
	 * @throws UpdateFailureException	if there is no client with the same ID to replace.
	 */
	private void replaceIfFound(Client client) throws UpdateFailureException {
		if (collection.replaceOne(
					session,
					Filters.eq(ID_DB, client.getId()),
					client,
					new ReplaceOptions().upsert(false))
				.getModifiedCount() == 0)
			throw new UpdateFailureException(
				"Client to update is not longer present in the repository.");
	}

	/**
	 * Removes the unique specified client from the MongoDB database.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code client} is null.
	 */
	@Override
	public void delete(Client client) throws IllegalArgumentException {
		if(client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
		
		collection.deleteOne(session, Filters.eq(ID_DB, client.getId()));
	}
}
