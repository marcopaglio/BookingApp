package io.github.marcopaglio.booking.repository.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;

/**
 * Implementation of repository layer through PostgreSQL for Client entities of the booking application.
 */
public class ClientPostgresRepository implements ClientRepository {
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ClientPostgresRepository.class);

	/**
	 * Entity Manager used to communicate with JPA provider.
	 */
	private EntityManager em;

	/**
	 * Constructs a repository layer for Client entities using PostgreSQL database. 
	 * 
	 * @param em	the {@code EntityManager} used to communicate with PostgreSQL database.
	 */
	public ClientPostgresRepository(EntityManager em) {
		this.em = em;
	}

	/**
	 * Retrieves all the clients from the PostgreSQL database in a list.
	 * 
	 * @return	the {@code List} of {@code Client}s found in the repository.
	 */
	@Override
	public List<Client> findAll() {
		return em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
	}

	/**
	 * Retrieves the unique client with the specified identifier from the PostgreSQL database,
	 * if it exists.
	 * 
	 * @param id	the identifier of the client to find.
	 * @return		an {@code Optional} contained the {@code Client} identified by {@code id},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Client> findById(UUID id) {
		if (id != null) {
			Client client = em.find(Client.class, id);
			
			if (client != null)
				return Optional.of(client);
		}
		return Optional.empty();
	}

	/**
	 * Retrieves the unique client with the specified name and surname from the PostgreSQL database,
	 * if it exists.
	 * 
	 * @param firstName	the name of the client to find.
	 * @param lastName	the surname of the client to find.
	 * @return			an {@code Optional} contained the {@code Client}
	 * 					named {@code firstName} and {@code lastName},
	 * 					if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Client> findByName(String firstName, String lastName) {
		try {
			Client client = em.createQuery(
					"SELECT c FROM Client c WHERE c.firstName = :firstName AND c.lastName = :lastName",
					Client.class)
				.setParameter("firstName", firstName)
				.setParameter("lastName", lastName)
				.getSingleResult();
			return Optional.of(client);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	/**
	 * Inserts a new Client in the PostgreSQL database or saves changes of an existing one.
	 * Note: a Client without an identifier is considered to be entered,
	 * while with the identifier it will be updated.
	 * Note: this method must be executed as part of a transaction.
	 *
	 * @param client									the Client to save.
	 * @return											the {@code Client} saved.
	 * @throws IllegalArgumentException					if {@code client} is null.
	 * @throws UpdateFailureException					if you try to save changes of a no longer
	 * 													existing client.
	 * @throws NotNullConstraintViolationException		if {@code firstName} or {@code lastName}
	 * 													of {@code client} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code [firstName, lastName]}
	 * 													of {@code client} to save are already present.
	 */
	@Override
	public Client save(Client client) throws IllegalArgumentException, UpdateFailureException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		
		try {
			if (client.getId() == null)
				em.persist(client);
			else
				client = mergeIfNotTransient(client);
			em.flush();
		} catch(PropertyValueException e) {
			LOGGER.warn(e.getMessage());
			throw new NotNullConstraintViolationException(
					"Client to save violates not-null constraints.", e.getCause());
		} catch(ConstraintViolationException e) {
			LOGGER.warn(e.getMessage());
			throw new UniquenessConstraintViolationException(
					"Client to save violates uniqueness constraints.", e.getCause());
		}
		return client;
	}

	/**
	 * Merge the existing Client with the same id in the PostgreSQL database.
	 * Note: this method must be executed as part of a transaction.
	 * 
	 * @param client					the replacement client.
	 * @throws UpdateFailureException	if there is no client with the same id to merge.
	 */
	private Client mergeIfNotTransient(Client client) throws UpdateFailureException {
		try {
			em.getReference(Client.class, client.getId());
			return em.merge(client);
		} catch(EntityNotFoundException e) {
			LOGGER.warn(e.getMessage());
			throw new UpdateFailureException(
					"Client to update is not longer present in the repository.",
					e.getCause());
		}
	}

	/**
	 * Removes the unique specified client from the PostgreSQL database, if it exists,
	 * otherwise it does nothing.
	 * Note: this method must be executed as part of a transaction.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code client} is null.
	 */
	@Override
	public void delete(Client client) throws IllegalArgumentException {
		if (client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
		
		UUID id = client.getId();
		if (id != null) {
			try {
				em.remove(em.getReference(Client.class, id));
			} catch(EntityNotFoundException e) {
				LOGGER.warn(e.getMessage());
			}
		} else
			LOGGER.warn(() -> client.toString() + " to delete was never been "
					+ "inserted into the database.");
	}
}