package io.github.marcopaglio.booking.exception;

/**
 * Thrown when a generic database transaction fails.
 */
public class TransactionException extends RuntimeException {

	/**
	 * Version number used during deserialization to verify that the sender
	 * and receiver of a serialized object have loaded classes for that object
	 * that are compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public TransactionException() {
		super();
	}

	/**
	 * Constructs a {@code TransactionException} with the specified detail message.
	 * 
	 * @param errorMessage	specifies the error message.
	 */
	public TransactionException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a {@code TransactionException} with the specified detail message and the cause.
	 *
	 * @param message	specifies the error message.
	 * @param cause		the cause of the exception.
	 */
	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
