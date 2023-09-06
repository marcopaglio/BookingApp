package io.github.marcopaglio.booking.validator.restricted;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

import io.github.marcopaglio.booking.validator.ReservationValidator;

/**
 * An implementation of validator for reservation entities that verifies if parameters are not
 * null neither date string contains non-valid characters, or are in a non-valid format
 * or out of range.
 * 
 * @see <a href="../model/Reservation.html">Reservation</a>
 */
public class RestrictedReservationValidator implements ReservationValidator{
	/**
	 * Regular expression for stating other characters except the numeric and the dash ones.
	 */
	private static final Pattern notOnlyNumeric = Pattern.compile("[^\\d\\-]");

	/**
	 * Checks if clientId is a not null identifier for the creation of a reservation entity,
	 * and returns it.
	 * 
	 * @param clientId					the associated client identifier to evaluate.
	 * @return							a valid {@code UUID} of {@code clientId}.
	 * @throws IllegalArgumentException	if {@code clientId} is null.
	 */
	@Override
	public UUID validateClientId(UUID clientId) throws IllegalArgumentException {
		checkNotNull(clientId, "client identifier");
		return clientId;
	}

	/**
	 * Checks if stringDate is valid as date for a reservation entity, and returns it.
	 * 
	 * @param stringDate				the string date to evaluate.
	 * @return							a valid {@code LocalDate} of {@code date}.
	 * @throws IllegalArgumentException	if {@code stringDate} is null,
	 * 									or it contains non-valid characters,
	 * 									or its format is not valid,
	 * 									or it is out of range.
	 */
	@Override
	public LocalDate validateDate(String stringDate) throws IllegalArgumentException {
		checkDateValidity(stringDate, "date");
		return LocalDate.parse(stringDate);
	}

	/**
	 * Checks if the string is a valid date that is not null, and contains
	 * only numeric and dash characters in the right format.
	 *
	 * @param stringDate				the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws IllegalArgumentException	if {@code stringDate} is a null string
	 * 									or {@code stringDate} contains non-numeric characters
	 * 									or {@code stringDate} has a wrong format
	 * 									or {@code stringDate} is out of range.
	 */
	private void checkDateValidity(String stringDate, String inputName)
			throws IllegalArgumentException {
		checkNotNull(stringDate, inputName);
		checkOnlyNumeric(stringDate, inputName);
		checkDateFormat(stringDate, inputName);
		checkDateInRange(stringDate);
	}

	/**
	 * Checks if the object is not null.
	 *
	 * @param o							the object to evaluate.
	 * @param inputName					the role of the object in the reservation's context.
	 * @throws IllegalArgumentException	if {@code o} is null.
	 */
	private void checkNotNull(Object o, String inputName) throws IllegalArgumentException {
		if (o == null)
			throw new IllegalArgumentException(
				"Reservation needs a not null " + inputName + ".");
	}

	/**
	 * Checks if the string contains only accepted characters that is
	 * numeric digits and dash separators {@code -}.
	 *
	 * @param stringDate				the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws IllegalArgumentException	if {@code stringDate} contains non-valid characters.
	 */
	private void checkOnlyNumeric(String stringDate, String inputName)
			throws IllegalArgumentException {
		if (notOnlyNumeric.matcher(stringDate).find())
			throw new IllegalArgumentException(
				"Reservation needs a only numeric " + inputName + ".");
	}

	/**
	 * Check if the string has the format aaaa-mm-dd.
	 *
	 * @param stringDate				the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws IllegalArgumentException	if {@code stringDate} has a different format from aaaa-mm-gg.
	 */
	private void checkDateFormat(String stringDate, String inputName)
			throws IllegalArgumentException {
		String[] arrOfStr = stringDate.split("-");
		if (arrOfStr.length != 3
			|| arrOfStr[0].length() != 4
			|| arrOfStr[1].length() != 2
			|| arrOfStr[2].length() != 2)
			throw new IllegalArgumentException(
				"Reservation needs a " + inputName + " in format aaaa-mm-dd.");
	}

	/**
	 * Check if the string has data values in the ranges.
	 *
	 * @param stringDate				the date string to evaluate.
	 * @throws IllegalArgumentException	if {@code stringDate} is out of ranges.
	 */
	private void checkDateInRange(String stringDate) throws IllegalArgumentException {
		try {
			LocalDate.parse(stringDate);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
