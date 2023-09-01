package io.github.marcopaglio.booking.service.transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcopaglio.booking.exception.DatabaseException;
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/**
 * Implements methods for operating on repositories of the booking application using transactions.
 * 
 * @see <a href="../../repository/ClientRepository.html">ClientRepository</a>
 * @see <a href="../../repository/ReservationRepository.html">ReservationRepository</a>
 */
public class TransactionalBookingService implements BookingService{
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(TransactionalBookingService.class);

	/**
	 * Defines an error message used when a database error occurs.
	 */
	private static final String DATABASE_ERROR_MSG = "A database error occurs: the request cannot be executed.";

	/**
	 * Allows the service to execute transactions.
	 */
	private TransactionManager transactionManager;

	/**
	 * Constructs a service for the booking application with a transaction manager.
	 * 
	 * @param transactionManager	the {@code TransactionManager} used for applying transactions.
	 */
	public TransactionalBookingService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Retrieves all the clients saved in the database within a transaction.
	 * 
	 * @return						the list of clients found in the database.
	 * @throws DatabaseException	if a database error occurs.
	 */
	@Override
	public List<Client> findAllClients() throws DatabaseException {
		try {
			return transactionManager.doInTransaction(ClientRepository::findAll);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Retrieves all the reservations saved in the database within a transaction.
	 * 
	 * @return						the list of reservations found in the database.
	 * @throws DatabaseException	if a transaction failure occurs on database.
	 */
	@Override
	public List<Reservation> findAllReservations() throws DatabaseException {
		try {
			return transactionManager.doInTransaction(ReservationRepository::findAll);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Retrieves the client with specified name and surname from the database within a transaction.
	 * 
	 * @param firstName						the name of the client to find.
	 * @param lastName						the surname of the client to find.
	 * @return								the {@code Client} named {@code firstName} and {@code lastName}.
	 * @throws IllegalArgumentException		if {@code firstName} or {@code lastName} are null.
	 * @throws InstanceNotFoundException	if there is no client with those names in database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public Client findClientNamed(String firstName, String lastName)
			throws IllegalArgumentException, InstanceNotFoundException, DatabaseException {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to find cannot be null.");
		
		try {
			Optional<Client> possibleClient = transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> clientRepository.findByName(firstName, lastName));
			if (possibleClient.isPresent())
				return possibleClient.get();
			throw new InstanceNotFoundException(clientNotFoundMsg(firstName, lastName));
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Retrieves the reservation of the specified date from the database within a transaction.
	 * 
	 * @param date							the date of the reservation to find.
	 * @return								the {@code Reservation} on {@code date}.
	 * @throws IllegalArgumentException		if {@code date} is null.
	 * @throws InstanceNotFoundException	if there is no reservation on that date in database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public Reservation findReservationOn(LocalDate date)
			throws IllegalArgumentException, InstanceNotFoundException, DatabaseException {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to find cannot be null.");
		
		try {
			return transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
					if (possibleReservation.isPresent())
						return possibleReservation.get();
					throw new InstanceNotFoundException(reservationNotFoundMsg(date));
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Adds a new client in the database within a transaction.
	 * This method checks if the client is present in the database before inserting.
	 * 
	 * @param client							the client to insert.
	 * @return									the {@code Client} inserted.
	 * @throws IllegalArgumentException			if {@code client} is null.
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 * @throws DatabaseException				if a transaction failure occurs on database.
	 */
	@Override
	public Client insertNewClient(Client client)
			throws IllegalArgumentException, InstanceAlreadyExistsException, DatabaseException {
		if (client == null)
			throw new IllegalArgumentException("Client to insert cannot be null.");
		
		try {
			return transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> {
					Optional<Client> possibleClient = clientRepository
							.findByName(client.getFirstName(), client.getLastName());
					if (possibleClient.isEmpty()) {
						return clientRepository.save(client);
					}
					throw new InstanceAlreadyExistsException(
						client.toString() + " is already in the database.");
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Adds a new reservation in the database within a transaction.
	 * This method checks if the reservation is not present and the associated client is present
	 * in the database before inserting.
	 * 
	 * @param reservation						the reservation to insert.
	 * @return									the {@code Reservation} inserted.
	 * @throws IllegalArgumentException			if {@code reservation} is null.
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already in the database.
	 * @throws InstanceNotFoundException		if the associated {@code client} doesn't exist in the database.
	 * @throws DatabaseException				if a transaction failure occurs on database.
	 */
	@Override
	public Reservation insertNewReservation(Reservation reservation) throws IllegalArgumentException,
			InstanceAlreadyExistsException, InstanceNotFoundException, DatabaseException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to insert cannot be null.");
		
		try {
			return transactionManager.doInTransaction(
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservation =
							reservationRepository.findByDate(reservation.getDate());
					if (possibleReservation.isEmpty()) {
						Optional<Client> possibleClient = clientRepository.findById(reservation.getClientId());
						if (possibleClient.isPresent()) {
							return reservationRepository.save(reservation);
						}
						throw new InstanceNotFoundException(
							"The client with id: " + reservation.getClientId()
							+ ", associated to " + reservation.toString() + " is not in the database. "
							+ "Please, insert the client before the reservation.");
					}
					throw new InstanceAlreadyExistsException(
						reservation.toString() + " is already in the database.");
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Deletes the client with specified name and surname
	 * and all his reservation from the database within a transaction.
	 * This method checks if the client is present in the database before removing.
	 * 
	 * @param firstName						the name of the client to remove.
	 * @param lastName						the surname of the client to remove.
	 * @throws IllegalArgumentException		if {@code firstName} or {@code lastName} are null.
	 * @throws InstanceNotFoundException	if there is no client with those names in database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public void removeClientNamed(String firstName, String lastName)
			throws IllegalArgumentException, InstanceNotFoundException, DatabaseException {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to remove cannot be null.");
		
		try {
			transactionManager.doInTransaction(
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					Optional<Client> possibleClient = clientRepository.findByName(firstName, lastName);
					if (possibleClient.isPresent()) {
						Client clientToRemove = possibleClient.get();
						List<Reservation> reservationList = reservationRepository
							.findByClient(clientToRemove.getId());
						for (Reservation reservation : reservationList)
							reservationRepository.delete(reservation);
						clientRepository.delete(clientToRemove);
						return null;
					}
					throw new InstanceNotFoundException(clientNotFoundMsg(firstName, lastName));
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Deletes the reservation of the specified date from the database within a transaction.
	 * This method checks if the reservation is present in the database before removing.
	 * 
	 * @param date							the date of the reservation to find.
	 * @throws IllegalArgumentException		if {@code date} is null.
	 * @throws InstanceNotFoundException	if there is no reservation on that date in database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public void removeReservationOn(LocalDate date)
			throws IllegalArgumentException, InstanceNotFoundException, DatabaseException {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to remove cannot be null.");
		
		try {
			transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
					if (possibleReservation.isPresent()) {
						reservationRepository.delete(possibleReservation.get());
						return null;
					}
					throw new InstanceNotFoundException(reservationNotFoundMsg(date));
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Generates a message for the client that was not found.
	 * 
	 * @param firstName	the name of the client not found.
	 * @param lastName	the surname of the client not found.
	 */
	private String clientNotFoundMsg(String firstName, String lastName) {
		return "There is no client named \"" + firstName + " " + lastName + "\" in the database.";
	}

	/**
	 * Generates a message for the reservation that was not found.
	 * 
	 * @param date	the date of the reservation not found.
	 */
	private String reservationNotFoundMsg(LocalDate date) {
		return "There is no reservation on \"" + date + "\" in the database.";
	}
}
