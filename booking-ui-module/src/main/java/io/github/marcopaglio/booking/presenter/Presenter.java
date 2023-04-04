package io.github.marcopaglio.booking.presenter;

/**
 * This interface provides methods for operating on the booking application and notifies
 * the view(s) about changes.
 */
public interface Presenter {

	/**
	 * Provides all the existing clients in the repository to the view.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * Finds all the clients in the repository through the service layer and
	 * then gives the list to the view for showing them.
	 */
	public void allClients();

	/**
	 * Provides all the existing reservations in the repository to the view.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * Finds all the reservations in the repository through the service layer and
	 * then gives the list to the view for showing them.
	 */
	public void allReservations();

	/**
	 * Creates and inserts a new reservation in the repository (and eventually a new client) and
	 * notifies the view of the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * Creates a reservation object if possible and checks if it doesn't already exist.
	 * After all, this method delegates the inserting of the new reservation
	 * to the service layer and notifies the view about the changes.
	 * 
	 * @param date		a {@code String} contained the date of the reservation.
	 * @param firstName	the name of the reservation's client.
	 * @param lastName	the surname of the reservation's client.
	 */
	public void newReservation(String date, String firstName, String lastName);

	/**
	 * Removes an existing reservation from the repository and notifies the view about the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * Checks if the reservation on the specified date is already in the repository,
	 * then delegates its elimination to the service layer.
	 * After all, the method notifies the view about the changes.
	 *
	 * @param date	a {@code String} contained the date of the reservation to delete.
	 */
	public void deleteReservation(String date);

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view about the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * Checks if the client with the specified name and surname is already in the repository,
	 * then delegates its elimination and of all his reservations to the service layer.
	 * After all, the method notifies the view about the changes.
	 *
	 * @param firstName	the name of the client to delete.
	 * @param lastName	the surname of the client to delete.
	 */
	public void deleteClient(String firstName, String lastName);
}
