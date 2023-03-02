package io.github.marcopaglio.booking.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import io.github.marcopaglio.booking.annotation.Generated;

public class Reservation {
	private static final Pattern notOnlyNumeric =
			Pattern.compile("[^\\d\\-]");
	
	private final UUID clientUUID;
	private final LocalDate date;

	public Reservation(UUID clientUUID, LocalDate date) {
		checkNotNull(clientUUID, "client uuid");
		checkNotNull(date, "date");
		
		this.clientUUID = clientUUID;
		this.date = date;
	}
	
	public Reservation(Client client, String stringDate) {
		checkNotNull(client, "client");
		checkDateValidity(stringDate, "date");
		
		this.clientUUID = client.getUuid();
		this.date = LocalDate.parse(stringDate);
	}

	Reservation(Reservation reservationToCopy) {
		checkNotNull(reservationToCopy, "reservation to copy");
		this.clientUUID = reservationToCopy.getClientUUID();
		this.date = reservationToCopy.getDate();
	}

	/*
	 * Check if stringDate is valid (not null, contains only numeric characters
	 * and in the right format)
	 * @throws IllegalArgumentException if stringDate is a null string,
	 *         or contains non-numeric characters.
	 * @throws NumberFormatException if stringDate has a wrong format.
	 */
	private static void checkDateValidity(String stringDate, String inputName) {
		checkNotNull(stringDate, inputName);
		checkOnlyNumeric(stringDate, inputName);
		checkDateFormat(stringDate, inputName);
	}
	
	/*
	 * Check if o is null
	 * @throws IllegalArgumentException if o is a null object
	 */
	private static void checkNotNull(Object o, String inputName) {
		if (o == null)
			throw new IllegalArgumentException(
				"Reservation needs a not null " + inputName + ".");
	}
	
	/*
	 * Check if str contains non-valid characters
	 * Accepted characters:
	 * - Numeric digits
	 * - Dash separator "-"
	 * @throws IllegalArgumentException if str contains non-valid characters.
	 */
	private static void checkOnlyNumeric(String str, String inputName) {
		if (notOnlyNumeric.matcher(str).find())
			throw new IllegalArgumentException(
				"Reservation needs a only numeric " + inputName + ".");
	}
	
	/*
	 * Check if str has a different format from aaaa-mm-dd
	 * @throws NumberFormatException if string has a format
	 *         different from aaaa-mm-gg.
	 */
	private static void checkDateFormat(String str, String inputName) {
		String[] arrOfStr = str.split("-");
		if (arrOfStr.length != 3
			|| arrOfStr[0].length() != 4
			|| arrOfStr[1].length() != 2
			|| arrOfStr[2].length() != 2)
			throw new NumberFormatException(
				"Reservation needs a " + inputName + " in format aaaa-mm-dd.");
	}

	/*
	 * UUID Objects are immutable
	 */
	@Generated
	public final UUID getClientUUID() {
		return clientUUID;
	}

	/*
	 * LocalDate Objects are immutable
	 */
	@Generated
	public final LocalDate getDate() {
		return date;
	}

	@Generated
	@Override
	public int hashCode() {
		return Objects.hash(date);
	}

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

	@Generated
	@Override
	public String toString() {
		return "Reservation [date=" + date + "]";
	}
}
