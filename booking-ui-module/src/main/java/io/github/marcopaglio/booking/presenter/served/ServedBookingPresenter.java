package io.github.marcopaglio.booking.presenter.served;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.BookingPresenter;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
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
		LOGGER.info("All clients have been retrieved with success.");
	}

	/**
	 * Finds all the existing reservations in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allReservations() {
		view.showAllReservations(bookingService.findAllReservations());
		LOGGER.info("All reservations have been retrieved with success.");
	}

	/**
	 * Finds all the existing entities in the repository through the service layer and
	 * gives the lists to the view for showing them.
	 */
	private void updateAll() {
		allReservations();
		allClients();
	}

	/**
	 * Creates and inserts a new client in the repository and notifies the view
	 * about the changes. This method delegates the inserting to the service layer.
	 * 
	 * @param firstName					the name of the client to add.
	 * @param lastName					the surname of the client to add.
	 * @return							the {@code Client} added to the repository or the existing one.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	@Override
	public Client addClient(String firstName, String lastName) throws IllegalArgumentException {
		Client client = createClient(firstName, lastName);
		
		Client clientInDB = null;
		do {
			
			try {
				clientInDB = bookingService.insertNewClient(client);
				view.clientAdded(clientInDB);
				LOGGER.info(() -> String.format("%s has been added with success.", client.toString()));
			} catch(InstanceAlreadyExistsException e) {
				LOGGER.warn(e.getMessage());
				try {
					clientInDB = bookingService.findClientNamed(firstName, lastName);
					updateAll();
				} catch (NoSuchElementException e1) {
					LOGGER.warn(e1.getMessage());
					clientInDB = null;
				}
			}
		} while (clientInDB == null);
		return clientInDB;
	}

	/**
	 * Manages the creation of a client object. If the creation goes wrong,
	 * the method notifies the view before throwing an exception.
	 * 
	 * @param firstName					the name of the client to create.
	 * @param lastName					the surname of the client to create.
	 * @return							the {@code Client} created.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	private Client createClient(String firstName, String lastName) throws IllegalArgumentException {
		try {
			return ClientValidator.newValidatedClient(firstName, lastName);
		} catch(IllegalArgumentException e) {
			view.showFormError("Client's name or surname is not valid.");
			throw new IllegalArgumentException(e.getMessage());
			// nella view non si lancia eccezioni, ma si logga il messaggio di errore delle entità
		}
	}

	/**
	 * Creates and inserts a new reservation in the repository and notifies the view
	 * about the changes. This method delegates the inserting to the service layer.
	 * 
	 * @param date						a {@code String} contained the date of the reservation to add.
	 * @param client					the {@code Client} associated to the reservation to add.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	@Override
	public void addReservation(String date, Client client) throws IllegalArgumentException {
		Reservation reservation = createReservation(date, client);
		
		try {
			bookingService.insertNewReservation(reservation);
			view.reservationAdded(reservation);
			LOGGER.info(() -> String.format("%s has been added with success.", reservation.toString()));
		} catch(InstanceAlreadyExistsException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation, " has already been booked.");
			updateAll();
		} catch(NoSuchElementException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation, "'s client has been deleted.");
			updateAll();
		}
	}

	/**
	 * Manages the creation of a reservation object. If the creation goes wrong,
	 * the method notifies the view before throwing an exception.
	 * 
	 * @param client					the associated client of the reservation to create.
	 * @param date						the date of the reservation to create.
	 * @return							the {@code Reservation} created.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 */
	private Reservation createReservation(String date, Client client) throws IllegalArgumentException {
		if (client == null)
			throw new IllegalArgumentException("Reservation's client to add cannot be null.");
		
		try {
			return ReservationValidator.newValidatedReservation(client, date);
		} catch(IllegalArgumentException e) {
			view.showFormError("Date of reservation is not valid.");
			throw new IllegalArgumentException(e.getMessage());
			// nella view non si lancia eccezioni, ma si logga il messaggio di errore delle entità
		}
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
			view.clientRemoved(client);
			LOGGER.info(() -> String.format("%s has been deleted with success.", client.toString()));
		} catch (NoSuchElementException e) {
			LOGGER.warn(e.getMessage());
			view.showClientError(client, " has already been deleted.");
			updateAll();
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
			view.reservationRemoved(reservation);
			LOGGER.info(() -> String.format("%s has been deleted with success.", reservation.toString()));
		} catch (NoSuchElementException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation, " has already been deleted.");
			updateAll();
		}
	}
}
