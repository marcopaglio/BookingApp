package io.github.marcopaglio.booking.repository.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;

import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Implementation of repository layer through PostgreSQL for Client entities of the booking application.
 */
public class ClientPostgresRepository implements ClientRepository {

	/**
	 * Entity Manager used to communicate with JPA provider.
	 */
	private EntityManager em;

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
		TypedQuery<Client> query = em.createQuery(
				"SELECT c FROM Client c WHERE c.firstName = :firstName AND c.lastName = :lastName",
				Client.class);
		query.setParameter("firstName", firstName);
		query.setParameter("lastName", lastName);
		
		try {
			Client client = query.getSingleResult();
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
			if (client.getId() == null) {
				em.persist(client);
			} else
				mergeIfNotTransient(client);
			em.flush();
		} catch(PropertyValueException e) {
			throw new NotNullConstraintViolationException(
					"Client to save must have both not-null names.");
		} catch(ConstraintViolationException e) {
			throw new UniquenessConstraintViolationException(
					"Client to save violates uniqueness constraints.");
		}
		return client;
	}

	/**
	 * Merge the existing Client with the same id in the PostgreSQL database.
	 * Note: this method must be executed as part of a transaction.
	 * 
	 * @param client					the replacement client.
	 * @throws UpdateFailureException	if there is no client with the same ID to merge.
	 */
	private void mergeIfNotTransient(Client client) throws UpdateFailureException {
		try {
			em.getReference(Client.class, client.getId());
			em.merge(client);
		} catch(EntityNotFoundException e) {
			throw new UpdateFailureException(
					"Client to update is not longer present in the repository.");
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
		
		em.remove(em.contains(client) ? client : em.merge(client));
	}

}
