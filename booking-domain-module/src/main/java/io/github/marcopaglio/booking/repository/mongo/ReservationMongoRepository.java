package io.github.marcopaglio.booking.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ReservationRepository;

/**
 * Implementation of repository layer through MongoDB for reservation entities of the booking application.
 */
public class ReservationMongoRepository implements ReservationRepository {
	public static final String BOOKING_DB_NAME = "booking_db";
	public static final String RESERVATION_COLLECTION_NAME = "booking_reservation";

	private MongoCollection<Reservation> reservationCollection;

	public ReservationMongoRepository(MongoClient client) {
		reservationCollection = client
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(RESERVATION_COLLECTION_NAME, Reservation.class);
	}

	@Override
	public List<Reservation> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Reservation> findByDate(LocalDate date) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<Reservation> findByClient(UUID clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reservation save(Reservation reservation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(LocalDate date) {
		// TODO Auto-generated method stub

	}

}
