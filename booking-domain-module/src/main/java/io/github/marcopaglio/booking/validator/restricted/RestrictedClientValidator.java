package io.github.marcopaglio.booking.validator.restricted;

import java.util.regex.Pattern;

import io.github.marcopaglio.booking.validator.ClientValidator;

/**
 * An implementation of validator for client entities that verifies if parameters are not null
 * or empty string neither they contain non-alphabetic characters, and fixes them if possible.
 * 
 * @see <a href="../../model/Client.html">Client</a>
 */
public class RestrictedClientValidator implements ClientValidator {
	/**
	 * Regular expression for stating other characters except the alphabetic ones and horizontal spaces.
	 */
	private static final Pattern notOnlyAlphabetic = Pattern.compile("[^\\p{IsAlphabetic}\\h]");

	/**
	 * Checks if firstName is valid as name for a client entity, and returns
	 * a fixed and valid alternative for it.
	 * 
	 * @param firstName					the string parameter to evaluate.
	 * @return							a fixed and valid {@code String} of {@code firstName}.
	 * @throws IllegalArgumentException	if {@code firstName} is null or not valid.
	 */
	@Override
	public String validateFirstName(String firstName) throws IllegalArgumentException {
		return checkAndFixNameValidity(firstName, "name");
	}

	/**
	 * Checks if lastName is valid as surname for a client entity, and returns
	 * a fixed and valid alternative for it.
	 * 
	 * @param lastName					the string parameter to evaluate.
	 * @return							a fixed and valid {@code String} of {@code lastName}.
	 * @throws IllegalArgumentException	if {@code lastName} is null or not valid.
	 */
	@Override
	public String validateLastName(String lastName) throws IllegalArgumentException {
		return checkAndFixNameValidity(lastName, "surname");
	}

	/**
	 * Checks if name is valid as name/surname for a client entity, and returns
	 * a fixed and valid alternative for it.
	 * 
	 * @param name						the string parameter to evaluate.
	 * @param inputName					the role of {@code name} in the client's context.
	 * @return							a fixed and valid {@code String} of {@code name}.
	 * @throws IllegalArgumentException	if {@code name} is null or not valid.
	 */
	private String checkAndFixNameValidity(String name, String inputName)
			throws IllegalArgumentException {
		checkNameValidity(name, inputName);
		return removeExcessedSpaces(name);
	}

	/**
	 * Checks if the string is a valid name that is not null neither empty,
	 * and contains only alphabetic and horizontal whitespace characters.
	 *
	 * @param name						the string to evaluate.
	 * @param inputName					the role of the string in the client's context.
	 * @throws IllegalArgumentException	if {@code name} is a null or empty string,
	 * 									or it contains non-alphabetic characters.
	 */
	private void checkNameValidity(String name, String inputName) throws IllegalArgumentException {
		checkNotNull(name, inputName);
		checkNotEmpty(name, inputName);
		checkOnlyAlphabetic(name, inputName);
	}

	/**
	 * Checks if the object is not null.
	 *
	 * @param o							the object to evaluate.
	 * @param inputName					the role of the object in the client's context.
	 * @throws IllegalArgumentException	if {@code o} is null.
	 */
	private void checkNotNull(Object o, String inputName) throws IllegalArgumentException {
		if (o == null)
			throw new IllegalArgumentException(
				"Client needs a not null " + inputName + ".");
	}

	/**
	 * Checks if the string is not empty.
	 *
	 * @param str						the string to evaluate.
	 * @param inputName					the role of the string in the client's context.
	 * @throws IllegalArgumentException	if {@code str} is empty.
	 */
	private void checkNotEmpty(String str, String inputName) {
		if (str.trim().isEmpty()) 
			throw new IllegalArgumentException(
				"Client needs a non-empty " + inputName + ".");
	}

	/**
	 * Checks if the string contains only accepted characters that is
	 * (lower and upper) alphabetic and accented letters, and horizontal whitespace character.
	 *
	 * @param str						the string to evaluate.
	 * @param inputName					the role of the string in the client's context.
	 * @throws IllegalArgumentException	if {@code str} contains non-valid characters.
	 */
	private void checkOnlyAlphabetic(String str, String inputName)
			throws IllegalArgumentException {
		if (notOnlyAlphabetic.matcher(str).find())
			throw new IllegalArgumentException(
				"Client's " + inputName + " must contain only alphabet letters.");
	}

	/**
	 * Removes side spaces and reduces multiple spaces into a single whitespace.
	 *
	 * @param name	the name to modify.
	 * @return		the {@code String} name modified.
	 */
	private String removeExcessedSpaces(String name) {
		return name.trim().replaceAll("\\s+", " ");
	}
}
