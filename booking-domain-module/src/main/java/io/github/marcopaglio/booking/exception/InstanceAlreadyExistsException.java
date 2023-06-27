package io.github.marcopaglio.booking.exception;

/**
 * Thrown when you try to create or insert an already existing entity in the repository.
 * This exception is a {@code RuntimeException} version of
 * {@code javax.management.InstanceAlreadyExistsException}.
 * 
 * @see 
 *   <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/InstanceAlreadyExistsException.html">
 *     javax.management.InstanceAlreadyExistsException
 *   </a>
 */
public class InstanceAlreadyExistsException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public InstanceAlreadyExistsException() {
		super();
	}

	/**
	 * Constructs a {@code InstanceAlreadyExistsException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public InstanceAlreadyExistsException(String errorMessage) {
		super(errorMessage);
	}
}
