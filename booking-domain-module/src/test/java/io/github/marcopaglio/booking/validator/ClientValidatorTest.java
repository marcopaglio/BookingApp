package io.github.marcopaglio.booking.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.marcopaglio.booking.model.Client;


@DisplayName("Tests for ClientValidator class")
class ClientValidatorTest {
	private static final String VALID_FIRSTNAME = "Mario";
	private static final String VALID_LASTNAME = "Rossi";

	@Nested
	@DisplayName("Tests for 'newValidatedClient'")
	class NewValidatedClientTest {

		@Nested
		@DisplayName("Valid inputs")
		class ValidInputsTest {

			@Test
			@DisplayName("Creation has success")
			void testNewValidatedClientWhenNamesAndIdAreValidShouldReturnClient() {
				Client client = ClientValidator.newValidatedClient(VALID_FIRSTNAME, VALID_LASTNAME);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRSTNAME),
					() -> assertThat(client.getLastName()).isEqualTo(VALID_LASTNAME));
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Accented names")
			@ValueSource(strings = {
				"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
				"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
			})
			void testNewValidatedClientWhenNameIsAccentedShouldNotThrow(String accentedName) {
				assertThatNoException().isThrownBy(
						() -> ClientValidator.newValidatedClient(accentedName, VALID_LASTNAME));
				assertThatNoException().isThrownBy(
						() -> ClientValidator.newValidatedClient(VALID_FIRSTNAME, accentedName));
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Names with spaces")
			@ValueSource(strings = {
				"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
				"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
			})
			void testNewValidatedClientWhenNameContainsSpacesShouldNotThrow(String spacedName) {
				assertThatNoException().isThrownBy(
						() -> ClientValidator.newValidatedClient(spacedName, VALID_LASTNAME));
				assertThatNoException().isThrownBy(
						() -> ClientValidator.newValidatedClient(VALID_FIRSTNAME, spacedName));
			}
		}

		@Nested
		@DisplayName("Invalid names")
		class InvalidNamesTest {

			@Test
			@DisplayName("Null names")
			void testNewValidatedClientWhenNameIsNullShouldThrow() {
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(null, VALID_LASTNAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a not null name.");
				
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(VALID_FIRSTNAME, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a not null surname.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Empty or only-spaces names")
			@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
			void testNewValidatedClientWhenNameIsEmptyShouldThrow(String emptyName) {
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(emptyName, VALID_LASTNAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty name.");
				
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(VALID_FIRSTNAME, emptyName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty surname.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-alphabet names")
			@ValueSource(strings = {"Mari0", "Ro55i", "Mario!", "Rossi@"})
			void testNewValidatedClientWhenNameContainsNonAlphabetCharactersShouldThrow(
					String nonAlphabetName) {
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(nonAlphabetName, VALID_LASTNAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's name must contain only alphabet letters.");
				
				assertThatThrownBy(
						() -> ClientValidator.newValidatedClient(VALID_FIRSTNAME, nonAlphabetName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's surname must contain only alphabet letters.");
			}
		}

		@Nested
		@DisplayName("Names fixed")
		class NamesFixedTest {

			@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
			@DisplayName("Side spaced names")
			@CsvSource({
				"' Mario', '\tRossi'",
				"'Mario\t', 'Rossi '",
				"' Mario ', '  Rossi'",
				"'\t Mario  ', 'Rossi  '"
			})
			void testNewValidatedClientWhenNamesContainSideSpacesShouldRemove(
					String sideSpacedFirstName, String sideSpacedLastName) {
				Client client = ClientValidator.newValidatedClient(sideSpacedFirstName, sideSpacedLastName);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRSTNAME),
					() -> assertThat(client.getLastName()).isEqualTo(VALID_LASTNAME)
				);
			}

			@ParameterizedTest(name = "{index}: ''{0}''''{1}'' => ''{2}''''{3}''")
			@DisplayName("Several spaced names")
			@CsvSource({
				"'Maria  Luisa', 'De  Lucia', 'Maria Luisa', 'De Lucia'",
				"'Mario  Maria  Mario', 'De  Lucio  Lucia', 'Mario Maria Mario', 'De Lucio Lucia'",
				"'Mario   Maria   Mario', 'De   Lucio   Lucia', 'Mario Maria Mario', 'De Lucio Lucia'"
			})
			void testNewValidatedClientWhenNamesContainSeveralSpacesShouldReduce(
					String actualFirstName, String actualLastName,
					String expectedFirstName, String expectedLastName) {
				Client client = ClientValidator.newValidatedClient(actualFirstName, actualLastName);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(expectedFirstName),
					() -> assertThat(client.getLastName()).isEqualTo(expectedLastName)
				);
			}
		}
	}
}
