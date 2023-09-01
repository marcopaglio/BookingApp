package io.github.marcopaglio.booking.exception;

/**
 * Thrown when there is a failure in querying the database.
 */
public class DatabaseException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public DatabaseException() {
		super();
	}

	/**
	 * Constructs a {@code DatabaseException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public DatabaseException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a {@code DatabaseException} with the specified detail message and the cause.
	 *
	 * @param message	specifies the error message.
	 * @param cause		the cause of the exception.
	 */
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}