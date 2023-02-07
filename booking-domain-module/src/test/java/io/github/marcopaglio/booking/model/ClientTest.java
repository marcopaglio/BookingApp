package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
		@DisplayName("Constructor throw exception if firstName contains numbers or non-alphabet letters.")
		@ValueSource(strings = {
						"1234", "3ario", "Mari0", "Ma7io", "M4ri0", "34rio", "M47io", "34ri0", //numbers
						"£$°&()", "Mario!", ".Mario", "¿Mario?", "M@rio" // special characters
					})
		void testConstructorWhenFirstNameContainsNumbersShouldThrow(String firstName) {
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
		}
		
		// TODO: non deve lanciare eccezioni con spazi e lettere accentate
		
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
		@DisplayName("Constructor throw exception if lastName contains numbers.")
		@ValueSource(strings = {
						"6789", "7ossi", "Ross1", "Ro5si", "R0ss1", "70ssi", "Ro55i", "70ss1", //numbers
						"^*=[]", "Rossi@", "#Rossi", "%Rossi%", "Ro\"si" // special characters
					})
		void testConstructorWhenLastNameContainsNumbersShouldThrow(String lastName) {
			String firstName = "Mario";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's last name must contain only alphabet letters.");
		}
	}

	// TODO: che fare se contiene spazi dopo il nome. Es: "Mario    ", oppure "Maria Luisa"
	// TODO: controllare le copie difensive dei get modificando il valore datogli
	// TODO: due clienti hanno uuid diversi
	// TODO: quando due oggetti sono uguali, hash e stringhe


}
