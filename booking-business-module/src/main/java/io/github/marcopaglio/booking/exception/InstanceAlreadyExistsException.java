package io.github.marcopaglio.booking.exception;

/**
 * Thrown when you try to create or insert an already existing object in the database.
 */
public class InstanceAlreadyExistsException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an {@code InstanceAlreadyExistsException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public InstanceAlreadyExistsException(String errorMessage) {
		super(errorMessage);
	}
}
