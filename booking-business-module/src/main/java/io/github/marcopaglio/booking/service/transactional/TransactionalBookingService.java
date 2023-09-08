package io.github.marcopaglio.booking.service.transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	 * Defines an error message used when a client entity is not found.
	 */
	static final String CLIENT_NOT_FOUND_ERROR_MSG = "The requested client was not found in the database.";

	/**
	 * Defines an error message used when a client entity already exists.
	 */
	static final String CLIENT_ALREADY_EXISTS_ERROR_MSG = "That client is already in the database.";

	/**
	 * Defines an error message used when a reservation entity is not found.
	 */
	static final String RESERVATION_NOT_FOUND_ERROR_MSG = "The requested reservation was not found in the database.";

	/**
	 * Defines an error message used when a reservation entity already exists.
	 */
	static final String RESERVATION_ALREADY_EXISTS_ERROR_MSG = "That reservation is already in the database.";

	/**
	 * Defines an error message used when a database error occurs.
	 */
	static final String DATABASE_ERROR_MSG = "A database error occurs: the request cannot be executed.";

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
	 * Retrieves the client with the specified id from the database within a transaction.
	 * 
	 * @param id							the identifier of the client to find.
	 * @return								the {@code Client} identified by {@code id}.
	 * @throws InstanceNotFoundException	if there is no client with that id in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	@Override
	public Client findClient(UUID id) throws InstanceNotFoundException, DatabaseException {
		try {
			Optional<Client> possibleClient = transactionManager.doInTransaction(
					(ClientRepository clientRepository) -> clientRepository.findById(id));
			if (possibleClient.isPresent())
				return possibleClient.get();
			throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Retrieves the reservation with the specified id from the database within a transaction.
	 * 
	 * @param id							the identifier of the reservation to find.
	 * @return								the {@code Reservation} identified by {@code id}.
	 * @throws InstanceNotFoundException	if there is no reservation with that id in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	@Override
	public Reservation findReservation(UUID id) throws InstanceNotFoundException, DatabaseException {
		try {
			Optional<Reservation> possibleReservation = transactionManager.doInTransaction(
					(ReservationRepository reservationRepository) -> reservationRepository.findById(id));
			if (possibleReservation.isPresent())
				return possibleReservation.get();
			throw new InstanceNotFoundException(RESERVATION_NOT_FOUND_ERROR_MSG);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Retrieves the client with specified name and surname from the database
	 * within a transaction.
	 * 
	 * @param firstName						the name of the client to find.
	 * @param lastName						the surname of the client to find.
	 * @return								the {@code Client} named {@code firstName}
	 * 										and {@code lastName}.
	 * @throws InstanceNotFoundException	if there is no client with those names in the database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public Client findClientNamed(String firstName, String lastName)
			throws InstanceNotFoundException, DatabaseException {
		try {
			Optional<Client> possibleClient = transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> clientRepository.findByName(firstName, lastName));
			if (possibleClient.isPresent())
				return possibleClient.get();
			throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
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
	 * @throws InstanceNotFoundException	if there is no reservation on that date in the database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public Reservation findReservationOn(LocalDate date) throws InstanceNotFoundException, DatabaseException {
		try {
			Optional<Reservation> possibleReservation = transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> reservationRepository.findByDate(date));
			if (possibleReservation.isPresent())
				return possibleReservation.get();
			throw new InstanceNotFoundException(RESERVATION_NOT_FOUND_ERROR_MSG);
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
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 * @throws DatabaseException				if a transaction failure occurs on database.
	 */
	@Override
	public Client insertNewClient(Client client)
			throws InstanceAlreadyExistsException, DatabaseException {
		try {
			return transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> {
					Optional<Client> possibleClient = clientRepository
							.findByName(client.getFirstName(), client.getLastName());
					if (possibleClient.isEmpty()) {
						return clientRepository.save(client);
					}
					throw new InstanceAlreadyExistsException(CLIENT_ALREADY_EXISTS_ERROR_MSG);
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
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already in the database.
	 * @throws InstanceNotFoundException		if the associated {@code client} doesn't
	 * 											exist in the database.
	 * @throws DatabaseException				if a transaction failure occurs on database.
	 */
	@Override
	public Reservation insertNewReservation(Reservation reservation)
			throws InstanceAlreadyExistsException, InstanceNotFoundException, DatabaseException {
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
						throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
					}
					throw new InstanceAlreadyExistsException(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Deletes the client with the specified id and all his reservation from the database
	 * within a transaction.
	 * 
	 * @param id							the identifier of the client to remove.
	 * @throws InstanceNotFoundException	if there is no client with that identifier
	 * 										in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	@Override
	public void removeClient(UUID id) throws InstanceNotFoundException, DatabaseException {
		try {
			transactionManager.doInTransaction(
				(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
					Optional<Client> possibleClient = clientRepository.findById(id);
					if (possibleClient.isPresent()) {
						List<Reservation> reservationList = reservationRepository.findByClient(id);
						for (Reservation reservation : reservationList)
							reservationRepository.delete(reservation);
						clientRepository.delete(possibleClient.get());
						return null;
					}
					throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Deletes the reservation with the specified id from the database within a transaction.
	 * 
	 * @param id							the identifier of the reservation to remove.
	 * @throws InstanceNotFoundException	if there is no reservation with that identifier
	 * 										in the database.
	 * @throws DatabaseException			if a database error occurs.
	 */
	@Override
	public void removeReservation(UUID id) throws InstanceNotFoundException, DatabaseException {
		try {
			transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservation = reservationRepository.findById(id);
					if (possibleReservation.isPresent()) {
						reservationRepository.delete(possibleReservation.get());
						return null;
					}
					throw new InstanceNotFoundException(RESERVATION_NOT_FOUND_ERROR_MSG);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Deletes the client with specified name and surname and all his reservation
	 * from the database within a transaction.
	 * This method checks if the client is present in the database before removing.
	 * 
	 * @param firstName						the name of the client to remove.
	 * @param lastName						the surname of the client to remove.
	 * @throws InstanceNotFoundException	if there is no client with those names in the database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public void removeClientNamed(String firstName, String lastName) throws InstanceNotFoundException, DatabaseException {
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
					throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
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
	 * @throws InstanceNotFoundException	if there is no reservation on that date in the database.
	 * @throws DatabaseException			if a transaction failure occurs on database.
	 */
	@Override
	public void removeReservationOn(LocalDate date) throws InstanceNotFoundException, DatabaseException {
		try {
			transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
					if (possibleReservation.isPresent()) {
						reservationRepository.delete(possibleReservation.get());
						return null;
					}
					throw new InstanceNotFoundException(RESERVATION_NOT_FOUND_ERROR_MSG);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Changes name and surname of the client with the specified id in the database
	 * within a transaction.
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
	@Override
	public Client renameClient(UUID id, String newFirstName, String newLastName)
			throws InstanceNotFoundException, InstanceAlreadyExistsException, DatabaseException {
		try {
			return transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> {
					Optional<Client> possibleClientInDB = clientRepository.findById(id);
					if (possibleClientInDB.isEmpty())
						throw new InstanceNotFoundException(CLIENT_NOT_FOUND_ERROR_MSG);
					Optional<Client> possibleSameNameClient = clientRepository
							.findByName(newFirstName, newLastName);
					if (possibleSameNameClient.isPresent())
						throw new InstanceAlreadyExistsException(CLIENT_ALREADY_EXISTS_ERROR_MSG);
					Client clientInDB = possibleClientInDB.get();
					clientInDB.setFirstName(newFirstName);
					clientInDB.setLastName(newLastName);
					return clientRepository.save(clientInDB);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}

	/**
	 * Changes date of the reservation with the specified id in the database
	 * within a transaction.
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
	@Override
	public Reservation rescheduleReservation(UUID id, LocalDate newDate)
			throws InstanceNotFoundException, InstanceAlreadyExistsException, DatabaseException {
		try {
			return transactionManager.doInTransaction(
				(ReservationRepository reservationRepository) -> {
					Optional<Reservation> possibleReservationInDB =
							reservationRepository.findById(id);
					if (possibleReservationInDB.isEmpty())
						throw new InstanceNotFoundException(RESERVATION_NOT_FOUND_ERROR_MSG);
					Optional<Reservation> possibleSameDateReservation =
							reservationRepository.findByDate(newDate);
					if (possibleSameDateReservation.isPresent())
						throw new InstanceAlreadyExistsException(RESERVATION_ALREADY_EXISTS_ERROR_MSG);
					Reservation reservationInDB = possibleReservationInDB.get();
					reservationInDB.setDate(newDate);
					return reservationRepository.save(reservationInDB);
				}
			);
		} catch(TransactionException e) {
			LOGGER.warn(e.getMessage());
			throw new DatabaseException(DATABASE_ERROR_MSG, e.getCause());
		}
	}
}