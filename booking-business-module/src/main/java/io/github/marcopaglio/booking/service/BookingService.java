package io.github.marcopaglio.booking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.github.marcopaglio.booking.exception.DatabaseException;
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * This interface provides methods for operating on repositories of the booking application.
 * 
 * @see <a href="../../repository/ClientRepository.html">ClientRepository</a>
 * @see <a href="../../repository/ReservationRepository.html">ReservationRepository</a>
 */
public interface BookingService {

	/**
	 * Retrieves all the clients saved in the database.
	 * 
	 * @return						the list of clients found in the database.
	 * @throws DatabaseException	if a database error occurs.
	 */
	public List<Client> findAllClients();

	/**
	 * Retrieves all the reservations saved in the database.
	 * 
	 * @return						the list of reservations found in the database.
	 * @throws DatabaseException	if a database error occurs.
	 */
	public List<Reservation> findAllReservations();

	/**
	 * Retrieves the client with the specified id from the database.
	 * 
	 * @param id							the identifier of the client to find.
	 * @return								the {@code Client} identified by {@code id}.
	 * @throws InstanceNotFoundException	if there is no client with that id in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public Client findClient(UUID id) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Retrieves the reservation with the specified id from the database.
	 * 
	 * @param id							the identifier of the reservation to find.
	 * @return								the {@code Reservation} identified by {@code id}.
	 * @throws InstanceNotFoundException	if there is no reservation with that id in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public Reservation findReservation(UUID id) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Retrieves the client with specified name and surname from the database.
	 * 
	 * @param firstName						the name of the client to find.
	 * @param lastName						the surname of the client to find.
	 * @return								the {@code Client} named {@code firstName}
	 * 										and {@code lastName}.
	 * @throws InstanceNotFoundException	if there is no client with those names in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public Client findClientNamed(String firstName, String lastName) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Retrieves the reservation of the specified date from the database.
	 * 
	 * @param date							the date of the reservation to find.
	 * @return								the {@code Reservation} on {@code date}.
	 * @throws InstanceNotFoundException	if there is no reservation on that date in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public Reservation findReservationOn(LocalDate date) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Adds a new client in the database.
	 * 
	 * @param client							the client to insert.
	 * @return									the {@code Client} inserted.
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 * @throws DatabaseException				if a database error occurs.
	 */
	public Client insertNewClient(Client client) throws InstanceAlreadyExistsException, DatabaseException;

	/**
	 * Adds a new reservation in the database.
	 * 
	 * @param reservation						the reservation to insert.
	 * @return									the {@code Reservation} inserted.
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already
	 * 											in the database.
	 * @throws InstanceNotFoundException		if the associated {@code client} doesn't
	 * 											exist in the database.
	 * @throws DatabaseException				if a database error occurs.
	 */
	public Reservation insertNewReservation(Reservation reservation) throws InstanceAlreadyExistsException, InstanceNotFoundException, DatabaseException;

	/**
	 * Deletes the client with the specified id and all his reservation from the database.
	 * 
	 * @param id							the identifier of the client to remove.
	 * @throws InstanceNotFoundException	if there is no client with that identifier in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public void removeClient(UUID id) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Deletes the reservation with the specified id from the database.
	 * 
	 * @param id							the identifier of the reservation to remove.
	 * @throws InstanceNotFoundException	if there is no reservation with that identifier
	 * 										in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public void removeReservation(UUID id) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Deletes the client with specified name and surname and all his reservation
	 * from the database.
	 * 
	 * @param firstName						the name of the client to remove.
	 * @param lastName						the surname of the client to remove.
	 * @throws InstanceNotFoundException	if there is no client with those names
	 * 										in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public void removeClientNamed(String firstName, String lastName) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Deletes the reservation on the specified date from the database.
	 * 
	 * @param date							the date of the reservation to remove.
	 * @throws InstanceNotFoundException	if there is no reservation on that date in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	public void removeReservationOn(LocalDate date) throws InstanceNotFoundException, DatabaseException;

	/**
	 * Changes name and surname of the client with the specified id in the database.
	 * 
	 * @param id								the identifier of the client to rename.
	 * @param newFirstName						the new name for the client.
	 * @param newLastName						the new surname for the client.
	 * @return									the {@code Client} renamed.
	 * @throws InstanceNotFoundException		if there is no {@code client} with specified id
	 * 											in the database.
	 * @throws InstanceAlreadyExistsException	if a {@code Client} with those names is
	 * 											already in the database.
	 * @throws DatabaseException				if a database error occurs.
	 */
	public Client renameClient(UUID id, String newFirstName, String newLastName) throws InstanceNotFoundException, InstanceAlreadyExistsException, DatabaseException;

	/**
	 * Changes date of the reservation with the specified id in the database.
	 * 
	 * @param id								the identifier of the reservation to reschedule.
	 * @param newDate							the new date for the reservation.
	 * @return									the {@code Reservation} rescheduled.
	 * @throws InstanceNotFoundException		if there is no {@code reservation} with
	 * 											specified id in the database.
	 * @throws InstanceAlreadyExistsException	if a {@code reservation} with that date is 
	 * 											already in the database.
	 * @throws DatabaseException				if a database error occurs.
	 */
	public Reservation rescheduleReservation(UUID id, LocalDate newDate) throws InstanceNotFoundException, InstanceAlreadyExistsException, DatabaseException;
}
