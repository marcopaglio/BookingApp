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
	 * Creates and inserts a new client in the repository and notifies the view(s)
	 * about the changes.
	 * 
	 * @param firstName					the name of the client to add.
	 * @param lastName					the surname of the client to add.
	 * @return							the {@code Client} added to the repository.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	public Client addClient(String firstName, String lastName) throws IllegalArgumentException;

	/**
	 * Creates and inserts a new reservation in the repository and notifies the view(s)
	 * about the changes.
	 * 
	 * @param date						a {@code String} contained the date of the reservation to add.
	 * @param client					the {@code Client} associated to the reservation to add.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	public void addReservation(String date, Client client);

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view(s) about the changes.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	public void deleteClient(Client client) throws IllegalArgumentException;

	/**
	 * Removes an existing reservation from the repository and notifies the view(s) about the changes.
	 *
	 * @param reservation				the {@code Reservation} to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	public void deleteReservation(Reservation reservation) throws IllegalArgumentException;

}
