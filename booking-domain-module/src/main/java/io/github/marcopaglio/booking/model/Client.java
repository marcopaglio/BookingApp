package io.github.marcopaglio.booking.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class Client {
	private UUID uuid;
	private String firstName;
	private String lastName;
	private Collection<Reservation> reservations;

	public Client(String firstName, String lastName) {
		this.firstName = checkAndAdjust(firstName, "first name");
		this.lastName = checkAndAdjust(lastName, "last name");
		
		this.uuid = UUID.randomUUID();
		this.reservations = new ArrayList<Reservation>(); // TODO: arrayList o altro?
	}

	private String checkAndAdjust(String name, String input) throws IllegalArgumentException {
		if (name.trim().isEmpty()) 
			throw new IllegalArgumentException("Client needs a non-empty " + input + ".");
		if (name.matches(".*[^\\p{IsAlphabetic}\\h].*"))
			throw new IllegalArgumentException(
				"Client's " + input + " must contain only alphabet letters.");
		return name.replaceAll("\\s+", " ");
	}
	
	public final String getFirstName() {
		return this.firstName;
	}

	public final String getLastName() {
		return this.lastName;
	}

	public final Collection<Reservation> getReservations() {
		return this.reservations;
	}

	public UUID getUUID() {
		return this.uuid;
	}
}
