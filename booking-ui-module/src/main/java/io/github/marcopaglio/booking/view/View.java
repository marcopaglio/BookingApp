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
	 * Retrieves the first name of the selected client on the view.
	 * @return	a {@code String} contained the {@code firstName} of the selected client.
	 */
	public String getNameOfSelectedClient();

	/**
	 * Retrieves the last name of the selected client on the view.
	 * @return	a {@code String} contained the {@code lastName} of the selected client.
	 */
	public String getSurnameOfSelectedClient();

	/**
	 * Retrieves the date of the selected reservation on the view.
	 * @return	the {@code LocalDate} of the selected reservation.
	 */
	public String getDateOfSelectedReservation();

	/**
	 * Displays the new reservation inserted into the repository on the user interface.
	 * @param reservation	the {@code Reservation} to show.
	 */
	public void reservationAdded(Reservation reservation);

	/**
	 * Displays the new client inserted into the repository on the user interface.
	 * @param client	the {@code Client} to show.
	 */
	public void clientAdded(Client client);

	/**
	 * Makes the deleted reservation disappear from the user interface.
	 * @param reservation	the {@code Reservation} to remove from the view.
	 */
	public void reservationRemoved(Reservation reservation);

	/**
	 * Makes the deleted client disappear from the user interface.
	 * @param client	the {@code Client} to remove from the view.
	 */
	public void clientRemoved(Client client);

	/**
	 * Displays an error message that involves a reservation.
	 * @param message		the message to show.
	 * @param reservation	the reservation involved in the error.
	 */
	public void showReservationError(String message, Reservation reservation);

	/**
	 * Displays an error message that involves a client.
	 * @param message	the message to show.
	 * @param client	the client involved in the error.
	 */
	public void showClientError(String message, Client client);
}
