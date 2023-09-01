package io.github.marcopaglio.booking.exception;

/**
 * Thrown when you try to update an entity that does not exist in the repository.
 * This exception is a {@code RuntimeException}.
 */
public class UpdateFailureException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public UpdateFailureException() {
		super();
	}

	/**
	 * Constructs a {@code UpdateFailureException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public UpdateFailureException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a {@code UpdateFailureException} with the specified detail message
	 * and the cause.
	 *
	 * @param message	specifies the error message.
	 * @param cause		the cause of the exception.
	 */
	public UpdateFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
