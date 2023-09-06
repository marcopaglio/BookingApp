package io.github.marcopaglio.booking.validator;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Facade of validator for reservation entities.
 * 
 * @see <a href="../model/Reservation.html">Reservation</a>
 */
public interface ReservationValidator {

	/**
	 * Checks if clientId is valid as identifier of associated client for a reservation entity.
	 * 
	 * @param clientId					the associated client identifier to evaluate.
	 * @return							a valid {@code UUID} of {@code clientId}.
	 * @throws IllegalArgumentException	if {@code clientId} is not valid.
	 */
	public UUID validateClientId(UUID clientId) throws IllegalArgumentException;

	/**
	 * Checks if stringDate is valid as date for a reservation entity.
	 * 
	 * @param stringDate				the string date to evaluate.
	 * @return							a valid {@code LocalDate} of {@code date}.
	 * @throws IllegalArgumentException	if {@code stringDate} is not valid.
	 */
	public LocalDate validateDate(String stringDate) throws IllegalArgumentException;
}
