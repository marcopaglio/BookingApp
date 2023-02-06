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
		if (firstName.trim().isEmpty()) 
			throw new IllegalArgumentException("Client needs a non-empty first name.");
		if (firstName.matches(".*\\d.*"))
			throw new IllegalArgumentException("Client's first name must contain only alphabet letters.");
		this.firstName = firstName;
		
		if (lastName.trim().isEmpty()) 
			throw new IllegalArgumentException("Client needs a non-empty last name.");
		if (lastName.matches(".*\\d.*"))
			throw new IllegalArgumentException("Client's last name must contain only alphabet letters.");
		this.lastName = lastName;
		
		this.uuid = UUID.randomUUID();
		this.reservations = new ArrayList<Reservation>(); // TODO: arrayList o altro?
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
