package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
		
		@Test
		@DisplayName("Constructor throw exception if firstName is empty.")
		void testConstructorWhenFirstNameIsEmptyShouldThrow() {
			String firstName = "";
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if firstName is a single space.")
		void testConstructorWhenFirstNameIsSingleSpaceShouldThrow() {
			String firstName = " ";
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if firstName is multiple spaces.")
		void testConstructorWhenFirstNameIsMultipleSpacesShouldThrow() {
			String firstName = "  ";
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			// TODO: check object is null (not created)
		}
		
		// TODO: more test on space characters
		
		@Test
		@DisplayName("Constructor throw exception if firstName contains numbers.")
		void testConstructorWhenFirstNameContainsNumbersShouldThrow() {
			String firstName = "M4ri0";
			String lastName = "Rossi";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if lastName is empty.")
		void testConstructorWhenLastNameIsEmptyShouldThrow() {
			String firstName = "Mario";
			String lastName = "";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if lastName is a single space.")
		void testConstructorWhenLastNameIsSingleSpaceShouldThrow() {
			String firstName = "Mario";
			String lastName = " ";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if lastName is multiple spaces.")
		void testConstructorWhenLastNameIsMultipleSpacesShouldThrow() {
			String firstName = "Mario";
			String lastName = "  ";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
			// TODO: check object is null (not created)
		}
		
		@Test
		@DisplayName("Constructor throw exception if lastName contains numbers.")
		void testConstructorWhenLastNameContainsNumbersShouldThrow() {
			String firstName = "Mario";
			String lastName = "R0ss1";
			
			assertThatThrownBy(() -> new Client(firstName, lastName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's last name must contain only alphabet letters.");
			// TODO: check object is null (not created)
		}
	}

	// TODO: che fare se contiene spazi dopo il nome. Es: "Mario    ", oppure "Maria Luisa"
	// TODO: controllare le copie difensive dei get modificando il valore datogli
	// TODO: due clienti hanno uuid diversi
	// TODO: quando due oggetti sono uguali, hash e stringhe


}
