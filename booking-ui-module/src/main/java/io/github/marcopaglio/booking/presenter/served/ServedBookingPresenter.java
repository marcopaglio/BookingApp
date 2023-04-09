package io.github.marcopaglio.booking.presenter.served;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.BookingPresenter;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.view.View;

/**
 * A concrete implementation of the presenter for the booking application using
 * a single view and delegating operations on repositories to a service layer.
 */
public class ServedBookingPresenter implements BookingPresenter {
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ServedBookingPresenter.class);

	/**
	 * Displays changes of the model on a user interface.
	 */
	private View view;

	/**
	 * Allows the presenter to interact with repositories.
	 */
	private BookingService bookingService;

	/**
	 * Constructs a presenter for the booking application with a view and a service.
	 * 
	 * @param view				the {@code View} used to show the user interface.
	 * @param bookingService	the {@code BookingService} used to interact with repositories.
	 */
	public ServedBookingPresenter(View view, BookingService bookingService) {
		this.view = view;
		this.bookingService = bookingService;
	}

	/**
	 * Finds all the existing clients in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allClients() {
		view.showAllClients(bookingService.findAllClients());
	}

	/**
	 * Finds all the existing reservations in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allReservations() {
		view.showAllReservations(bookingService.findAllReservations());
	}

	/**
	 * Creates and inserts a new reservation (and eventually a new client) in the repository and
	 * notifies the view about the changes. This method checks if objects don't already exist before
	 * delegating the inserting to the service layer.
	 * 
	 * @param date		a {@code String} contained the date of the reservation to add.
	 * @param firstName	the name of the reservation's client to add.
	 * @param lastName	the surname of the reservation's client to add.
	 */
	@Override
	public void addReservation(String date, String firstName, String lastName) {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to add cannot be null.");
		if (firstName == null)
			throw new IllegalArgumentException("Client's name of reservation to add cannot be null.");
		if (lastName == null)
			throw new IllegalArgumentException("Client's surname of reservation to add cannot be null.");
		
		Client client;
		try {
			client = bookingService.findClientNamed(firstName, lastName);
		} catch (NoSuchElementException e) {
			client = new Client(firstName, lastName, new ArrayList<>());
			bookingService.insertNewClient(client);
			view.clientAdded(client);
		}
		
		Reservation reservation = new Reservation(client, date);
		try {
			bookingService.insertNewReservation(reservation);
			view.reservationAdded(reservation);
		} catch (InstanceAlreadyExistsException e) {
			view.showReservationError(reservation, " has already been booked.");
			allClients();
			allReservations();
		}
	}

	/**
	 * Removes an existing reservation from the repository and notifies the view about the changes.
	 * This method delegates its elimination to the service layer.
	 *
	 * @param reservation				the {@code Reservation} to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	@Override
	public void deleteReservation(Reservation reservation) throws IllegalArgumentException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete cannot be null.");
		
		try {
			bookingService.removeReservationOn(reservation.getDate());
		} catch (NoSuchElementException e) {
			view.showReservationError(reservation, " has already been eliminated.");
			logOnConsole(e);
		}
		view.reservationRemoved(reservation);
	}

	/**
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view about the changes.
	 * This method delegates its elimination and of all his reservations to the service layer.
	 *
	 * @param client					the client to delete.
	 * @throws IllegalArgumentException	if {@code client} is null.
	 */
	@Override
	public void deleteClient(Client client) throws IllegalArgumentException {
		if (client == null)
			throw new IllegalArgumentException("Client to delete cannot be null.");
		
		try {
			bookingService.removeClientNamed(client.getFirstName(), client.getLastName());
		} catch (NoSuchElementException e) {
			view.showClientError(client, " has already been eliminated.");
			logOnConsole(e);
		}
		view.clientRemoved(client);
	}

	/**
	 * Used for generating the right level of information to display on the console.
	 * 
	 * @param e	the exception containing logging informations.
	 */
	private void logOnConsole(RuntimeException e) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug(e.getMessage(), e);
		else
			LOGGER.warn(e.getMessage());
	}
}
