package io.github.marcopaglio.booking.presenter;

import java.util.List;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * This interface provides methods for operating on the booking application and
 * notifying changes to the view(s).
 */
public interface Presenter {

	/**
	 * Retrieves a list of existing clients in the repository and notifies changes to the view.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * This method has to find all the clients in the repository through the service layer and
	 * then gives the list to the view for showing them.
	 * @return	the {@code List} of clients retrieves from the database.
	 */
	public List<Client> getSavedClients();

	/**
	 * Retrieves a list of existing reservations in the repository and notifies changes to the view.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * This method has to find all the reservations in the repository through the service layer and
	 * then gives the list to the view for showing them.
	 * @return	the {@code List} of reservations retrieves from the database.
	 */
	public List<Client> getSavedReservations();

	/**
	 * Inserts a new reservation in the repository (and eventually a new client) and
	 * notifies the view of the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * This method has to retrieves informations of the new reservation from the view,
	 * then creates the reservation if possible and checks if it doesn't already exist.
	 * If all the checks pass, this method delegates the inserting of the reservation
	 * to the service layer, and notifies the view of the new reservation.
	 * @return	the {@code Reservation} added to the database.
	 */
	public Reservation addReservation();

	/**
	 * Deletes an existing reservation from the repository and notifies the view of the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * This method has to retrieve the date of the reservation to delete from the view, checks if
	 * the reservation is not already eliminated through the service layer, then delegates to the service
	 * the elimination of the reservation from the repository. At the end of these operations, the method
	 * notifies the view of the changes.
	 */
	public void removeReservation();

	/**
	 * Deletes an existing client from the repository (and all his reservations) and notifies the 
	 * view of the changes.
	 * 
	 * METTERE SUL METODO CONCRETO:
	 * This method has to recover name and surname from the view for checking if the client yet in the
	 * repository. If the service returns a positive response, then this method delegates to the service
	 * the elimination of the client and all his reservations.
	 * After all, it notifies the view of the changes of the client (clientDeleted)
	 * and all his reservations (in a cycle, reservationDeleted).
	 */
	public void removeClient();
}
