package io.github.marcopaglio.booking.view;

import java.util.List;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * This interface provides methods for operating on a user interface for the booking application.
 */
public interface View {

	/**
	 * Displays the clients of the given list on the user interface.
	 * @param clients	the {@code List} of clients to show.
	 */
	public void showAllClients(List<Client> clients);

	/**
	 * Displays the reservations of the given list on the user interface.
	 * @param reservations	the {@code List} of reservations to show.
	 */
	public void showAllReservations(List<Reservation> reservations);

	/**
	 * Displays the reservation just inserted into the repository on the user interface.
	 * @param reservation	the {@code Reservation} to show.
	 */
	public void reservationAdded(Reservation reservation);

	/**
	 * Displays the client just inserted into the repository on the user interface.
	 * @param client	the {@code Client} to show.
	 */
	public void clientAdded(Client client);

	/**
	 * Makes the just deleted reservation disappear from the user interface.
	 * @param reservation	the {@code Reservation} to remove from the view.
	 */
	public void reservationRemoved(Reservation reservation);

	/**
	 * Makes the just deleted client disappear from the user interface.
	 * @param client	the {@code Client} to remove from the view.
	 */
	public void clientRemoved(Client client);

	/**
	 * Displays an error message that involves a reservation object.
	 * @param message		the message to show.
	 * @param reservation	the reservation involved in the error.
	 */
	public void showReservationError(String message, Reservation reservation);

	/**
	 * Displays an error message that involves a client object.
	 * @param message	the message to show.
	 * @param client	the client involved in the error.
	 */
	public void showClientError(String message, Client client);
}
