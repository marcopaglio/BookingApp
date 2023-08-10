package io.github.marcopaglio.booking.repository.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Implementation of repository layer through PostgreSQL for Client entities of the booking application.
 */
public class ClientPostgresRepository implements ClientRepository {

	/**
	 * Entity Manager used to communicate with Hibernate provider.
	 */
	private EntityManager em;

	public ClientPostgresRepository(EntityManager em) {
		this.em = em;
	}

	@Override
	public List<Client> findAll() {
		return em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
	}

	@Override
	public Optional<Client> findById(UUID id) {
		if (id != null) {
			Client client = em.find(Client.class, id);
			
			if (client != null)
				return Optional.of(client);
		}
		return Optional.empty();
	}

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

	@Override
	public Client save(Client client) throws IllegalArgumentException, NotNullConstraintViolationException,
			UniquenessConstraintViolationException {
		if (client == null)
			throw new IllegalArgumentException("Client to save cannot be null.");
		return null;
	}

	@Override
	public void delete(Client client) throws IllegalArgumentException {
		if (client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
	}

}
