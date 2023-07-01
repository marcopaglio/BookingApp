package io.github.marcopaglio.booking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;

/**
 * Facade of repository layer for Client entities.
 */
public interface ClientRepository {

	/**
	 * Retrieves all the clients from the database in a list.
	 * 
	 * @return	the {@code List} of {@code Client}s found in the repository.
	 */
	public List<Client> findAll();

	/**
	 * Retrieves the unique client with the specified identifier from the database if exists.
	 * 
	 * @param id						the identifier of the client to find.
	 * @return							an {@code Optional} contained the {@code Client}
	 * 									named {@code id} if exists;
	 * 									an {@code Optional} empty if it doesn't exist.
	 */
	public Optional<Client> findById(UUID id);

	/**
	 * Retrieves the unique client with the specified name and surname from the database if exists.
	 * 
	 * @param firstName					the name of the client to find.
	 * @param lastName					the surname of the client to find.
	 * @return							an {@code Optional} contained the {@code Client}
	 * 									named {@code firstName} and {@code lastName} if exists;
	 * 									an {@code Optional} empty if it doesn't exist.
	 */
	public Optional<Client> findByName(String firstName, String lastName);

	/**
	 * Inserts a new Client in the database or saves changes of an existing one.
	 *
	 * @param client									the client to save.
	 * @return											the {@code Client} saved.
	 * @throws IllegalArgumentException					if {@code client} is null.
	 * @throws NotNullConstraintViolationException		if a not-null constraint is violated.
	 * @throws UniquenessConstraintViolationException	if a uniqueness constraint is violated.
	 */
	public Client save(Client client) throws IllegalArgumentException, NotNullConstraintViolationException, UniquenessConstraintViolationException;

	/**
	 * Removes the unique specified client from the database.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code client} is null.
	 */
	public void delete(Client client) throws IllegalArgumentException;
}
