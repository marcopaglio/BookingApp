package io.github.marcopaglio.booking.repository.mongo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;

/**
 * @author marco
 *
 */
public class ClientMongoRepository implements ClientRepository {

	public static final String BOOKING_DB_NAME = "booking_db";
	public static final String CLIENT_COLLECTION_NAME = "booking_client";

	private MongoCollection<Client> clientCollection;

	public ClientMongoRepository(MongoClient client) {
		clientCollection = client
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(CLIENT_COLLECTION_NAME, Client.class);
	}

	@Override
	public List<Client> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Client> findByName(String firstName, String lastName) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Client> findById(UUID uuid) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Client save(Client client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String firstName, String lastName) {
		// TODO Auto-generated method stub

	}

}
