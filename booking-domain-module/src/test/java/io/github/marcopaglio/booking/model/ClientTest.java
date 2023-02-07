package io.github.marcopaglio.booking.model;

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

@DisplayName("Tests for Client entity.")
class ClientTest {
	
	@Nested
	@DisplayName("Check constructor inputs.")
	class ConstructorInput {
		
		@Test
		@DisplayName("Constructor correctly sets right values.")
		void testConstructorWhenArgumentsAreValidShouldSetRightValues() {
			String firstName = "Mario";
			String lastName = "Rossi";
			Client client = new Client(firstName, lastName);
			
			assertThat(client.getUUID()).isNotNull();
			assertThat(client.getFirstName()).isEqualTo(firstName);
			assertThat(client.getLastName()).isEqualTo(lastName);
			assertThat(client.getReservations()).isEmpty();
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throw exception if firstName is empty or contains only spaces.")
		@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
		void testConstructorWhenFirstNameIsEmptyShouldThrow(String firstName) {
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throw exception if firstName contains non-alphabet characters.")
		@ValueSource(strings = {
						"1234", "3ario", "Mari0", "Ma7io", "M4ri0", "34rio", "M47io", "34ri0",	//numbers
						"£$°&()", "Mario!", ".Mario", "¿Mario?", "M@rio" 						// special characters
					})
		void testConstructorWhenFirstNameContainsNonAlphabetCharactersShouldThrow(String firstName) {
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor not throw exception if firstName "
				+ "contains spaces or accented letters.")
		@ValueSource(strings = {
			"Maria Luisa", "Mario Maria Mario", " Maria", "Maria ", " Maria ",			// single space
			"Maria  Luisa", "Mario  Maria  Mario", "  Maria", "Maria  ", "  Maria  ",	// multiple spaces
			"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",					// accented letters
			"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"						// accented letters
		})
		void testConstructorWhenFirstNameContainsSpacesOrAccentedLettersShouldNotThrow(String firstName) {
			String lastName = "Rossi";
			
			assertThatNoException().isThrownBy(() -> new Client(firstName, lastName));
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throw exception if lastName is empty or contains only spaces.")
		@ValueSource(strings = {"", " ", "  ", " \t ", "\n ", " \r", "\f  "})
		void testConstructorWhenLastNameIsEmptyShouldThrow(String lastName) {
			String firstName = "Mario";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throw exception if lastName contains non-alphabet characters.")
		@ValueSource(strings = {
						"6789", "7ossi", "Ross1", "Ro5si", "R0ss1", "70ssi", "Ro55i", "70ss1",	//numbers
						"^*=[]", "Rossi@", "#Rossi", "%Rossi%", "Ro\"si"						//special characters
					})
		void testConstructorWhenLastNameContainsNonAlphabetCharactersShouldThrow(String lastName) {
			String firstName = "Mario";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's last name must contain only alphabet letters.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor not throw exception if lastName "
				+ "contains spaces or accented letters.")
		@ValueSource(strings = {
			"De Lucia", "De Lucio Lucia", " Rossi", "Rossi ", " Rossi ",			// single space
			"De  Lucia", "De  Lucio  Lucia", "  Rossi", "Rossi  ", "  Rossi  ",		// multiple spaces
			"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",				// accented letters
			"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"					// accented letters
		})
		void testConstructorWhenLastNameContainsSpacesOrAccentedLettersShouldNotThrow(String lastName) {
			String firstName = "Mario";
			
			assertThatNoException().isThrownBy(() -> new Client(firstName, lastName));
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''''{1}'' => ''{2}''''{3}''")
		@DisplayName("Constructor correctly deletes side spaces "
				+ "and converts multiple spaces into single spaces.")
		@CsvSource({
			" Maria, Rossi,Maria,Rossi",
			"Maria ,Rossi ,Maria,Rossi",
			" Maria , Rossi ,Maria,Rossi",
			"Maria  Luisa,De  Lucia,Maria Luisa,De Lucia",
			"Mario  Maria  Mario,De  Lucio  Lucia,Mario Maria Mario,De Lucio Lucia",
			"  Maria,  Rossi,Maria,Rossi",
			"Maria  ,Rossi  ,Maria,Rossi",
			"  Maria  ,  Rossi  ,Maria,Rossi"
		})
		void testConstructorWhenInputsContainsTooMuchSpacesShouldAdjust(
				String actualFirstName, String actualLastName,
				String expectedFirstName, String expectedLastName) {
			Client client = new Client(actualFirstName, actualLastName);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(expectedFirstName),
				() -> assertThat(client.getLastName()).isEqualTo(expectedLastName)
			);
		}
	}

	// TODO: controllare le copie difensive dei get modificando il valore datogli
	// TODO: due clienti hanno uuid diversi
	// TODO: quando due oggetti sono uguali, hash e stringhe


}
