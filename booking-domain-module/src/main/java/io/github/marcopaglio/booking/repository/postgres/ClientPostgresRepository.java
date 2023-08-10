package io.github.marcopaglio.booking.repository.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import jakarta.persistence.EntityManager;

/**
 * Implementation of repository layer through PostgreSQL for Client entities of the booking application.
 */
public class ClientPostgresRepository implements ClientRepository {

	private EntityManager em;

	public ClientPostgresRepository(EntityManager em) {
		this.em = em;
	}

	@Override
	public List<Client> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Client> findById(UUID id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Client> findByName(String firstName, String lastName) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Client save(Client client) throws IllegalArgumentException, NotNullConstraintViolationException,
			UniquenessConstraintViolationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Client client) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

}
