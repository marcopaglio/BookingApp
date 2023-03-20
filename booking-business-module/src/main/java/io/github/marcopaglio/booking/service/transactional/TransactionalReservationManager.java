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

	public TransactionalReservationManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * This method is used to retrieve all the reservations saved in the database
	 * within a transaction.
	 */
	@Override
	public List<Reservation> findAllReservations() {
		return transactionManager.doInTransaction(ReservationRepository::findAll);
	}

	/*
	 * This method is used to retrieve the reservation of the specified date
	 * from the database within a transaction.
	 * @throws IllegalArgumentException if date is null.
	 * @throws NoSuchElementException if there is no reservation on that date in database.
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
						possibleClient.get().addReservation(reservation);
						clientRepository.save(possibleClient.get());
						return reservationRepository.save(reservation);
					}
					throw new NoSuchElementException(
						"The client with uuid: " + reservation.getClientUUID() + ", associated to the reservation "
						+ "to insert is not in the database. Please, insert the client before the reservation.");
				}
				throw new InstanceAlreadyExistsException(reservation.toString() + " is already in the database.");
			}
		);
	}

	@Override
	public void removeReservationOn(LocalDate date) {
		if (date == null)
			throw new IllegalArgumentException("Date of reservation to remove cannot be null.");
		
		transactionManager.doInTransaction(
			(ReservationRepository reservationRepository) -> {
				Optional<Reservation> possibleReservation = reservationRepository.findByDate(date);
				if (possibleReservation.isPresent()) {
					reservationRepository.delete(date);
					return null;
				}
				throw new NoSuchElementException(
					"There is no reservation on \"" + date + "\" in the database.");
			}
		);
	}

}
