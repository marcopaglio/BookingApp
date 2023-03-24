package io.github.marcopaglio.booking.exception;

public class InstanceAlreadyExistsException extends RuntimeException {

	/**
	 * From Javadoc:
	 * The serialization runtime associates with each serializable class a version number,
	 * called a serialVersionUID, which is used during deserialization to verify that the
	 * sender and receiver of a serialized object have loaded classes for that object that
	 * are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Used for throw a new RuntimeException
	 */
	public InstanceAlreadyExistsException(String errorMessage) {
		super(errorMessage);
	}
}
