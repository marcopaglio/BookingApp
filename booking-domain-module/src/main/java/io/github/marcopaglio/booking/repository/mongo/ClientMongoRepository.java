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

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;

import static io.github.marcopaglio.booking.model.Entity.ID_DB;
import static io.github.marcopaglio.booking.model.Client.FIRSTNAME_DB;
import static io.github.marcopaglio.booking.model.Client.LASTNAME_DB;

/**
 * Implementation of repository layer through MongoDB for client entities of the booking application.
 */
public class ClientMongoRepository extends MongoRepository<Client> implements ClientRepository {
	public static final String BOOKING_DB_NAME = "booking_db";
	public static final String CLIENT_COLLECTION_NAME = "booking_client";

	public ClientMongoRepository(MongoClient mongoClient) {
		super(mongoClient
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(CLIENT_COLLECTION_NAME, Client.class));
		
		// collection configuration
		getCollection().createIndex(Indexes.descending("name", "surname"), 
				new IndexOptions().unique(true));
	}

	@Override
	public List<Client> findAll() {
		return StreamSupport
				.stream(getCollection().find().spliterator(), false)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<Client> findByName(String firstName, String lastName) {
		Client client = getCollection().find(Filters.and(
					Filters.eq(FIRSTNAME_DB, firstName),
					Filters.eq(LASTNAME_DB, lastName)
				)).first();
		
		if (client != null)
			return Optional.of(client);
		return Optional.empty();
	}

	@Override
	public Optional<Client> findById(UUID uuid) {
		Client client = getCollection().find(Filters.eq(ID_DB, uuid)).first();
		
		if (client != null)
			return Optional.of(client);
		return Optional.empty();
	}

	@Override
	public Client save(Client client) throws IllegalArgumentException, InstanceAlreadyExistsException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		
		if (client.getFirstName() == null || client.getLastName() == null)
			throw new IllegalArgumentException("Client to save cannot have a null name.");
		
		if(client.getId() == null)
			client.setId(UUID.randomUUID());
		
		try {
			getCollection().insertOne(client);
		} catch(MongoWriteException e) {
			throw new InstanceAlreadyExistsException("The insertion violates uniqueness constraints.");
		}
		return client;
	}

	@Override
	public void delete(Client client) throws IllegalArgumentException {
		if(client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
		
		if (client.getId() != null)
			getCollection().deleteOne(Filters.eq(ID_DB, client.getId()));
		else
			getCollection().deleteOne(Filters.and(
					Filters.eq(FIRSTNAME_DB, client.getFirstName()),
					Filters.eq(LASTNAME_DB, client.getLastName())));
	}
}
