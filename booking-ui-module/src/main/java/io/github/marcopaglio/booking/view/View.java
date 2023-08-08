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
	 * Makes the just deleted client and all his reservations disappear from the user interface.
	 * @param client	the {@code Client} to remove from the view.
	 */
	public void clientRemoved(Client client);

	// I SEGUENTI METODI NON SONO UNIFICABILI IN showError PERCHé MOSTRERANNO IL MESSAGGIO
	// DI ERRORE IN POSIZIONI DIFFERENTI DELLA VISTA
	/**
	 * Displays an error message that involves reservation objects.
	 * @param message		the message to show.
	 */
	public void showReservationError(String message);

	/**
	 * Displays an error message that involves client objects.
	 * @param message	the message to show.
	 */
	public void showClientError(String message);

	/**
	 * Displays an error message that involves form inputs.
	 * @param message	the message to show.
	 */
	public void showFormError(String message);
}
