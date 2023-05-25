package io.github.marcopaglio.booking.validator;

import java.util.regex.Pattern;

import io.github.marcopaglio.booking.model.Client;

/**
 * A validator for client entities that verifies if parameters are not null or empty string
 * neither they contain non-alphabetic characters,
 * and fixes them if possible.
 *  * A validator for client entities that verifies if parameters are valid.
 */
public final class RestrictedClientValidator {
	/**
	 * Regular expression for stating other characters except the alphabetic ones and horizontal spaces.
	 */
	private static final Pattern notOnlyAlphabetic = Pattern.compile("[^\\p{IsAlphabetic}\\h]");

	/**
	 * Private constructors provided for avoiding the creation of this only-static-method class.
	 */
	private RestrictedClientValidator() {}

	/**
	 * Checks if parameters are valid for the creation of a client entity for the booking application,
	 * then creates it.
	 * 
	 * @param firstName					the name of the client.
	 * @param lastName					the surname of the client.
	 * @throws IllegalArgumentException	if at least one of the argument is null or not valid.
	 * @return							a valid {@code Client} entity.
	 */
	public static Client newValidatedClient(String firstName, String lastName)
			throws IllegalArgumentException {
		checkNameValidity(firstName, "name");
		checkNameValidity(lastName, "surname");
		
		return new Client(removeExcessedSpaces(firstName), removeExcessedSpaces(lastName));
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
	private static void checkNameValidity(String name, String inputName) throws IllegalArgumentException {
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
	private static void checkNotNull(Object o, String inputName) throws IllegalArgumentException {
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
	private static void checkNotEmpty(String str, String inputName) {
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
	private static void checkOnlyAlphabetic(String str, String inputName)
			throws IllegalArgumentException {
		if (notOnlyAlphabetic.matcher(str).find())
			throw new IllegalArgumentException(
				"Client's " + inputName + " must contain only alphabet letters.");
	}

	/**
	 * Removes side spaces and reduces multiple spaces into a single whitespace.
	 */
	private static String removeExcessedSpaces(String name) {
		return name.trim().replaceAll("\\s+", " ");
	}
}
