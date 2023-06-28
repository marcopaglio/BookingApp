package io.github.marcopaglio.booking.model;

import java.util.Objects;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

import static org.bson.BsonType.STRING;

/**
 * This entity represents the customer's model of the booking application.
 */
public class Client extends Entity {
	/**
	 * Field name used in a database to access the {@code firstName} attribute.
	 */
	public static final String FIRSTNAME_DB = "name";

	/**
	 * Field name used in a database to access the {@code lastName} attribute.
	 */
	public static final String LASTNAME_DB = "surname";

	/**
	 * The name of the client entity.
	 * Note: the couple [{@code firstName}, {@code lastName}] is unique among client entities.
	 */
	@BsonProperty(value = FIRSTNAME_DB)
	@BsonRepresentation(value = STRING)
	private String firstName;

	/**
	 * The surname of the client entity.
	 * Note: the couple [{@code firstName}, {@code lastName}] is unique among client entities.
	 */
	@BsonProperty(value = LASTNAME_DB)
	@BsonRepresentation(value = STRING)
	private String lastName;

	/**
	 * Constructs a client for the booking application with a name, a surname and an identifier.
	 * 
	 * @param firstName	the name of the client.
	 * @param lastName	the surname of the client.
	 */
	@BsonCreator
	public Client(@BsonProperty(value = FIRSTNAME_DB) String firstName,
				@BsonProperty(value = LASTNAME_DB) String lastName) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	/**
	 * Empty constructor needed for database purposes.
	 */
	//protected Client() {}

	/**
	 * Retrieves the name of the client. Note: Java String Objects are immutable.
	 *
	 * @return	the {@code firstName} of the client.
	 */
	public final String getFirstName() {
		return this.firstName;
	}

	/**
	 * Sets the name of the client.
	 * 
	 * @param firstName	the name to set.
	 */
	public final void setFirstName(String firstName) {
		this.firstName = firstName;
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
	 * Sets the surname of the client.
	 * 
	 * @param lastName	the surname to set.
	 */
	public final void setLastName(String lastName) {
		this.lastName = lastName;
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
