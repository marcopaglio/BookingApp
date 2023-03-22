package io.github.marcopaglio.booking.service.transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.service.ReservationManager;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/*
 * Implements methods for operating on Reservation repository using transactions.
 */
public class TransactionalReservationManager implements ReservationManager {
	private TransactionManager transactionManager;

	/*
	 * The constructor.
	 * @param transactionManager	the {@code TransactionManager} used for applying transactions.
	 */
	public TransactionalReservationManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * This method is used to retrieve all the reservations saved in the database within a transaction.
	 * @return	the list of reservations found in the database.
	 */
	@Override
	public List<Reservation> findAllReservations() {
		return transactionManager.doInTransaction(ReservationRepository::findAll);
	}

	/*
	 * This method is used to retrieve the reservation of the specified date
	 * from the database within a transaction.
	 * @param date						the date of the reservation to find.
	 * @return							the {@code Reservation} on {@code date}.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	@Override
	public Reservation findReservationOn(LocalDate date) {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to find cannot be null.");
		
		return transactionManager.doInTransaction(
			(ReservationRepository reservationRepository) -> {
				Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
				if (possibleReservation.isPresent())
					return possibleReservation.get();
				throw new NoSuchElementException(
					"There is no reservation on \"" + date.toString() + "\" in the database.");
			}
		);
	}

	/*
	 * This method is used to add a new reservation in the database within a transaction.
	 * This method checks if the reservation is not present and the associated client is present
	 * in the database before inserting.
	 * @param reservation						the reservation to be inserting.
	 * @return									the {@code Reservation} inserted.
	 * @throws IllegalArgumentException			if {@code reservation} is null.
	 * @throws InstanceAlreadyExistsException	if {@code reservation} is already in the database.
	 */
	@Override
	public Reservation insertNewReservation(Reservation reservation) {
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
						client.addReservation(reservation);
						clientRepository.save(client);
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

	/*
	 * This method is used to remove the reservation on the specified date from the database.
	 * This method checks if the reservation is present in the database before removing.
	 * This method updates the client's reservations list after inserting
	 * if the client is in the database.
	 * @param date						the date of the reservation to find.
	 * @throws IllegalArgumentException	if {@code date} is null.
	 * @throws NoSuchElementException	if there is no reservation on that date in database.
	 */
	@Override
	public void removeReservationOn(LocalDate date) {
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
						possibleClient.get().removeReservation(possibleReservation.get());
						clientRepository.save(possibleClient.get());
					}
					return null;
				}
				throw new NoSuchElementException(
					"There is no reservation on \"" + date + "\" in the database.");
			}
		);
	}
}
