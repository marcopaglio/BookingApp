package io.github.marcopaglio.booking.model;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Contains the necessary structure of a generic entity.
 */
@MappedSuperclass
public abstract class BaseEntity {
	/**
	 * Field name used in a database to access the {@code id} attribute.
	 */
	public static final String ID_DB = "_id";

	/**
	 * The identifier of the entity.
	 */
	@Id
	@GeneratedValue
	@UuidGenerator
	@BsonId
	@BsonProperty(value = ID_DB)
	private UUID id;

	/**
	 * Empty constructor.
	 */
	protected BaseEntity() {
		super();
	}

	/**
	 * Retrieves the identifier of the entity. Note: UUID Objects are immutable.
	 *
	 * @return	the {@code UUID} of the entity.
	 */
	public final UUID getId() {
		return this.id;
	}

	/**
	 * Sets the identifier of the entity.
	 * 
	 * @param id	the identifier to set.
	 */
	public final void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Enforces overriding in entity classes based on equality.
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Enforces overriding in entity classes based on equality.
	 */
	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Enforces overriding in entity classes based on descriptiveness.
	 */
	@Override
	public abstract String toString();
}
