package io.github.marcopaglio.booking.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;

import static io.github.marcopaglio.booking.model.Entity.ID_DB;
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
	 * @param mongoClient	the {@code MongoClient} used to retrieve the collection.
	 */
	public ClientMongoRepository(MongoClient mongoClient) {
		super(mongoClient
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(CLIENT_COLLECTION_NAME, Client.class));
		
		// configuration
		collection.createIndex(Indexes.descending("name", "surname"), 
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
				.stream(collection.find().spliterator(), false)
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
		Client client = collection.find(Filters.eq(ID_DB, id)).first();
		
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
		Client client = collection.find(Filters.and(
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
	 * @throws NotNullConstraintViolationException		if {@code firstName} or {@code lastName}
	 * 													of {@code client} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code [firstName, lastName]}
	 * 													of {@code client} to save are already present.
	 */
	@Override
	public Client save(Client client) throws IllegalArgumentException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		
		if (client.getFirstName() == null || client.getLastName() == null)
			throw new NotNullConstraintViolationException("Client to save must have both not-null names.");
		
		if(client.getId() == null) {
			client.setId(UUID.randomUUID());
			try {
				collection.insertOne(client);
			} catch(MongoWriteException e) {
				throw new UniquenessConstraintViolationException(
						"The insertion violates uniqueness constraints.");
			}
		} else {
			try {
				collection.replaceOne(Filters.eq(ID_DB, client.getId()), client);
			} catch(MongoWriteException e) {
				throw new UniquenessConstraintViolationException(
						"The update violates uniqueness constraints.");
			}
		}
		return client;
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
		
		collection.deleteOne(Filters.eq(ID_DB, client.getId()));
	}
}
