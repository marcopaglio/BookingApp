package io.github.marcopaglio.booking.presenter;

/**
 * A concrete implementation of the presenter for the booking application using a single view
 * and a transactional version of the booking service layer.
 */
public class BookingPresenter implements Presenter {

	/**
	 * Finds all the existing clients in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allClients() {
		// TODO Auto-generated method stub

	}

	/**
	 * Finds all the existing reservations in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allReservations() {
		// TODO Auto-generated method stub

	}

	/**
	 * Creates and inserts a new reservation (and eventually a new client) in the repository and
	 * notifies the view about the changes. This method checks if objects don't already exist before
	 * delegating the inserting to the service layer.
	 * 
	 * @param date		a {@code String} contained the date of the reservation.
	 * @param firstName	the name of the reservation's client.
	 * @param lastName	the surname of the reservation's client.
	 */
	@Override
	public void newReservation(String date, String firstName, String lastName) {
		// TODO Auto-generated method stub

	}

	/**
	 * Removes an existing reservation from the repository and notifies the view about the changes.
	 * This method checks if the reservation on the specified date is already in the repository
	 * before delegates its elimination to the service layer.
	 *
	 * @param date	a {@code String} contained the date of the reservation to delete.
	 */
	@Override
	public void deleteReservation(String date) {
		// TODO Auto-generated method stub

	}

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view about the changes.
	 * This method checks if the client with the specified name and surname is already in the
	 * repository, before delegating its elimination and of all his reservations to the service layer.
	 *
	 * @param firstName	the name of the client to delete.
	 * @param lastName	the surname of the client to delete.
	 */
	@Override
	public void deleteClient(String firstName, String lastName) {
		// TODO Auto-generated method stub

	}

}
