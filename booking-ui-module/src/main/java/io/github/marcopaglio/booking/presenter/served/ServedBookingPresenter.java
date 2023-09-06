package io.github.marcopaglio.booking.presenter.served;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcopaglio.booking.exception.DatabaseException;
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
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
	 * Validates inputs for creating Client entities.
	 */
	private ClientValidator clientValidator;

	/**
	 * Validates inputs for creating Reservation entities.
	 */
	private ReservationValidator reservationValidator;

	/**
	 * Constructs a presenter for the booking application with a view and a service.
	 * 
	 * @param view				the {@code View} used to show the user interface.
	 * @param bookingService	the {@code BookingService} used to interact with repositories.
	 */
	public ServedBookingPresenter(View view, BookingService bookingService,
			ClientValidator clientValidator, ReservationValidator reservationValidator) {
		this.view = view;
		this.bookingService = bookingService;
		this.clientValidator = clientValidator;
		this.reservationValidator = reservationValidator;
	}

	/**
	 * Finds all the existing clients in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allClients() {
		try {
			view.showAllClients(bookingService.findAllClients());
			LOGGER.info("All clients have been retrieved with success.");
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showClientError("An error occurred while updating clients.");
		}
	}

	/**
	 * Finds all the existing reservations in the repository through the service layer and
	 * gives the list to the view for showing them.
	 */
	@Override
	public void allReservations() {
		try {
		view.showAllReservations(bookingService.findAllReservations());
		LOGGER.info("All reservations have been retrieved with success.");
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError("An error occurred while updating reservations.");
		}
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
	 */
	@Override
	public void addClient(String firstName, String lastName) {
		Client client = createClient(firstName, lastName);
		
		if (client != null) {
			try {
				Client clientInDB = bookingService.insertNewClient(client);
				view.clientAdded(clientInDB);
				LOGGER.info(() -> String.format("%s has been added with success.", clientInDB.toString()));
			} catch(InstanceAlreadyExistsException e) {
				LOGGER.warn(e.getMessage());
				updateAll();
			} catch(DatabaseException e) {
				LOGGER.warn(e.getMessage());
				view.showClientError("An error occurred while adding " + client.toString() + ".");
			}
		}
	}

	/**
	 * Manages the creation of a client object. If the creation goes wrong,
	 * the method notifies the view before returning a null object.
	 * 
	 * @param firstName					the name of the client to create.
	 * @param lastName					the surname of the client to create.
	 * @return							the {@code Client} created.
	 */
	private Client createClient(String firstName, String lastName) {
		try {
			return new Client(
					clientValidator.validateFirstName(firstName),
					clientValidator.validateLastName(lastName));
		} catch(IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			view.showFormError("Client's name or surname is not valid.");
			return null;
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
			Reservation reservationInDB = bookingService.insertNewReservation(reservation);
			view.reservationAdded(reservationInDB);
			LOGGER.info(() -> String.format("%s has been added with success.", reservationInDB.toString()));
		} catch(InstanceAlreadyExistsException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation.toString() + " has already been booked.");
			updateAll();
		} catch(InstanceNotFoundException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation.toString() + "'s client has been deleted.");
			updateAll();
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError("An error occurred while adding " + reservation.toString() + ".");
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
			return new Reservation(
					reservationValidator.validateClientId(client.getId()),
					reservationValidator.validateDate(date));
		} catch(IllegalArgumentException e) {
			view.showFormError("Date of reservation is not valid.");
			throw e;
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
		} catch (InstanceNotFoundException e) {
			LOGGER.warn(e.getMessage());
			view.showClientError(client.toString() + " has already been deleted.");
			updateAll();
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showClientError("An error occurred while deleting " + client.toString() + ".");
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
		} catch (InstanceNotFoundException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError(reservation.toString() + " has already been deleted.");
			updateAll();
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showReservationError("An error occurred while deleting " + reservation.toString() + ".");
		}
	}
}
