package io.github.marcopaglio.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;

/**
 * Facade of repository layer for Client entities.
 */
public interface ClientRepository {

	/**
	 * Retrieves all the clients from the database in a list.
	 * 
	 * @return	the list of clients found in the repository.
	 */
	public List<Client> findAll();

	/**
	 * Retrieves the unique client with the specified name and surname from the database if exists.
	 * 
	 * @param firstName	the name of the client to find.
	 * @param lastName	the surname of the client to find.
	 * @return			an {@code Optional} contained the {@code Client}
	 * 					named {@code firstName} and {@code lastName} if exists;
	 * 					an {@code Optional} empty if it doesn't exist.
	 */
	public Optional<Client> findByName(String firstName, String lastName);

	/**
	 * Retrieves the unique client with the specified identifier from the database if exists.
	 * 
	 * @param id	the identifier of the client to find.
	 * @return		an {@code Optional} contained the {@code Client}
	 * 				named {@code firstName} and {@code lastName} if exists;
	 * 				an {@code Optional} empty if it doesn't exist.
	 */
	public Optional<Client> findById(UUID id);

	/**
	 * Insert a new Client in the database or saves changes of an existing one.
	 *
	 * @param client							the client to save.
	 * @return									the {@code Client} saved.
	 * @throws IllegalArgumentException			if {@code client} is null
	 * 											or a not-null constraint is violated in the database.
	 * @throws InstanceAlreadyExistsException	if a uniqueness constraint is violated in the database.
	 */
	public Client save(Client client) throws IllegalArgumentException, InstanceAlreadyExistsException;

	/**
	 * Removes the unique client with specified name and surname from the database.
	 *
	 * @param client						the client to delete.
	 * @throws IllegalArgumentException		if {@code client} is null.
	 */
	public void delete(Client client);
}
