package io.github.marcopaglio.booking.repository.mongo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
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
	 * @return	the {@code List} of {@code Clients} found in the repository.
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
	 * @param id						the identifier of the client to find.
	 * @return							an {@code Optional} contained the {@code Client}
	 * 									with {@code id} if exists;
	 * 									an {@code Optional} empty if it doesn't exist.
	 * @throws IllegalArgumentException	if {@code id} is null.
	 */
	@Override
	public Optional<Client> findById(UUID id) throws IllegalArgumentException {
		if (id == null)
			throw new IllegalArgumentException("Identifier of client to find cannot be null.");
		
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
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 */
	@Override
	public Optional<Client> findByName(String firstName, String lastName)
			throws IllegalArgumentException {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Name(s) of client to find cannot be null.");
		
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
	 *
	 * @param client							the Client to save.
	 * @return									the {@code Client} saved.
	 * @throws IllegalArgumentException			if {@code client} is null
	 * 											or a not-null constraint is violated.
	 * @throws InstanceAlreadyExistsException	if a uniqueness constraint is violated.
	 */
	@Override
	public Client save(Client client) throws IllegalArgumentException, InstanceAlreadyExistsException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		
		if (client.getFirstName() == null || client.getLastName() == null)
			throw new IllegalArgumentException("Client to save must have both not-null names.");
		
		if(client.getId() == null)
			client.setId(UUID.randomUUID());
		// TODO: Update or replace: cambiare anche javadoc
		// TODO: CONSEGUENZE: cambia la logica con cui testare le collissioni di id perché
		// anziché usare setId fuori dal metodo va stubbato quello dentro al metodo

		try {
			collection.insertOne(client);
		} catch(MongoWriteException e) {
			throw new InstanceAlreadyExistsException("The insertion violates uniqueness constraints.");
		}
		return client;
	}

	/**
	 * Removes the unique client with specified name and surname from the MongoDB database.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code client} is null.
	 * @throws NoSuchElementException	if {@code client} is not in database.
	 */
	@Override
	public void delete(Client client) throws IllegalArgumentException, NoSuchElementException {
		if(client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
		
		DeleteResult result = collection.deleteOne(Filters.eq(ID_DB, client.getId()));
		
		if (result.getDeletedCount() == 0)
			throw new NoSuchElementException(client.toString() + " to delete is not in database.");
	}
}
