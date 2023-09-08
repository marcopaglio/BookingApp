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
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view(s) about the changes.
	 *
	 * @param client	the client to delete.
	 */
	public void deleteClient(Client client);

	/**
	 * Removes an existing reservation from the repository and notifies the view(s)
	 * about the changes.
	 *
	 * @param reservation	the reservation to delete.
	 */
	public void deleteReservation(Reservation reservation);

	/**
	 * Creates and inserts a new client in the repository and notifies the view(s)
	 * about the changes.
	 * 
	 * @param firstName	the name of the client to add.
	 * @param lastName	the surname of the client to add.
	 */
	public void addClient(String firstName, String lastName);

	/**
	 * Creates and inserts a new reservation in the repository and notifies the view(s)
	 * about the changes.
	 * 
	 * @param client	the associated client of the reservation to add.
	 * @param date		the date of the reservation to add.
	 */
	public void addReservation(Client client, String date);

	/**
	 * Modifies names of an existing client and notifies the view(s) about the changes.
	 * 
	 * @param client		the client to modify.
	 * @param newFirstName	the new name for the client.
	 * @param newLastName	the new surname for the client.
	 */
	public void renameClient(Client client, String newFirstName, String newLastName);

	/**
	 * Modifies the date of an existing reservation and notifies the view(s) about the changes.
	 * 
	 * @param reservation	the reservation to modify.
	 * @param newDate		the new date for the reservation.
	 */
	public void rescheduleReservation(Reservation reservation, String newDate);
}
