package io.github.marcopaglio.booking.service.transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
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
	 * @return	the list of clients found in the database.
	 */
	@Override
	public List<Client> findAllClients() {
		return transactionManager.doInTransaction(ClientRepository::findAll);
	}

	/**
	 * Retrieves all the reservations saved in the database within a transaction.
	 * 
	 * @return	the list of reservations found in the database.
	 */
	@Override
	public List<Reservation> findAllReservations() {
		return transactionManager.doInTransaction(ReservationRepository::findAll);
	}

	/**
	 * Retrieves the client with specified name and surname from the database within a transaction.
	 * 
	 * @param firstName					the name of the client to find.
	 * @param lastName					the surname of the client to find.
	 * @return							the {@code Client} named {@code firstName} and {@code lastName}.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	@Override
	public Client findClientNamed(String firstName, String lastName)
			throws IllegalArgumentException, NoSuchElementException {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to find cannot be null.");
		
		Optional<Client> possibleClient = transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> clientRepository.findByName(firstName, lastName));
		if (possibleClient.isPresent())
			return possibleClient.get();
		throw new NoSuchElementException(clientNotFoundMsg(firstName, lastName));
	}

	/**
	 * Retrieves the reservation of the specified date from the database within a transaction.
	 * 
	 * @param date						the date of the reservation to find.
	 * @return							the {@code Reservation} on {@code date}.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	@Override
	public Reservation findReservationOn(LocalDate date)
			throws IllegalArgumentException, NoSuchElementException {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to find cannot be null.");
		
		return transactionManager.doInTransaction(
			(ReservationRepository reservationRepository) -> {
				Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
				if (possibleReservation.isPresent())
					return possibleReservation.get();
				throw new NoSuchElementException(reservationNotFoundMsg(date));
			}
		);
	}

	/**
	 * Adds a new client in the database within a transaction.
	 * Eventual reservations of the new client will no be saved.
	 * This method checks if the client is present in the database before inserting.
	 * 
	 * @param client							the client to insert.
	 * @return									the {@code Client} inserted.
	 * @throws IllegalArgumentException			if {@code client} is null.
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 */
	@Override
	public Client insertNewClient(Client client)
			throws IllegalArgumentException, InstanceAlreadyExistsException {
		if (client == null)
			throw new IllegalArgumentException("Client to insert cannot be null.");
		
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
	}

	/**
	 * Adds a new reservation in the database and to update the associated client within a transaction.
	 * This method checks if the reservation is not present and the associated client is present
	 * in the database before inserting.
	 * In case of the reservation is already in client's list, the method generates a warning message.
	 * 
	 * @param reservation						the reservation to insert.
	 * @return									the {@code Reservation} inserted.
	 * @throws IllegalArgumentException			if {@code reservation} is null.
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already in the database.
	 * @throws NoSuchElementException			if the associated client doesn't exists in the database.
	 */
	@Override
	public Reservation insertNewReservation(Reservation reservation)
			throws IllegalArgumentException, InstanceAlreadyExistsException, NoSuchElementException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to insert cannot be null.");
		
		return transactionManager.doInTransaction(
			(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
				Optional<Reservation> possibleReservation =
						reservationRepository.findByDate(reservation.getDate());
				if (possibleReservation.isEmpty()) {
					Optional<Client> possibleClient = clientRepository.findById(reservation.getClientUUID());
					if (possibleClient.isPresent()) {
						Client client = possibleClient.get();
						try {
							client.addReservation(reservation);
							clientRepository.save(client);
						} catch (InstanceAlreadyExistsException e) {
							logOnConsole(e);
						}
						return reservationRepository.save(reservation);
					}
					throw new NoSuchElementException(
						"The client with uuid: " + reservation.getClientUUID()
						+ ", associated to the reservation to insert is not in the database. "
						+ "Please, insert the client before the reservation.");
				}
				throw new InstanceAlreadyExistsException(
					reservation.toString() + " is already in the database.");
			}
		);
	}

	/**
	 * Deletes the client with specified name and surname
	 * and all his reservation from the database within a transaction.
	 * This method checks if the client is present in the database before removing.
	 * 
	 * @param firstName					the name of the client to remove.
	 * @param lastName					the surname of the client to remove.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	@Override
	public void removeClientNamed(String firstName, String lastName)
			throws IllegalArgumentException, NoSuchElementException {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to remove cannot be null.");
		
		transactionManager.doInTransaction(
			(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
				Optional<Client> possibleClient = clientRepository.findByName(firstName, lastName);
				if (possibleClient.isPresent()) {
					List<Reservation> reservationList = reservationRepository
						.findByClient(possibleClient.get().getUuid());
					for (Reservation reservation : reservationList)
						reservationRepository.delete(reservation.getDate());
					clientRepository.delete(firstName, lastName);
					return null;
				}
				throw new NoSuchElementException(clientNotFoundMsg(firstName, lastName));
			}
		);
	}

	/**
	 * Deletes the reservation of the specified date from the database
	 * and update the associated client within a transaction.
	 * This method checks if the reservation is present in the database before removing.
	 * This method updates the client's reservations list after inserting
	 * if the client is in the database.
	 * In case of the reservation is not in client's list, the method generates a warning message.
	 * 
	 * @param date						the date of the reservation to find.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	@Override
	public void removeReservationOn(LocalDate date)
			throws IllegalArgumentException, NoSuchElementException {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to remove cannot be null.");
		
		transactionManager.doInTransaction(
			(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
				Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
				if (possibleReservation.isPresent()) {
					reservationRepository.delete(date);
					Optional<Client> possibleClient = clientRepository
							.findById(possibleReservation.get().getClientUUID());
					if (possibleClient.isPresent()) {
						Client client = possibleClient.get();
						try {
							client.removeReservation(possibleReservation.get());
							clientRepository.save(client);
						} catch (NoSuchElementException e) {
							logOnConsole(e);
						}
					}
					return null;
				}
				throw new NoSuchElementException(reservationNotFoundMsg(date));
			}
		);
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
