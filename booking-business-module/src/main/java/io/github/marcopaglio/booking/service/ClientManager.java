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
	 * @param firstName the first name of the client to retrieve
	 * @param lastName the last name of the client to retrieve
	 */
	public Client findClientNamed(String firstName, String lastName);

	/*
	 * Saves a new client in the repository.
	 * Note: eventual reservations of the new client will no be saved.
	 * @param client the new client to insert
	 */
	// TODO: Il Client restituito serve?
	public Client insertNewClient(Client client);

	/*
	 * Deletes the client with the specific firstName and lastName 
	 * and all his reservations.
	 * @param firstName the first name of the client to remove
	 * @param lastName the last name of the client to remove
	 */
	public void removeClientNamed(String firstName, String lastName);
}
