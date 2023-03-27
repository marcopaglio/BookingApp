package io.github.marcopaglio.booking.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import io.github.marcopaglio.booking.annotation.Generated;

/**
 * This entity represents the reservation's model of the booking application.
 */
public class Reservation {
	/**
	 * Regular expression for stating other characters except the numeric and the dash ones.
	 */
	private static final Pattern notOnlyNumeric =
			Pattern.compile("[^\\d\\-]");

	/**
	 * The identifier of the associated client entity.
	 */
	private final UUID clientUUID;

	/**
	 * The date of the reservation. Note: {@code date} is unique among reservation entities.
	 */
	private final LocalDate date;

	/**
	 * Constructs (without conversions) a reservation for the booking application
	 * from the associated client's identifier and a date.
	 * The constructor checks if the parameters are valid for the creation of the reservation.
	 * 
	 * @param clientUUID				the identifier of the associated client of the reservation.
	 * @param date						the date of the reservation.
	 * @throws IllegalArgumentException	if at least one of the argument is null.
	 */
	public Reservation(UUID clientUUID, LocalDate date) throws IllegalArgumentException {
		checkNotNull(clientUUID, "client identifier");
		checkNotNull(date, "date");
		
		this.clientUUID = clientUUID;
		this.date = date;
	}

	/**
	 * Constructs (with conversions) a reservation for the booking application
	 * from the associated client and a string date.
	 * The constructor checks if the parameters are valid for the creation of the reservation.
	 * 
	 * @param client					the associated client of the reservation.
	 * @param stringDate				the string date of the reservation.
	 * @throws IllegalArgumentException	if at least one of the argument is null
	 * 									or {@code stringDate} contains non-valid characters.
	 * @throws NumberFormatException	if {@code stringDate}'s format is not valid.
	 */
	public Reservation(Client client, String stringDate)
			throws IllegalArgumentException, NumberFormatException {
		checkNotNull(client, "client");
		checkDateValidity(stringDate, "date");
		
		this.clientUUID = client.getUuid();
		this.date = LocalDate.parse(stringDate);
	}

	/**
	 * Creates a copy of a reservation.
	 * 
	 * @param reservationToCopy			the reservation to copy.
	 * @throws IllegalArgumentException	if {@code reservationToCopy} is null.
	 */
	Reservation(Reservation reservationToCopy) throws IllegalArgumentException {
		checkNotNull(reservationToCopy, "reservation to copy");
		this.clientUUID = reservationToCopy.getClientUUID();
		this.date = reservationToCopy.getDate();
	}

	/**
	 * Checks if the string is a valid date that is not null, and contains
	 * only numeric and dash characters in the right format.
	 *
	 * @param stringDate				the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws IllegalArgumentException	if {@code stringDate} is a null string
	 * 									or contains non-numeric characters.
	 * @throws NumberFormatException	if {@code stringDate} has a wrong format.
	 */
	private static void checkDateValidity(String stringDate, String inputName)
			throws IllegalArgumentException, NumberFormatException {
		checkNotNull(stringDate, inputName);
		checkOnlyNumeric(stringDate, inputName);
		checkDateFormat(stringDate, inputName);
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
	 * @param str						the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws IllegalArgumentException	if {@code str} contains non-valid characters.
	 */
	private static void checkOnlyNumeric(String str, String inputName) throws IllegalArgumentException {
		if (notOnlyNumeric.matcher(str).find())
			throw new IllegalArgumentException(
				"Reservation needs a only numeric " + inputName + ".");
	}

	/**
	 * Check if the string has the format aaaa-mm-dd
	 *
	 * @param str						the string to evaluate.
	 * @param inputName					the role of the string in the reservation's context.
	 * @throws NumberFormatException	if {@code str} has a different format from aaaa-mm-gg.
	 */
	private static void checkDateFormat(String str, String inputName) throws NumberFormatException {
		String[] arrOfStr = str.split("-");
		if (arrOfStr.length != 3
			|| arrOfStr[0].length() != 4
			|| arrOfStr[1].length() != 2
			|| arrOfStr[2].length() != 2)
			throw new NumberFormatException(
				"Reservation needs a " + inputName + " in format aaaa-mm-dd.");
	}

	/**
	 * Retrieves the identifier of the associated client of the reservation.
	 * Note: UUID Objects are immutable.
	 *
	 * @return	the {@code clientUUID} of the associated client of the reservation.
	 */
	@Generated
	public final UUID getClientUUID() {
		return clientUUID;
	}

	/**
	 * Retrieves the date of the reservation. Note: LocalDate Objects are immutable.
	 *
	 * @return	the {@code date} of the reservation.
	 */
	@Generated
	public final LocalDate getDate() {
		return date;
	}

	/**
	 * Overridden method for returning a hash code value for the reservation object.
	 * 
	 * @return	a hash code value for this reservation object.
	 */
	@Generated
	@Override
	public int hashCode() {
		return Objects.hash(date);
	}

	/**
	 * Overridden method for indicating whether some other reservation object is "equal to" this one.
	 * Two reservation objects are equal if they have the date.
	 * 
	 * @param obj	the reference reservation object with which to compare.
	 * @return		{@code true} if this object is the same as the {@code obj} argument;
	 * 				{@code false} otherwise.
	 */
	@Generated
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Reservation other = (Reservation) obj;
		return Objects.equals(date, other.date);
	}

	/**
	 * Overridden method for returning a string representation of the reservation. 
	 *
	 * @return	a string representation of the reservation.
	 */
	@Generated
	@Override
	public String toString() {
		return "Reservation [date=" + date + "]";
	}
}
