package io.github.marcopaglio.booking.service.transactional;

import java.time.LocalDate;
import java.util.List;

import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.ReservationManager;

/*
 * Implements methods for operating on Reservation entities using transactions.
 */
public class TransactionalReservationManager implements ReservationManager {

	@Override
	public List<Reservation> getAllReservations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reservation getReservationOn(LocalDate date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertNewReservation(Reservation reservation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeReservationOn(LocalDate date) {
		// TODO Auto-generated method stub
		
	}

}
