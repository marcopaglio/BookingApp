package io.github.marcopaglio.booking.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ReservationRepository;

/**
 * Implementation of repository layer through MongoDB for reservation entities of the booking application.
 */
public class ReservationMongoRepository extends MongoRepository<Reservation> implements ReservationRepository {
	public static final String BOOKING_DB_NAME = "booking_db";
	public static final String RESERVATION_COLLECTION_NAME = "booking_reservation";

	public ReservationMongoRepository(MongoClient client) {
		super(client
				.getDatabase(BOOKING_DB_NAME)
				.getCollection(RESERVATION_COLLECTION_NAME, Reservation.class));
		
		// collection configuration
		getCollection().createIndex(Indexes.descending("date"), new IndexOptions().unique(true));
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
	public void delete(Reservation reservation) {
		// TODO Auto-generated method stub

	}

}
