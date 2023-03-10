package io.github.marcopaglio.booking.repository;

import java.util.List;

import io.github.marcopaglio.booking.model.Client;

/*
 * Facade for repository layer for Client entity.
 */
public interface ClientRepository {
	// TODO: Valutare se i sequenti metodi vengono utilizzati e se sono sufficienti o ne servono altri.

	/*
	 * Retrieve all Clients from the repository in a List.
	 */
	public List<Client> findAll();

	/*
	 * Retrieve the unique Client with given names.
	 */
	public Client findByName(String firstName, String lastName);

	/*
	 * Insert a new Client in the repository.
	 */
	public void save(Client client);

	/*
	 * Remove the unique Client with given names from the repository.
	 */
	public void delete(String firstName, String lastName);
}
