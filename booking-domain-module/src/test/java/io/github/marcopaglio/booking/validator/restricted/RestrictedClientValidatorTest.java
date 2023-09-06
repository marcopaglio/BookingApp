package io.github.marcopaglio.booking.validator.restricted;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests for RestrictedClientValidator class")
class RestrictedClientValidatorTest {

	private RestrictedClientValidator clientValidator;

	@BeforeEach
	void setUp() throws Exception {
		clientValidator = new RestrictedClientValidator();
	}

	@Nested
	@DisplayName("Tests for 'validateFirstName'")
	class ValidateFirstNameTest {
		private static final String VALID_FIRSTNAME = "Mario";

		@Nested
		@DisplayName("Valid names")
		class ValidNamesTest {

			@Test
			@DisplayName("Name is valid")
			void testValidateNameWhenNameIsValidShouldReturnTheSameName() {
				assertThat(clientValidator.validateFirstName(VALID_FIRSTNAME))
					.isEqualTo(VALID_FIRSTNAME);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Accented names")
			@ValueSource(strings = {
				"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
				"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
			})
			void testValidateNameWhenNameIsAccentedShouldNotThrow(String accentedFirstName) {
				assertThatNoException().isThrownBy(
						() -> clientValidator.validateFirstName(accentedFirstName));
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Names with spaces")
			@ValueSource(strings = {
				"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
				"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
			})
			void testValidateNameWhenNameContainsSpacesShouldNotThrow(String spacedFirstName) {
				assertThatNoException().isThrownBy(
						() -> clientValidator.validateFirstName(spacedFirstName));
			}
		}

		@Nested
		@DisplayName("Invalid names")
		class InvalidNamesTest {

			@Test
			@DisplayName("Null names")
			void testValidateNameWhenNameIsNullShouldThrow() {
				assertThatThrownBy(
						() -> clientValidator.validateFirstName(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a not null name.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Empty or only-spaces names")
			@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
			void testValidateNameWhenNameIsEmptyShouldThrow(String emptyFirstName) {
				assertThatThrownBy(
						() -> clientValidator.validateFirstName(emptyFirstName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty name.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-alphabet names")
			@ValueSource(strings = {"Mari0", "Ro55i", "Mario!", "Rossi@"})
			void testValidateNameWhenNameContainsNonAlphabetCharactersShouldThrow(
					String nonAlphabetFirstName) {
				assertThatThrownBy(
						() -> clientValidator.validateFirstName(nonAlphabetFirstName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's name must contain only alphabet letters.");
			}
		}

		@Nested
		@DisplayName("Names fixed")
		class NamesFixedTest {

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Side spaced names")
			@ValueSource(strings = {" Mario", "Mario\t", " Mario ", "\t Mario  "})
			void testValidateNameWhenNameContainsSideSpacesShouldRemove(
					String sideSpacedFirstName) {
				assertThat(clientValidator.validateFirstName(sideSpacedFirstName))
					.isEqualTo(VALID_FIRSTNAME);
			}

			@ParameterizedTest(name = "{index}: ''{0}'' => ''{1}''")
			@DisplayName("Several spaced names")
			@CsvSource({
				"'Maria  Luisa', 'Maria Luisa'",
				"'Mario  Maria  Mario', 'Mario Maria Mario'",
				"'Mario   Maria   Mario', 'Mario Maria Mario'"
			})
			void testValidateNameWhenNameContainsSeveralSpacesShouldReduce(
					String actualFirstName, String expectedFirstName) {
				assertThat(clientValidator.validateFirstName(actualFirstName))
					.isEqualTo(expectedFirstName);
			}
		}
	}

	@Nested
	@DisplayName("Tests for 'validateLastName'")
	class ValidateLastNameTest {
		private static final String VALID_LASTNAME = "Rossi";

		@Nested
		@DisplayName("Valid surnames")
		class ValidSurnamesTest {

			@Test
			@DisplayName("Surname is valid")
			void testValidateSurnameWhenSurnameIsValidShouldReturnTheSameSurname() {
				assertThat(clientValidator.validateLastName(VALID_LASTNAME))
					.isEqualTo(VALID_LASTNAME);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Accented surnames")
			@ValueSource(strings = {
				"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
				"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
			})
			void testValidateSurnameWhenSurnameIsAccentedShouldNotThrow(String accentedLastName) {
				assertThatNoException().isThrownBy(
						() -> clientValidator.validateLastName(accentedLastName));
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Surnames with spaces")
			@ValueSource(strings = {
				"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
				"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
			})
			void testValidateSurnameWhenSurnameContainsSpacesShouldNotThrow(
					String spacedLastName) {
				assertThatNoException().isThrownBy(
						() -> clientValidator.validateLastName(spacedLastName));
			}
		}

		@Nested
		@DisplayName("Invalid surnames")
		class InvalidSurnamesTest {

			@Test
			@DisplayName("Null surnames")
			void testValidateSurnameWhenSurnameIsNullShouldThrow() {
				assertThatThrownBy(
						() -> clientValidator.validateLastName(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a not null surname.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Empty or only-spaces surnames")
			@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
			void testValidateSurnameWhenSurnameIsEmptyShouldThrow(String emptyLastName) {
				assertThatThrownBy(
						() -> clientValidator.validateLastName(emptyLastName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty surname.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-alphabet surnames")
			@ValueSource(strings = {"Mari0", "Ro55i", "Mario!", "Rossi@"})
			void testValidateSurnameWhenSurnameContainsNonAlphabetCharactersShouldThrow(
					String nonAlphabetLastName) {
				assertThatThrownBy(
						() -> clientValidator.validateLastName(nonAlphabetLastName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's surname must contain only alphabet letters.");
			}
		}

		@Nested
		@DisplayName("Surnames fixed")
		class SurnamesFixedTest {

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Side spaced surnames")
			@ValueSource(strings = {"\tRossi", "Rossi ", "  Rossi", "Rossi  "})
			void testValidateSurnameWhenSurnameContainsSideSpacesShouldRemove(
					String sideSpacedLastName) {
				assertThat(clientValidator.validateLastName(sideSpacedLastName))
					.isEqualTo(VALID_LASTNAME);
			}

			@ParameterizedTest(name = "{index}: ''{0}'' => ''{1}''")
			@DisplayName("Several spaced surnames")
			@CsvSource({
				"'De  Lucia', 'De Lucia'",
				"'De  Lucio  Lucia', 'De Lucio Lucia'",
				"'De   Lucio   Lucia', 'De Lucio Lucia'"
			})
			void testValidateSurnameWhenSurnameContainsSeveralSpacesShouldReduce(
					String actualLastName, String expectedLastName) {
				assertThat(clientValidator.validateLastName(actualLastName))
					.isEqualTo(expectedLastName);
			}
		}
	}
}
