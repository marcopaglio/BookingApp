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
	 * Field name used in a MongoDB database to access the {@code id} attribute.
	 */
	public static final String ID_MONGODB = "_id";

	/**
	 * Field name used in a PostgreSQL database to access the {@code id} attribute.
	 */
	public static final String ID_POSTGRESQL = "id";

	/**
	 * The identifier of the entity.
	 */
	@Id
	@GeneratedValue
	@UuidGenerator
	@BsonId
	@BsonProperty(value = ID_MONGODB)
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
	public UUID getId() {
		return this.id;
	}

	/**
	 * Sets the identifier of the entity.
	 * 
	 * @param id	the identifier to set.
	 */
	public void setId(UUID id) {
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
