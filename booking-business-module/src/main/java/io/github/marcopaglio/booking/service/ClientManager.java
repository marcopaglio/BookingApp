package io.github.marcopaglio.booking.service;

import java.util.List;

import io.github.marcopaglio.booking.model.Client;

/*
 * This interface provides methods for operating on Client repository.
 */
public interface ClientManager {

	/*
	 * Retrieves all the saved clients.
	 */
	public List<Client> findAllClients();

	/*
	 * Retrieves the client with the specific firstName and lastName.
	 */
	public Client findClientNamed(String firstName, String lastName);

	/*
	 * Saves a new client in the repository.
	 */
	public void insertNewClient(Client client);

	/*
	 * Deletes the client with the specific firstName and lastName 
	 * and all his reservations.
	 */
	public void removeClientNamed(String firstName, String lastName);
}
