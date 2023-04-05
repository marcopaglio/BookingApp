package io.github.marcopaglio.booking.presenter;

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
	 * @param date		a {@code String} contained the date of the reservation.
	 * @param firstName	the name of the reservation's client.
	 * @param lastName	the surname of the reservation's client.
	 */
	public void newReservation(String date, String firstName, String lastName);

	/**
	 * Removes an existing reservation from the repository and notifies the view(s) about the changes.
	 *
	 * @param date	a {@code String} contained the date of the reservation to delete.
	 */
	public void deleteReservation(String date);

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view(s) about the changes.
	 *
	 * @param firstName	the name of the client to delete.
	 * @param lastName	the surname of the client to delete.
	 */
	public void deleteClient(String firstName, String lastName);
}
