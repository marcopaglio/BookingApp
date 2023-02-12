package io.github.marcopaglio.booking.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class Client {
	private final UUID uuid;
	private final String firstName;
	private final String lastName;
	private Collection<Reservation> reservations;

	public Client(String firstName, String lastName) {
		checkNameValidity(firstName, "first name");
		checkNameValidity(lastName, "last name");
		
		this.firstName = removeExcessedSpaces(firstName);
		this.lastName = removeExcessedSpaces(lastName);
		
		this.uuid = UUID.randomUUID();
		this.reservations = new ArrayList<Reservation>();
	}

	private static void checkNameValidity(String name, String inputName)
			throws IllegalArgumentException {
		checkNotNull(name, inputName);
		checkNotEmpty(name, inputName);
		checkOnlyAlphabetic(name, inputName);
	}
	
	/*
	 * Check if o is null
	 */
	private static void checkNotNull(Object o, String inputName)
			throws IllegalArgumentException {
		if (o == null)
			throw new IllegalArgumentException(
				"Client needs a not null " + inputName + ".");
	}
	
	/*
	 * Check if str is empty
	 */
	private static void checkNotEmpty(String str, String inputName)
			throws IllegalArgumentException {
		if (str.trim().isEmpty()) 
			throw new IllegalArgumentException(
				"Client needs a non-empty " + inputName + ".");
	}
	
	/*
	 * Check if str contains non-valid characters
	 * Accepted characters:
	 * 		- (lower and upper) alphabetic letters
	 * 		- accented letters
	 * 		- horizontal whitespace character
	 */
	private static void checkOnlyAlphabetic(String str, String inputName)
			throws IllegalArgumentException {
		if (str.matches(".*[^\\p{IsAlphabetic}\\h].*"))
			throw new IllegalArgumentException(
				"Client's " + inputName + " must contain only alphabet letters.");
	}

	static private String removeExcessedSpaces(String name) {
		return name.replaceAll("\\s+", " ");//.trim();
	}

	/*
	 * Java String Objects are immutable
	 */
	public final String getFirstName() {
		return this.firstName;
	}

	/*
	 * Java String Objects are immutable
	 */
	public final String getLastName() {
		return this.lastName;
	}

	/*
	 * UUID Objects are immutable
	 */
	public final UUID getUUID() {
		return this.uuid;
	}

	/*
	 * Java Collection Objects are mutable
	 * In order to protect reservations
	 * A defensive copy is returned
	 */
	public final Collection<Reservation> getReservations() {
		return new ArrayList<Reservation>(this.reservations);
	}
	
	/*
	 * Only for test purposes
	 */
	void setReservations(Collection<Reservation> reservations) {
		this.reservations = reservations;
	}

	public void addReservation(Reservation reservation) {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to add can't be null.");
		if (this.reservations.contains(reservation))
			throw new IllegalArgumentException(
					reservation.toString() + " to add is already in " + this.toString() + "'s list.");
		this.reservations.add(reservation);
	}
	
	public void removeReservation(Reservation reservation) {
		if (reservation == null)
			throw new IllegalArgumentException("Reservation to delete can't be null.");
		if (!this.reservations.contains(reservation))
			throw new NoSuchElementException(
					reservation.toString() + " to delete is not in " + this.toString() + "'s list.");
		this.reservations.remove(reservation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName);
	}

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
