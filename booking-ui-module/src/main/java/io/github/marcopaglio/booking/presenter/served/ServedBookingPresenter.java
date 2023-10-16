package io.github.marcopaglio.booking.presenter.served;


import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

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
import io.github.marcopaglio.booking.view.BookingView;

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
	private BookingView view;

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
	public ServedBookingPresenter(BookingView view, BookingService bookingService,
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
			view.showOperationError(databaseErrorMsg("updating clients"));
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
			view.showOperationError(databaseErrorMsg("updating reservations"));
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
	 * Removes an existing client and all his reservations from the repository and notifies the 
	 * view about the changes.
	 * This method delegates its elimination and of all his reservations to the service layer.
	 *
	 * @param client	the client to delete.
	 */
	@Override
	public void deleteClient(Client client) {
		if (client == null) {
			LOGGER.warn("Client to delete cannot be null.");
			view.showFormError("Select a client to delete.");
		} else {
			try {
				bookingService.removeClientNamed(client.getFirstName(), client.getLastName());
				allReservations();
				view.clientRemoved(client);
				LOGGER.info(() -> String.format("%s and all his reservations have been deleted with success.", client.toString()));
			} catch (InstanceNotFoundException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(instanceNotFoundErrorMsg(client.toString()));
				updateAll();
			} catch(DatabaseException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(databaseErrorMsg("deleting " + client.toString()));
				updateAll();
			}
		}
	}

	/**
	 * Removes an existing reservation from the repository and notifies the view about the changes.
	 * This method delegates its elimination to the service layer.
	 *
	 * @param reservation	the reservation to delete.
	 */
	@Override
	public void deleteReservation(Reservation reservation) {
		if (reservation == null) {
			LOGGER.warn("Reservation to delete cannot be null.");
			view.showFormError("Select a reservation to delete.");
		} else {
			try {
				bookingService.removeReservationOn(reservation.getDate());
				view.reservationRemoved(reservation);
				LOGGER.info(() -> String.format("%s has been deleted with success.", reservation.toString()));
			} catch (InstanceNotFoundException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(instanceNotFoundErrorMsg(reservation.toString()));
				updateAll();
			} catch(DatabaseException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(databaseErrorMsg("deleting " + reservation.toString()));
			}
		}
	}

	/**
	 * Validates and inserts a new client in the repository and notifies the view
	 * about the changes. This method delegates the inserting to the service layer.
	 * 
	 * @param firstName	the name of the client to add.
	 * @param lastName	the surname of the client to add.
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
				view.showOperationError(clientAlreadyExistsErrorMsg(
						client.getFirstName(), client.getLastName()));
				updateAll();
			} catch(DatabaseException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(databaseErrorMsg("adding " + client.toString()));
				updateAll();
			}
		}
	}

	/**
	 * Manages the creation of a client object. If the creation goes wrong,
	 * the method notifies the view before returning a null object.
	 * 
	 * @param firstName	the name of the client to create.
	 * @param lastName	the surname of the client to create.
	 * @return			the {@code Client} created, if validation is successful;
	 * 					a {@code null} object, otherwise.
	 */
	private Client createClient(String firstName, String lastName) {
		try {
			return new Client(
					getValidatedFirstName(firstName),
					getValidatedLastName(lastName));
		} catch(IllegalArgumentException e) {
			view.showFormError(e.getMessage());
			return null;
		}
	}

	/**
	 * Performs client name validation thought {@code clientValidator}.
	 * 
	 * @param firstName					the name to validate.
	 * @return							the validated name as {@code String}.
	 * @throws IllegalArgumentException	if validation fails.
	 */
	private String getValidatedFirstName(String firstName) throws IllegalArgumentException {
		try {
			return clientValidator.validateFirstName(firstName);
		} catch(IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			throw new IllegalArgumentException("Client's name is not valid.");
		}
	}

	/**
	 * Performs client surname validation thought {@code clientValidator}.
	 * 
	 * @param lastName					the surname to validate.
	 * @return							the validated surname as {@code String}.
	 * @throws IllegalArgumentException	if validation fails.
	 */
	private String getValidatedLastName(String lastName) throws IllegalArgumentException {
		try {
			return clientValidator.validateLastName(lastName);
		} catch(IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			throw new IllegalArgumentException("Client's surname is not valid.");
		}
	}

	/**
	 * Validates and inserts a new reservation in the repository and notifies the view
	 * about the changes. This method delegates the inserting to the service layer.
	 * 
	 * @param client	the associated client of the reservation to add.
	 * @param date		the date of the reservation to add.
	 */
	@Override
	public void addReservation(Client client, String date) {
		Reservation reservation = createReservation(client, date);
		
		if (reservation != null) {
			try {
				Reservation reservationInDB = bookingService.insertNewReservation(reservation);
				view.reservationAdded(reservationInDB);
				LOGGER.info(() -> String.format("%s has been added with success.", reservationInDB.toString()));
			} catch(InstanceAlreadyExistsException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(reservationAlreadyExistsErrorMsg(
						reservation.getDate().toString()));
				updateAll();
			} catch(InstanceNotFoundException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(instanceNotFoundErrorMsg(reservation.toString()
						+ "'s client [id=" + reservation.getClientId() + "]"));
				updateAll();
			} catch(DatabaseException e) {
				LOGGER.warn(e.getMessage());
				view.showOperationError(databaseErrorMsg("adding " + reservation.toString()));
				updateAll();
			}
		}
	}

	/**
	 * Manages the creation of a reservation object. If the creation goes wrong,
	 * the method notifies the view before throwing an exception.
	 * 
	 * @param client	the associated client of the reservation to create.
	 * @param date		the date of the reservation to create.
	 * @return			the {@code Reservation} created, if validation is successful;
	 * 					a {@code null} object, otherwise.
	 */
	private Reservation createReservation(Client client, String date) throws IllegalArgumentException {
		if (client == null) {
			LOGGER.warn("Reservation's client to add cannot be null.");
			view.showFormError("Select a client to add the reservation to.");
			return null;
		}
		
		try {
			return new Reservation(
					getValidatedClientId(client.getId()),
					getValidatedDate(date));
		} catch(IllegalArgumentException e) {
			view.showFormError(e.getMessage());
			return null;
		}
	}

	/**
	 * Performs reservation client's identifier validation thought {@code reservationValidator}.
	 * 
	 * @param clientId					the client identifier to validate.
	 * @return							the validated client identifier as {@code UUID}.
	 * @throws IllegalArgumentException	if validation fails.
	 */
	private UUID getValidatedClientId(UUID clientId) throws IllegalArgumentException {
		try {
			return reservationValidator.validateClientId(clientId);
		} catch(IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			throw new IllegalArgumentException("Client's identifier associated with reservation is not valid.");
		}
	}

	/**
	 * Performs reservation date validation thought {@code reservationValidator}.
	 * 
	 * @param date						the date to validate.
	 * @return							the validated date as {@code LocalDate}.
	 * @throws IllegalArgumentException	if validation fails.
	 */
	private LocalDate getValidatedDate(String date) throws IllegalArgumentException {
		try {
			return reservationValidator.validateDate(date);
		} catch(IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			throw new IllegalArgumentException("Reservation's date is not valid.");
		}
	}

	/**
	 * Validates and modifies names of an existing client and notifies the view about the changes.
	 * This method delegates the renaming to the service layer only if new names are
	 * actually different from the old ones.
	 * 
	 * @param client		the client to modify.
	 * @param newFirstName	the new name for the client.
	 * @param newLastName	the new surname for the client.
	 */
	@Override
	public void renameClient(Client client, String newFirstName, String newLastName) {
		if (client == null) {
			LOGGER.warn("Client to rename cannot be null.");
			view.showFormError("Select a client to rename.");
			return;
		}
		
		try {
			newFirstName = getValidatedFirstName(newFirstName);
			newLastName = getValidatedLastName(newLastName);
		} catch(IllegalArgumentException e) {
			view.showFormError(e.getMessage());
			return;
		}
		
		if (Objects.equals(newFirstName, client.getFirstName())
				&& Objects.equals(newLastName, client.getLastName())) {
			LOGGER.warn("The new names are the same as the old ones.");
			view.showFormError("Insert new names for the client to be renamed.");
			return;
		}
		
		try {
			Client clientInDB = bookingService.renameClient(
					client.getId(), newFirstName, newLastName);
			view.clientRenamed(client, clientInDB);
			LOGGER.info(() -> String.format("%s has been renamed with success.", clientInDB.toString()));
		} catch(InstanceAlreadyExistsException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(clientAlreadyExistsErrorMsg(newFirstName, newLastName));
			updateAll();
		} catch(InstanceNotFoundException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(instanceNotFoundErrorMsg(client.toString()));
			updateAll();
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(databaseErrorMsg("renaming " + client.toString()));
			updateAll();
		}
	}

	/**
	 * Modifies the date of an existing reservation and notifies the view about the changes.
	 * This method delegates the rescheduling to the service layer only if new date is
	 * actually different from the old one.
	 * 
	 * @param reservation	the reservation to modify.
	 * @param newDate		the new date for the reservation.
	 */
	@Override
	public void rescheduleReservation(Reservation reservation, String newDate) {
		if (reservation == null) {
			LOGGER.warn("Reservation to reschedule cannot be null.");
			view.showFormError("Select a reservation to reschedule.");
			return;
		}
		
		LocalDate validatedDate;
		try {
			validatedDate = getValidatedDate(newDate);
		} catch(IllegalArgumentException e) {
			view.showFormError(e.getMessage());
			return;
		}
		
		if (validatedDate == reservation.getDate()) {
			LOGGER.warn("The new date is the same as the old one.");
			view.showFormError("Insert a new date for the reservation to be rescheduled.");
			return;
		}
		
		try {
			Reservation reservationInDB = bookingService
					.rescheduleReservation(reservation.getId(), validatedDate);
			view.reservationRescheduled(reservation, reservationInDB);
			LOGGER.info(() -> String.format("%s has been rescheduled with success.", reservationInDB.toString()));
		} catch(InstanceAlreadyExistsException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(reservationAlreadyExistsErrorMsg(validatedDate.toString()));
			updateAll();
		} catch(InstanceNotFoundException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(instanceNotFoundErrorMsg(reservation.toString()));
			updateAll();
		} catch(DatabaseException e) {
			LOGGER.warn(e.getMessage());
			view.showOperationError(databaseErrorMsg("rescheduling " + reservation.toString()));
			updateAll();
		}
	}

	/**
	 * Generates an error message used when a {@code InstanceAlreadyExistsException} occurs
	 * for {@code Client} entities.
	 * 
	 * @param date	the date of the existing reservation.
	 * @return		a {@code String} containing the generated error message.
	 */
	private String clientAlreadyExistsErrorMsg(String firstName, String lastName) {
		return "A client named " + firstName + " " + lastName + " has already been made.";
	}

	/**
	 * Generates an error message used when a {@code InstanceAlreadyExistsException} occurs
	 * for {@code Reservation} entities.
	 * 
	 * @param date	the date of the existing reservation.
	 * @return		a {@code String} containing the generated error message.
	 */
	private String reservationAlreadyExistsErrorMsg(String date) {
		return "A reservation on " + date + " has already been made.";
	}

	/**
	 * Generates an error message used when a {@code InstanceNotFoundException} occurs.
	 * 
	 * @param notFoundInstance	description of the not found object.
	 * @return					a {@code String} containing the generated error message.
	 */
	private String instanceNotFoundErrorMsg(String notFoundInstance) {
		return notFoundInstance + " no longer exists.";
	}

	/**
	 * Generates an error message used when a {@code DatabaseException} occurs.
	 * 
	 * @param failedAction	description of the failed actions.
	 * @return				a {@code String} containing the generated error message.
	 */
	private String databaseErrorMsg(String failedAction) {
		return "Something went wrong while " + failedAction + ".";
	}
}
