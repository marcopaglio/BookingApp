package io.github.marcopaglio.booking.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mongodb.MongoWriteException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import io.github.marcopaglio.booking.exception.UpdateFailureException;
import io.github.marcopaglio.booking.exception.NotNullConstraintViolationException;
import io.github.marcopaglio.booking.exception.UniquenessConstraintViolationException;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ReservationRepository;

import static io.github.marcopaglio.booking.model.BaseEntity.ID_MONGODB;
import static io.github.marcopaglio.booking.model.Reservation.DATE_DB;
import static io.github.marcopaglio.booking.model.Reservation.CLIENTID_DB;
import static io.github.marcopaglio.booking.model.Reservation.RESERVATION_TABLE_DB;

/**
 * Implementation of repository layer through MongoDB for Reservation entities of the booking application.
 */
public class ReservationMongoRepository extends MongoRepository<Reservation> implements ReservationRepository {
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(ReservationMongoRepository.class);

	/**
	 * Constructs a repository layer for Reservation entities using MongoDB database. 
	 * The construction generates and configures a collection for using by the repository.
	 * 
	 * @param client		the {@code MongoClient} used to retrieve the collection.
	 * @param session		the {@code ClientSession} used to communicate with MongoDB database.
	 * @param databaseName	the name of the database in which the repository works.
	 */
	public ReservationMongoRepository(MongoClient client, ClientSession session, String databaseName) {
		super(client
				.getDatabase(databaseName)
				.getCollection(RESERVATION_TABLE_DB, Reservation.class),
				session);
		
		// collection configuration
		collection.createIndex(session, Indexes.descending(DATE_DB), new IndexOptions().unique(true));
	}

	/**
	 * Retrieves all the reservations from the database in a list.
	 * 
	 * @return	the {@code List} of {@code Reservation}s found in the repository.
	 */
	@Override
	public List<Reservation> findAll() {
		return StreamSupport
				.stream(collection.find(session).spliterator(), false)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves all the reservations associated with the specified client's identifier
	 * from the MongoDB database in a list.
	 * 
	 * @param clientId	the identifier of the associated client.
	 * @return			the {@code List} of {@code Reservation}s associated
	 * 					with {@code clientId} found in the repository.
	 */
	@Override
	public List<Reservation> findByClient(UUID clientId) {
		return StreamSupport
				.stream(collection.find(session, Filters.eq(CLIENTID_DB, clientId)).spliterator(), false)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the unique reservation with the specified identifier from the MongoDB database,
	 * if it exists.
	 * 
	 * @param id	the identifier of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} identified by {@code id},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Reservation> findById(UUID id) {
		Reservation reservation = collection.find(session, Filters.eq(ID_MONGODB, id)).first();
		
		if (reservation != null)
			return Optional.of(reservation);
		return Optional.empty();
	}

	/**
	 * Retrieves the unique reservation of the specified date from the MongoDB database,
	 * if it exists.
	 * 
	 * @param date	the date of the reservation to find.
	 * @return		an {@code Optional} contained the {@code Reservation} on {@code date},
	 * 				if it exists; an {@code Optional} empty, otherwise.
	 */
	@Override
	public Optional<Reservation> findByDate(LocalDate date) {
		Reservation reservation = collection.find(session, Filters.eq(DATE_DB, date)).first();
		
		if (reservation != null)
			return Optional.of(reservation);
		return Optional.empty();
	}

	/**
	 * Insert a new reservation in the MongoDB database or saves changes of an existing one.
	 * Note: a Reservation without an identifier is considered to be entered,
	 * while with the identifier it will be updated.
	 *
	 * @param reservation								the reservation to save.
	 * @return											the {@code Reservation} saved.
	 * @throws IllegalArgumentException					if {@code reservation} is null.
	 * @throws UpdateFailureException					if you try to save changes of a no longer
	 * 													existing reservation.
	 * @throws NotNullConstraintViolationException		if {@code date} or {@code clientId}
	 * 													of {@code reservation} to save are null.
	 * @throws UniquenessConstraintViolationException	if {@code id} or {@code date}
	 * 													of {@code reservation} to save are already present.
	 */
	@Override
	public Reservation save(Reservation reservation) throws IllegalArgumentException, UpdateFailureException,
			NotNullConstraintViolationException, UniquenessConstraintViolationException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to save cannot be null.");
		
		if (reservation.getClientId() == null || reservation.getDate() == null)
			throw new NotNullConstraintViolationException(
					"Reservation to save violates not-null constraints.");
		
		try {
			if (reservation.getId() == null) {
				reservation.setId(UUID.randomUUID());
				collection.insertOne(session, reservation);
			} else {
				replaceIfFound(reservation);
			}
		} catch(MongoWriteException e) {
			LOGGER.warn(e.getMessage());
			throw new UniquenessConstraintViolationException(
					"Reservation to save violates uniqueness constraints.", e.getCause());
		}
		return reservation;
	}

	/**
	 * Replace the existing Reservation with the same id in the MongoDB database.
	 * 
	 * @param reservation				the replacement reservation.
	 * @throws UpdateFailureException	if there is no reservation with the same id to replace.
	 */
	private void replaceIfFound(Reservation reservation) throws UpdateFailureException {
		if (collection.replaceOne(
					session,
					Filters.eq(ID_MONGODB, reservation.getId()),
					reservation,
					new ReplaceOptions().upsert(false))
				.getModifiedCount() == 0)
			throw new UpdateFailureException(
					"Reservation to update is not longer present in the repository.");
	}

	/**
	 * Removes the unique specified reservation from the MongoDB database, if it exists,
	 * otherwise it does nothing.
	 *
	 * @param reservation				the reservation to delete.
	 * @throws IllegalArgumentException	if {@code reservation} is null.
	 */
	@Override
	public void delete(Reservation reservation) throws IllegalArgumentException {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete cannot be null.");
		
		if (reservation.getId() != null) {
			if(collection.deleteOne(session, Filters.eq(ID_MONGODB, reservation.getId()))
					.getDeletedCount() == 0)
				LOGGER.warn(() -> reservation.toString() +
						" has already been deleted from the database.");
		}
		else
			 LOGGER.warn(() -> reservation.toString() + " to delete was never been "
					+ "inserted into the database.");
	}

}
