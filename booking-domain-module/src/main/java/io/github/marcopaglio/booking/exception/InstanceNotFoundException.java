package io.github.marcopaglio.booking.exception;

/**
 * Thrown when you try to update or remove an entity that does not exist in the repository.
 * This exception is a {@code RuntimeException} version of
 * {@code javax.management.InstanceNotFoundException}.
 * 
 * @see 
 *   <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/javax/management/InstanceNotFoundException.html">
 *     javax.management.InstanceNotFoundException
 *   </a>
 */
public class InstanceNotFoundException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public InstanceNotFoundException() {
		super();
	}

	/**
	 * Constructs a {@code InstanceNotFoundException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public InstanceNotFoundException(String errorMessage) {
		super(errorMessage);
	}
}
