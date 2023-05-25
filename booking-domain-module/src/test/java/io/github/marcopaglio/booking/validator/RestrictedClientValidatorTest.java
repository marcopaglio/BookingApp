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


@DisplayName("Tests for RestrictedClientValidator")
class RestrictedClientValidatorTest {
	private static final String VALID_FIRST_NAME = "Mario";
	private static final String VALID_LAST_NAME = "Rossi";

	@Nested
	@DisplayName("Tests for 'newValidatedClient'")
	class NewValidatedClientTest {

		@Nested
		@DisplayName("Valid names")
		class ValidNamesTest {

			@Test
			@DisplayName("Creation has success")
			void testNewValidatedClientWhenNamesAreValidShouldReturnClient() {
				Client client = RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, VALID_LAST_NAME);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
					() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME),
					() -> assertThat(client.getUuid()).isNotNull()
				);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Accented names")
			@ValueSource(strings = {
				"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
				"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
			})
			void testNewValidatedClientWhenNameAreAccentedShouldNotThrow(String accentedName) {
				assertThatNoException().isThrownBy(
						() -> RestrictedClientValidator.newValidatedClient(accentedName, VALID_LAST_NAME));
				assertThatNoException().isThrownBy(
						() -> RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, accentedName));
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Names with spaces")
			@ValueSource(strings = {
				"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
				"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
			})
			void testConstructorWhenNameContainsSpacesShouldNotThrow(String spacedName) {
				assertThatNoException().isThrownBy(
						() -> RestrictedClientValidator.newValidatedClient(spacedName, VALID_LAST_NAME));
				assertThatNoException().isThrownBy(
						() -> RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, spacedName));
			}
		}

		@Test
		@DisplayName("Null names")
		void testNewValidatedClientWhenNameIsNullShouldThrow() {
			assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(null, VALID_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null name.");
			
			assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null surname.");
		}

		@Nested
		@DisplayName("Invalid names")
		class InvalidNamesTest {

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Empty or only-spaces names")
			@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
			void testNewValidatedClientWhenNameIsEmptyShouldThrow(String emptyName) {
				assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(emptyName, VALID_LAST_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty name.");
				
				assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, emptyName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client needs a non-empty surname.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-alphabet names")
			@ValueSource(strings = {"Mari0", "Ro55i", "Mario!", "Rossi@"})
			void testNewValidatedClientWhenNameContainsNonAlphabetCharactersShouldThrow(
					String nonAlphabetName) {
				assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(nonAlphabetName, VALID_LAST_NAME))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's name must contain only alphabet letters.");
				
				assertThatThrownBy(() -> RestrictedClientValidator.newValidatedClient(VALID_FIRST_NAME, nonAlphabetName))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client's surname must contain only alphabet letters.");
			}
		}

		@Nested
		@DisplayName("Names fixed")
		class NameFixedTest {

			@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
			@DisplayName("Side spaced names")
			@CsvSource({
				"' Mario', '\tRossi'",
				"'Mario\t', 'Rossi '",
				"' Mario ', '  Rossi'",
				"'\t Mario  ', 'Rossi  '"
			})
			void testConstructorWhenInputsContainSideSpacesShouldRemove(
					String sideSpacedFirstName, String sideSpacedLastName) {
				Client client = RestrictedClientValidator.newValidatedClient(sideSpacedFirstName, sideSpacedLastName);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
					() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME)
				);
			}

			@ParameterizedTest(name = "{index}: ''{0}''''{1}'' => ''{2}''''{3}''")
			@DisplayName("Several spaced names")
			@CsvSource({
				"'Maria  Luisa', 'De  Lucia', 'Maria Luisa', 'De Lucia'",
				"'Mario  Maria  Mario', 'De  Lucio  Lucia', 'Mario Maria Mario', 'De Lucio Lucia'",
				"'Mario   Maria   Mario', 'De   Lucio   Lucia', 'Mario Maria Mario', 'De Lucio Lucia'"
			})
			void testConstructorWhenInputsContainSeveralSpacesShouldReduce(
					String actualFirstName, String actualLastName,
					String expectedFirstName, String expectedLastName) {
				Client client = RestrictedClientValidator.newValidatedClient(actualFirstName, actualLastName);
				
				assertAll(
					() -> assertThat(client.getFirstName()).isEqualTo(expectedFirstName),
					() -> assertThat(client.getLastName()).isEqualTo(expectedLastName)
				);
			}
		}
	}
}
