package io.github.marcopaglio.booking.model;

import java.time.LocalDate;
import java.util.UUID;

public class Reservation {
	private UUID clientUUID;
	private LocalDate date;

	public Reservation(UUID clientUUID, LocalDate date) {
		this.clientUUID = clientUUID;
		this.date = date;
	}
	
	public Reservation(Client client, String stringDate) {
		this.clientUUID = client.getUUID();
		this.date = LocalDate.parse(stringDate);
	}

	public UUID getClientUUID() {
		return clientUUID;
	}

	public LocalDate getDate() {
		return date;
	}
}
