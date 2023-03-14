package io.github.marcopaglio.booking.service;

import java.time.LocalDate;
import java.util.List;

import io.github.marcopaglio.booking.model.Reservation;

/*
 * This interface provides methods for operating on Reservation repository.
 */
public interface ReservationManager {

	/*
	 * Retrieves all the saved reservations.
	 */
	public List<Reservation> findAllReservations();

	/*
	 * Retrieves the reservation of the specific date.
	 */
	public Reservation findReservationOn(LocalDate date);

	/*
	 * Saves a new reservation in the repository
	 * and update the client one.
	 */
	public void insertNewReservation(Reservation reservation);

	/*
	 * Deletes the reservation of the specific date.
	 */
	public void removeReservationOn(LocalDate date);
}
