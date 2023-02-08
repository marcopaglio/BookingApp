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
		private String firstName = "Mario";
		private String lastName = "Rossi";
		
		@Test
		@DisplayName("Constructor correctly sets right values.")
		void testConstructorWhenArgumentsAreValidShouldSetRightValues() {
			Client client = new Client(firstName, lastName);
			
			assertThat(client.getUUID()).isNotNull();
			assertThat(client.getFirstName()).isEqualTo(firstName);
			assertThat(client.getLastName()).isEqualTo(lastName);
			assertThat(client.getReservations()).isEmpty();
		}
		
		@Test
		@DisplayName("Constructor throws exception if at least a name parameter is null.")
		void testConstructorWhenNameIsNullShouldThrow() {
			assertThatThrownBy(() -> new Client(null, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null first name.");
			
			assertThatThrownBy(() -> new Client(firstName, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null last name.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throws exception if at least a name "
				+ "parameter is empty or contains only spaces.")
		@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
		void testConstructorWhenNameIsEmptyShouldThrow(String emptyName) {
			assertThatThrownBy(() -> new Client(emptyName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			
			assertThatThrownBy(() -> new Client(firstName, emptyName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor throws exception if at least a name"
				+ "parameter contains non-alphabet characters.")
		@ValueSource(strings = {
			"1234", "3ario", "Mari0", "Ma7io",					// numbers
			"R0ss1", "70ssi", "Ro55i", "70ss1",					// numbers
			"£$°&()", "Mario!", ".Mario", "¿Mario?", "M@rio",	// special characters
			"^*=[]", "Rossi@", "#Rossi", "%Rossi%", "Ro\"si"	// special characters
		})
		void testConstructorWhenFirstNameContainsNonAlphabetCharactersShouldThrow(
				String nonAlphabetName) {
			assertThatThrownBy(() -> new Client(nonAlphabetName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
			
			assertThatThrownBy(() -> new Client(firstName, nonAlphabetName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's last name must contain only alphabet letters.");
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor doesn't throw exception if name parameter contains spaces.")
		@ValueSource(strings = {
			"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
			"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
		})
		void testConstructorWhenNameContainsSpacesShouldNotThrow(String spacedName) {
			assertThatNoException().isThrownBy(() -> new Client(spacedName, lastName));
			assertThatNoException().isThrownBy(() -> new Client(firstName, spacedName));
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Constructor doesn't throw exception if name parameter contains accented letters.")
		@ValueSource(strings = {
			"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
			"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
		})
		void testConstructorWhenNameContainsAccentedLettersShouldNotThrow(String accentedName) {
			assertThatNoException().isThrownBy(() -> new Client(accentedName, lastName));
			assertThatNoException().isThrownBy(() -> new Client(firstName, accentedName));
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''''{1}''")
		@DisplayName("Constructor correctly deletes side spaces.")
		@CsvSource({
			" Mario,\tRossi",
			"Mario\t,Rossi ",
			" Mario ,\fRossi\r",
			"  Mario,\f Rossi",
			"Mario\f ,Rossi  ",
			"  Mario  , \rRossi \t"
		})
		void testConstructorWhenInputsContainsTooMuchSpacesShouldAdjust(
				String sideSpacedFirstName, String sideSpacedLastName) {
			Client client = new Client(sideSpacedFirstName, sideSpacedLastName);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(firstName),
				() -> assertThat(client.getLastName()).isEqualTo(lastName)
			);
		}
		
		@ParameterizedTest(name = "{index}: ''{0}''''{1}'' => ''{2}''''{3}''")
		@DisplayName("Constructor correctly converts multiple spaces into single spaces.")
		@CsvSource({
			"Maria  Luisa,De  Lucia,Maria Luisa,De Lucia",
			"Mario  Maria  Mario,De  Lucio  Lucia,Mario Maria Mario,De Lucio Lucia",
			"Mario   Maria   Mario,De   Lucio   Lucia,Mario Maria Mario,De Lucio Lucia",
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
	// TODO: due clienti hanno uuid diversi

	// TODO: controllare le copie difensive dei get modificando il valore datogli
	// TODO: aggiungere rimuovere prenotazioni
	// TODO: quando due oggetti sono uguali, hash e stringhe


}
