package io.github.marcopaglio.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.marcopaglio.booking.model.Reservation;

/*
 * Facade for repository layer for Reservation entity.
 */
public interface ReservationRepository {
	// TODO: Valutare se i sequenti metodi vengono utilizzati e se sono sufficienti o ne servono altri.

	/*
	 * Retrieve all Reservations from the repository in a List.
	 */
	public List<Reservation> findAll();
	
	/*
	 * Retrieve all the Reservations of Client having clientUUID from the repository in a List.
	 */
	public List<Reservation> findByClient(UUID clientUUID);

	/*
	 * Retrieve the unique Reservation in the given date.
	 */
	public Optional<Reservation> findByDate(LocalDate date);

	/*
	 * Insert a new Reservation in the repository.
	 */
	public Reservation save(Reservation reservation);

	/*
	 * Remove the unique Reservation in the given date from the repository.
	 */
	public void delete(LocalDate date);
}
