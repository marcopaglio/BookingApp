package io.github.marcopaglio.booking.validator;

/**
 * Facade of validator for client entities.
 * 
 * @see <a href="../model/Client.html">Client</a>
 */
public interface ClientValidator {

	/**
	 * Checks if firstName is valid as name for a client entity.
	 * 
	 * @param firstName					the string parameter to evaluate.
	 * @return							a valid {@code String} of {@code firstName}.
	 * @throws IllegalArgumentException	if {@code firstName} is not valid.
	 */
	public String validateFirstName(String firstName) throws IllegalArgumentException;

	/**
	 * Checks if lastName is valid as surname for a client entity.
	 * 
	 * @param lastName					the string parameter to evaluate.
	 * @return							a valid {@code String} of {@code lastName}.
	 * @throws IllegalArgumentException	if {@code lastName} is not valid.
	 */
	public String validateLastName(String lastName) throws IllegalArgumentException;
}
