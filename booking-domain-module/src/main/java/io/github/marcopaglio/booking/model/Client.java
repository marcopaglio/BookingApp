package io.github.marcopaglio.booking.model;

import java.util.Objects;
import java.util.UUID;

/**
 * This entity represents the customer's model of the booking application.
 */
public class Client {
	/**
	 * The identifier of the client entity.
	 */
	private final UUID uuid;

	/**
	 * The name of the client entity.
	 * Note: the couple [{@code firstName}, {@code lastName}] is unique among client entities.
	 */
	private final String firstName;

	/**
	 * The surname of the client entity.
	 * Note: the couple [{@code firstName}, {@code lastName}] is unique in the client entity.
	 */
	private final String lastName;

	/**
	 * Constructs a client for the booking application with a name and a surname.
	 * 
	 * @param firstName					the name of the client.
	 * @param lastName					the surname of the client.
	 */
	public Client(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
		
		this.uuid = UUID.randomUUID();
	}

	/**
	 * Retrieves the name of the client. Note: Java String Objects are immutable.
	 *
	 * @return	the {@code firstName} of the client.
	 */
	public final String getFirstName() {
		return this.firstName;
	}

	/**
	 * Retrieves the surname of the client. Note: Java String Objects are immutable.
	 *
	 * @return	the {@code lastName} of the client.
	 */
	public final String getLastName() {
		return this.lastName;
	}

	/**
	 * Retrieves the identifier of the client. Note: UUID Objects are immutable.
	 *
	 * @return	the {@code uuid} of the client.
	 */
	public final UUID getUuid() {
		return this.uuid;
	}

	/**
	 * Overridden method for returning a hash code value for the client object.
	 * 
	 * @return	a hash code value for this client object.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName);
	}

	/**
	 * Overridden method for indicating whether some other client object is "equal to" this one.
	 * Two client objects are equal if they have both the same name and surname.
	 * 
	 * @param obj	the reference client object with which to compare.
	 * @return		{@code true} if this object is the same as the {@code obj} argument;
	 * 				{@code false} otherwise.
	 */
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

	/**
	 * Overridden method for returning a string representation of the client. 
	 *
	 * @return	a string representation of the client.
	 */
	@Override
	public String toString() {
		return "Client [" + firstName + " " + lastName + "]";
	}
}
