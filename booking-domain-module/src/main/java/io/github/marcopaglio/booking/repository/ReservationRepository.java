package io.github.marcopaglio.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * Facade for repository layer for Reservation entities.
 */
public interface ReservationRepository {

	/**
	 * Retrieves all the reservations from the database in a list.
	 * 
	 * @return	the {@code List} of {@code Reservation}s found in the repository.
	 */
	public List<Reservation> findAll();

	/**
	 * Retrieves all the reservations associated with the specified client's identifier
	 * from the database in a list.
	 * 
	 * @param clientId	the identifier of the associated client.
	 * @return			the {@code List} of {@code Reservation}s associated
	 * 					with {@code clientId} found in the repository.
	 */
	public List<Reservation> findByClient(UUID clientId);

	/**
	 * Retrieves the unique reservation with the specified identifier from the database, if it exists.
	 * 
	 * @param id	the identifier of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} identified by {@code id},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	public Optional<Reservation> findById(UUID id);

	/**
	 * Retrieves the unique reservation of the specified date from the database, if it exists.
	 * 
	 * @param date	the date of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} on {@code date},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	public Optional<Reservation> findByDate(LocalDate date);

	/**
	 * Insert a new reservation in the database or saves changes of an existing one.
	 *
	 * @param reservation								the reservation to save.
	 * @return											the {@code Reservation} saved.
	 * @throws IllegalArgumentException					if {@code reservation} is null.
	 * @throws UpdateFailureException					if the update fails.
	 * @throws NotNullConstraintViolationException		if a not-null constraint is violated.
	 * @throws UniquenessConstraintViolationException	if a uniqueness constraint is violated.
	 */
	public Reservation save(Reservation reservation) throws IllegalArgumentException, UpdateFailureException, NotNullConstraintViolationException, UniquenessConstraintViolationException;

	/**
	 * Removes the unique specified reservation from the database, if it exists,
	 * otherwise it does nothing.
	 *
	 * @param reservation				the reservation to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	public void delete(Reservation reservation) throws IllegalArgumentException;
}