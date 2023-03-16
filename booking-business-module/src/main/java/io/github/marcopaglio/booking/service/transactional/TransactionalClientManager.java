package io.github.marcopaglio.booking.service.transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.service.ClientManager;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/*
 * Implements methods for operating on Client repository using transactions.
 */
public class TransactionalClientManager implements ClientManager {
	private TransactionManager transactionManager;

	public TransactionalClientManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * It is used to retrieve all the clients saved in the database
	 * within a transaction.
	 */
	@Override
	public List<Client> findAllClients() {
		return transactionManager.doInTransaction(ClientRepository::findAll);
	}

	/*
	 * It is used to retrieve a client with specified name and surname
	 * from the database within a transaction.
	 * @throws NoSuchElementException if that client is not in database.
	 */
	@Override
	public Client findClientNamed(String firstName, String lastName) {
		Optional<Client> possibleClient = transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> clientRepository.findByName(firstName, lastName));
		if (possibleClient.isPresent())
			return possibleClient.get();
		else
			throw new NoSuchElementException(
				"Client named \"" + firstName + " " + lastName + "\" is not present in the database.");
	}

	/*
	 * It is used to add a new client in the database within a transaction.
	 * This method checks if the client is already present in the database
	 * before inserting.
	 * @throws IllegalArgumentException if client is null.
	 * @throws InstanceAlreadyExistsException if client is already in database.
	 */
	@Override
	public void insertNewClient(Client client) {
		if (client == null)
			throw new IllegalArgumentException("Client to insert cannot be null.");
		transactionManager.doInTransaction(
			(ClientRepository clientRepository) -> {
				Optional<Client> possibleClient = clientRepository
						.findByName(client.getFirstName(), client.getLastName());
				if (possibleClient.isEmpty()) {
					// TODO: catch exception from database
					return clientRepository.save(client);
				}
				throw new InstanceAlreadyExistsException(
					client.toString() + " already exists in the database.");
			});
	}

	@Override
	public void removeClientNamed(String firstName, String lastName) {
		// TODO Auto-generated method stub
		
	}

}
