package io.github.marcopaglio.booking.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ReservationRepository;

import static io.github.marcopaglio.booking.model.Entity.ID_DB;

/**
 * Implementation of repository layer through MongoDB for reservation entities of the booking application.
 */
public class ReservationMongoRepository extends MongoRepository<Reservation> implements ReservationRepository {
	/**
	 * Name of the database in which the repository works.
	 */
	public static final String BOOKING_DB_NAME = "booking_db";

	/**
	 * Name of the collection managed by the repository.
	 */
	public static final String RESERVATION_COLLECTION_NAME = "booking_reservation";

	/**
	 * Constructs a repository layer for Reservation entities using MongoDB database. 
	 * The construction generates and configures a collection for using by the repository.
	 * 
	 * @param mongoClient	the {@code MongoClient} used to retrieve the collection.
	 */
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
	public Optional<Client> findById(UUID id) {
		// TODO Auto-generated method stub
		return Optional.empty();
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

	/**
	 * Insert a new reservation in the MongoDB database or saves changes of an existing one.
	 * Note: a Reservation without an identifier is considered to be entered,
	 * while with the identifier it will be updated.
	 *
	 * @param reservation								the reservation to save.
	 * @return											the {@code Reservation} saved.
	 * @throws IllegalArgumentException					if {@code reservation} is null.
	 * @throws NotNullConstraintViolationException		if {@code date} or {@code clientId}
	 * 													of {@code reservation} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code date}
	 * 													of {@code reservation} to save are already present.
	 */
	@Override
	public Reservation save(Reservation reservation) throws IllegalArgumentException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to save cannot be null.");
		
		if (reservation.getClientId() == null)
			throw new NotNullConstraintViolationException(
					"Reservation to save must have a not-null client.");
		if (reservation.getDate() == null)
			throw new NotNullConstraintViolationException(
					"Reservation to save must have a not-null date.");
		
		if (reservation.getId() == null) {
			reservation.setId(UUID.randomUUID());
			try {
				collection.insertOne(reservation);
			} catch(MongoWriteException e) {
				throw new UniquenessConstraintViolationException(
						"The insertion violates uniqueness constraints.");
			}
		} else {
			try {
				collection.replaceOne(Filters.eq(ID_DB, reservation.getId()), reservation);
			} catch(MongoWriteException e) {
				throw new UniquenessConstraintViolationException(
						"The update violates uniqueness constraints.");
			}
		}
		return reservation;
	}

	/**
	 * Removes the unique specified reservation from the MongoDB database.
	 *
	 * @param reservation				the reservation to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	@Override
	public void delete(Reservation reservation) throws IllegalArgumentException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete cannot be null.");
		
		collection.deleteOne(Filters.eq(ID_DB, reservation.getId()));
	}

}
