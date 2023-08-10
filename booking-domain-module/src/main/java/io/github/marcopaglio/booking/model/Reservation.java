package io.github.marcopaglio.booking.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * This entity represents the reservation's model of the booking application.
 */
@Entity
@Table(name = Reservation.RESERVATION_TABLE_DB)
public class Reservation extends BaseEntity {
	/**
	 * Table name used in a database to access {@code Reservation} entities.
	 */
	public static final String RESERVATION_TABLE_DB = "reservations";

	/**
	 * Field name used in a database to access the {@code clientId} attribute.
	 */
	public static final String CLIENTID_DB = "client";

	/**
	 * Field name used in a database to access the {@code date} attribute.
	 */
	public static final String DATE_DB = "date";

	/**
	 * The identifier of the associated client entity.
	 */
	@Column(name = CLIENTID_DB, nullable = false)
	@BsonProperty(value = CLIENTID_DB)
	private UUID clientId;

	/**
	 * The date of the reservation. Note: {@code date} is unique among reservation entities.
	 */
	@Column(name = DATE_DB, unique = true, nullable = false)
	@BsonProperty(value = DATE_DB)
	private LocalDate date;

	/**
	 * Constructs a reservation for the booking application
	 * from the associated client's identifier and a date.
	 * The constructor checks if the parameters are valid for the creation of the reservation.
	 * 
	 * @param clientId	the identifier of the associated client of the reservation.
	 * @param date		the date of the reservation.
	 */
	public Reservation(UUID clientId, LocalDate date) {
		super();
		this.clientId = clientId;
		this.date = date;
	}

	/**
	 * Empty constructor needed for database purposes.
	 */
	protected Reservation() {
		super();
	}

	/**
	 * Retrieves the identifier of the associated client of the reservation.
	 * Note: UUID Objects are immutable.
	 *
	 * @return	the {@code UUID} of the associated client of the reservation.
	 */
	public final UUID getClientId() {
		return clientId;
	}

	/**
	 * Sets the client's identifier of the reservation.
	 * 
	 * @param clientId	the client's identifier to set.
	 */
	public final void setClientId(UUID clientId) {
		this.clientId = clientId;
	}

	/**
	 * Retrieves the date of the reservation. Note: LocalDate Objects are immutable.
	 *
	 * @return	the {@code date} of the reservation.
	 */
	public final LocalDate getDate() {
		return date;
	}

	/**
	 * Sets the date of the reservation.
	 * 
	 * @param date	the date to set.
	 */
	public final void setDate(LocalDate date) {
		this.date = date;
	}

	/**
	 * Overridden method for returning a hash code value for the reservation object.
	 * 
	 * @return	a hash code value for this reservation object.
	 */
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
	@Override
	public String toString() {
		return "Reservation [date=" + date + "]";
	}
}
