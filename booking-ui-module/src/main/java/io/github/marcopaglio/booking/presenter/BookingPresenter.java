package io.github.marcopaglio.booking.presenter;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * This interface provides methods for operating on the booking application and notifies
 * the view(s) about changes.
 */
public interface BookingPresenter {

	/**
	 * Provides all the existing clients in the repository to the view(s).
	 */
	public void allClients();

	/**
	 * Provides all the existing reservations in the repository to the view(s).
	 */
	public void allReservations();

	/**
	 * Creates and inserts a new reservation (and eventually a new client) in the repository and
	 * notifies the view(s) about the changes.
	 * 
	 * @param date		a {@code String} contained the date of the reservation to add.
	 * @param firstName	the name of the reservation's client to add.
	 * @param lastName	the surname of the reservation's client to add.
	 */
	public void addReservation(String date, String firstName, String lastName);

	/**
	 * Removes an existing reservation from the repository and notifies the view(s) about the changes.
	 *
	 * @param reservation	the {@code Reservation} to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	public void deleteReservation(Reservation reservation) throws IllegalArgumentException;

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view(s) about the changes.
	 *
	 * @param client	the client to delete.
	 */
	public void deleteClient(Client client);
}
