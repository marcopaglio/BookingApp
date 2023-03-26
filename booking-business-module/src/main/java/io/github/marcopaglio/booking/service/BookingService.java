package io.github.marcopaglio.booking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
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
	 * @return	the list of clients found in the database.
	 */
	public List<Client> findAllClients();

	/**
	 * Retrieves the client with specified name and surname from the database.
	 * 
	 * @param firstName					the name of the client to find.
	 * @param lastName					the surname of the client to find.
	 * @return							the {@code Client} named {@code firstName} and {@code lastName}.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	public Client findClientNamed(String firstName, String lastName) throws IllegalArgumentException, NoSuchElementException;

	/**
	 * Adds a new client in the database.
	 * Eventual reservations of the new client will no be saved.
	 * 
	 * @param client							the client to insert.
	 * @return									the {@code Client} inserted.
	 * @throws IllegalArgumentException			if {@code client} is null.
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 */
	public Client insertNewClient(Client client) throws IllegalArgumentException, InstanceAlreadyExistsException;

	/**
	 * Deletes the client with specified name and surname
	 * and all his reservation from the database.
	 * 
	 * @param firstName					the name of the client to remove.
	 * @param lastName					the surname of the client to remove.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	public void removeClientNamed(String firstName, String lastName) throws IllegalArgumentException, NoSuchElementException;

	/**
	 * Retrieves all the reservations saved in the database.
	 * 
	 * @return	the list of reservations found in the database.
	 */
	public List<Reservation> findAllReservations();

	/**
	 * Retrieves the reservation of the specified date from the database.
	 * 
	 * @param date						the date of the reservation to find.
	 * @return							the {@code Reservation} on {@code date}.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	public Reservation findReservationOn(LocalDate date) throws IllegalArgumentException, NoSuchElementException;

	/**
	 * Adds a new reservation in the database and updates the associated client.
	 * 
	 * @param reservation						the reservation to insert.
	 * @return									the {@code Reservation} inserted.
	 * @throws IllegalArgumentException			if {@code reservation} is null.
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already in the database.
	 */
	public Reservation insertNewReservation(Reservation reservation) throws IllegalArgumentException, InstanceAlreadyExistsException;

	/**
	 * Deletes the reservation of the specified date from the database
	 * and update the associated client.
	 * 
	 * @param date						the date of the reservation to find.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	public void removeReservationOn(LocalDate date) throws IllegalArgumentException, NoSuchElementException;
}
