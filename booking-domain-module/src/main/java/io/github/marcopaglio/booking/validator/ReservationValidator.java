package io.github.marcopaglio.booking.validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

/**
 * A validator for reservation entities that verifies if parameters are valid.
 */
public final class ReservationValidator {
	/**
	 * Regular expression for stating other characters except the numeric and the dash ones.
	 */
	private static final Pattern notOnlyNumeric = Pattern.compile("[^\\d\\-]");

	private ReservationValidator() {}

	/**
	 * Checks if parameters are valid for the creation of a reservation entity for the
	 * booking application, then creates it.
	 * 
	 * @param client					the associated client of the reservation.
	 * @param stringDate				the string date of the reservation.
	 * @throws IllegalArgumentException	if at least one of the argument is null
	 * 									or {@code stringDate} contains non-valid characters
	 * 									or {@code stringDate}'s format is not valid.
	 * 									or {@code stringDate} is out of range.
	 * @return							a valid {@code Reservation} entity.
	 */
	public static Reservation newValidatedReservation(Client client, String stringDate)
			throws IllegalArgumentException {
		checkNotNull(client, "client");
		checkNotNull(client.getUuid(), "client identifier");
		checkDateValidity(stringDate, "date");
		
		return new Reservation(client.getUuid(), LocalDate.parse(stringDate));
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
	private static void checkDateValidity(String stringDate, String inputName)
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
	private static void checkNotNull(Object o, String inputName) throws IllegalArgumentException {
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
	private static void checkOnlyNumeric(String stringDate, String inputName)
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
	private static void checkDateFormat(String stringDate, String inputName)
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
	private static void checkDateInRange(String stringDate) throws IllegalArgumentException {
		try {
			LocalDate.parse(stringDate);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
