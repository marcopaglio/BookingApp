package io.github.marcopaglio.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * Facade for repository layer for Reservation entities.
 */
public interface ReservationRepository {

	/**
	 * Retrieves all the reservations from the database in a list.
	 * 
	 * @return	the list of reservations found in the repository.
	 */
	public List<Reservation> findAll();

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
	 * Retrieves the unique reservation of the specified date from the database if exists.
	 * 
	 * @param date	the date of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation}
	 * 				on {@code date} if exists;
	 * 				an {@code Optional} empty if it doesn't exist.
	 */
	public Optional<Reservation> findByDate(LocalDate date);

	/**
	 * Retrieves all the reservations associated at the specified client's identifier
	 * from the database in a list.
	 * 
	 * @param clientId	the identifier of the associated client.
	 * @return			the list of reservations found in the repository.
	 */
	public List<Reservation> findByClient(UUID clientId);

	/**
	 * Insert a new reservation in the database or saves changes of an existing one.
	 *
	 * @param reservation								the reservation to save.
	 * @return											the {@code Reservation} saved.
	 * @throws IllegalArgumentException					if {@code reservation} is null.
	 * @throws NotNullConstraintViolationException		if a not-null constraint is violated.
	 * @throws UniquenessConstraintViolationException	if a uniqueness constraint is violated.
	 */
	public Reservation save(Reservation reservation) throws IllegalArgumentException, NotNullConstraintViolationException, UniquenessConstraintViolationException;

	/**
	 * Removes the unique reservation of the specified date from the database.
	 *
	 * @param reservation	the reservation to delete.
	 */
	public void delete(Reservation reservation);
}
