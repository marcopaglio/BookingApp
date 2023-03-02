package io.github.marcopaglio.booking.model;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.marcopaglio.booking.annotation.Generated;

public class Client {
	private static final Pattern notOnlyAlphabetic =
			Pattern.compile("[^\\p{IsAlphabetic}\\h]");
	
	private final UUID uuid;
	private final String firstName;
	private final String lastName;
	private List<Reservation> reservations;

	public Client(String firstName, String lastName, List<Reservation> reservations) {
		checkNameValidity(firstName, "first name");
		checkNameValidity(lastName, "last name");
		checkNotNull(reservations, "reservations' list");
		
		this.firstName = removeExcessedSpaces(firstName);
		this.lastName = removeExcessedSpaces(lastName);
		
		this.uuid = UUID.randomUUID();
		this.reservations = reservations.stream().map(Reservation::new).collect(Collectors.toList());
		
	}

	/*
	 * Check if str is valid (not null neither empty,
	 * and contains only alphabetic characters)
	 * @throws IllegalArgumentException if str is a null or empty string,
	 *         or it contains non-alphabetic characters.
	 */
	private static void checkNameValidity(String name, String inputName) {
		checkNotNull(name, inputName);
		checkNotEmpty(name, inputName);
		checkOnlyAlphabetic(name, inputName);
	}
	
	/*
	 * Check if o is null
	 * @throws IllegalArgumentException if o is a null object
	 */
	private static void checkNotNull(Object o, String inputName) {
		if (o == null)
			throw new IllegalArgumentException(
				"Client needs a not null " + inputName + ".");
	}
	
	/*
	 * Check if str is empty
	 * @throws IllegalArgumentException if str is empty
	 */
	private static void checkNotEmpty(String str, String inputName) {
		if (str.trim().isEmpty()) 
			throw new IllegalArgumentException(
				"Client needs a non-empty " + inputName + ".");
	}
	
	/*
	 * Check if str contains non-valid characters
	 * Accepted characters:
	 * - (lower and upper) alphabetic letters
	 * - accented letters
	 * - horizontal whitespace character
	 * @throws IllegalArgumentException if str contains non-valid characters.
	 */
	private static void checkOnlyAlphabetic(String str, String inputName) {
		if (notOnlyAlphabetic.matcher(str).find())
			throw new IllegalArgumentException(
				"Client's " + inputName + " must contain only alphabet letters.");
	}

	/*
	 * Remove side spaces and
	 * reduce multiple spaces into single spaces
	 */
	private static String removeExcessedSpaces(String name) {
		return name.trim().replaceAll("\\s+", " ");
	}

	/*
	 * Java String Objects are immutable
	 */
	@Generated
	public final String getFirstName() {
		return this.firstName;
	}

	/*
	 * Java String Objects are immutable
	 */
	@Generated
	public final String getLastName() {
		return this.lastName;
	}

	/*
	 * UUID Objects are immutable
	 */
	@Generated
	public final UUID getUuid() {
		return this.uuid;
	}

	/*
	 * Java Collection Objects are mutable
	 * In order to protect reservations
	 * A defensive copy is returned
	 */
	public final List<Reservation> getCopyOfReservations() {
		return this.reservations.stream().map(Reservation::new).collect(Collectors.toList());
	}

	/*
	 * Only for test purposes
	 */
	@Generated
	final List<Reservation> getReservations() {
		return reservations;
	}

	public final void addReservation(Reservation reservation) {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to add can't be null.");
		if (this.reservations.contains(reservation))
			throw new IllegalArgumentException(
					reservation.toString() + " to add is already in " + this.toString() + "'s list.");
		this.reservations.add(reservation);
	}
	
	public final void removeReservation(Reservation reservation) {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete can't be null.");
		if (!this.reservations.contains(reservation))
			throw new NoSuchElementException(
					reservation.toString() + " to delete is not in " + this.toString() + "'s list.");
		this.reservations.remove(reservation);
	}

	@Generated
	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName);
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
		Client other = (Client) obj;
		return Objects.equals(firstName, other.firstName)
			&& Objects.equals(lastName, other.lastName);
	}

	@Override
	public String toString() {
		return "Client [" + firstName + " " + lastName + "]";
	}
}
