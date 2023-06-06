package io.github.marcopaglio.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	 * Insert a new reservation in the database.
	 * Note: reservation entities are immutable,
	 * so this method cannot be used for saving changes of existing reservations.
	 *
	 * @param reservation	the reservation to save.
	 * @return				the {@code Reservation} saved.
	 */
	public Reservation save(Reservation reservation);

	/**
	 * Removes the unique reservation of the specified date from the database.
	 *
	 * @param date	the date of the reservation to delete.
	 */
	public void delete(LocalDate date);
}
