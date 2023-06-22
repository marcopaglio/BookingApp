package io.github.marcopaglio.booking.model;

import java.util.UUID;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * Contains the necessary structure of a generic entity.
 */
public abstract class Entity {
	public static final String ID_DB = "_id";
	/**
	 * The identifier of the entity.
	 */
	@BsonId
	@BsonProperty(value = ID_DB)
	private UUID id;

	/**
	 * Default constructor.
	 */
	protected Entity() {
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
