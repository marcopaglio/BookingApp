package io.github.marcopaglio.booking.exception;

/**
 * Thrown when you try to insert an entity that violates uniqueness constraints in the repository.
 * This exception is a {@code RuntimeException}.
 */
public class UniquenessConstraintViolationException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UniquenessConstraintViolationException() {
		super();
	}

	/**
	 * Constructs an {@code UniquenessConstraintViolationException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public UniquenessConstraintViolationException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a {@code UniquenessConstraintViolationException} with the specified detail
	 * message and the cause.
	 *
	 * @param message	specifies the error message.
	 * @param cause		the cause of the exception.
	 */
	public UniquenessConstraintViolationException(String message, Throwable cause) {
		super(message, cause);
	}
}
