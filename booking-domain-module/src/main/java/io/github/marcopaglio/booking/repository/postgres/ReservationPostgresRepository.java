package io.github.marcopaglio.booking.repository.postgres;

import java.time.LocalDate;
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
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;

/**
 * Implementation of repository layer through PostgreSQL for Reservation entities of the booking application.
 */
public class ReservationPostgresRepository implements ReservationRepository {
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ReservationPostgresRepository.class);

	/**
	 * Entity Manager used to communicate with JPA provider.
	 */
	private EntityManager em;

	/**
	 * Constructs a repository layer for Reservation entities using PostgreSQL database. 
	 * 
	 * @param em	the {@code EntityManager} used to communicate with PostgreSQL database.
	 */
	public ReservationPostgresRepository(EntityManager em) {
		this.em = em;
	}

	/**
	 * Retrieves all the reservations from the PostgreSQL database in a list.
	 * 
	 * @return	the {@code List} of {@code Reservation}s found in the repository.
	 */
	@Override
	public List<Reservation> findAll() {
		return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
	}

	/**
	 * Retrieves all the reservations associated with the specified client's identifier
	 * from the PostgreSQL database in a list.
	 * 
	 * @param clientId	the identifier of the associated client.
	 * @return			the {@code List} of {@code Reservation}s associated
	 * 					with {@code clientId} found in the repository.
	 */
	@Override
	public List<Reservation> findByClient(UUID clientId) {
		return em.createQuery(
				"SELECT r FROM Reservation r WHERE r.clientId = :clientId",
				Reservation.class)
			.setParameter("clientId", clientId)
			.getResultList();
	}

	/**
	 * Retrieves the unique reservation with the specified identifier from the PostgreSQL database,
	 * if it exists.
	 * 
	 * @param id	the identifier of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} identified by {@code id},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Reservation> findById(UUID id) {
		if (id != null) {
			Reservation reservation = em.find(Reservation.class, id);
			
			if (reservation != null)
				return Optional.of(reservation);
		}
		return Optional.empty();
	}

	/**
	 * Retrieves the unique reservation of the specified date from the PostgreSQL database,
	 * if it exists.
	 * 
	 * @param date	the date of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} on {@code date},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Reservation> findByDate(LocalDate date) {
		try {
			Reservation reservation = em.createQuery(
					"SELECT r FROM Reservation r WHERE r.date = :date",
					Reservation.class)
				.setParameter("date", date)
				.getSingleResult();
			return Optional.of(reservation);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	/**
	 * Insert a new reservation in the PostgreSQL database or saves changes of an existing one.
	 * Note: a Reservation without an identifier is considered to be entered,
	 * while with the identifier it will be updated.
	 *
	 * @param reservation								the reservation to save.
	 * @return											the {@code Reservation} saved.
	 * @throws IllegalArgumentException					if {@code reservation} is null.
	 * @throws UpdateFailureException					if you try to save changes of a no longer
	 * 													existing reservation.
	 * @throws NotNullConstraintViolationException		if {@code date} or {@code clientId}
	 * 													of {@code reservation} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code date}
	 * 													of {@code reservation} to save are already present.
	 */
	@Override
	public Reservation save(Reservation reservation) throws IllegalArgumentException, UpdateFailureException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to save cannot be null.");
		
		try {
			if (reservation.getId() == null)
				em.persist(reservation);
			else
				reservation = mergeIfNotTransient(reservation);
			em.flush();
		} catch(PropertyValueException e) {
			LOGGER.warn(e.getMessage());
			throw new NotNullConstraintViolationException(
					"Reservation to save violates not-null constraints.", e.getCause());
		} catch(ConstraintViolationException e) {
			LOGGER.warn(e.getMessage());
			throw new UniquenessConstraintViolationException(
					"Reservation to save violates uniqueness constraints.", e.getCause());
		}
		return reservation;
	}

	/**
	 * Merge the existing Reservation with the same id in the PostgreSQL database.
	 * Note: this method must be executed as part of a transaction.
	 * 
	 * @param reservation				the replacement reservation.
	 * @throws UpdateFailureException	if there is no reservation with the same id to merge.
	 */
	private Reservation mergeIfNotTransient(Reservation reservation) throws UpdateFailureException {
		try {
			em.getReference(Reservation.class, reservation.getId());
			return em.merge(reservation);
		} catch(EntityNotFoundException e) {
			LOGGER.warn(e.getMessage());
			throw new UpdateFailureException(
					"Reservation to update is not longer present in the repository.",
					e.getCause());
		}
	}

	/**
	 * Removes the unique specified reservation from the PostgreSQL database, if it exists,
	 * otherwise it does nothing.
	 * Note: this method must be executed as part of a transaction.
	 *
	 * @param reservation				the reservation to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	@Override
	public void delete(Reservation reservation) throws IllegalArgumentException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete cannot be null.");
		
		UUID id = reservation.getId();
		if (id != null) {
			try {
				em.remove(em.getReference(Reservation.class, id));
			} catch(EntityNotFoundException e) {
				LOGGER.warn(e.getMessage());
			}
		} else 
			LOGGER.warn(() -> reservation.toString() + " to delete was never been "
					+ "inserted into the database.");
	}
}