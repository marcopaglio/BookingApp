package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests for Client entity.")
class ClientTest {
	private static final String VALID_FIRST_NAME = "Mario";
	private static final String VALID_LAST_NAME = "Rossi";
	
	@Nested
	@DisplayName("Check constructor inputs")
	class ConstructorTest {
		@Test
		@DisplayName("Null names")
		void testConstructorWhenNameIsNullShouldThrow() {
			assertThatThrownBy(() -> new Client(null, VALID_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null first name.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null last name.");
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Empty or only-spaces names")
		@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
		void testConstructorWhenNameIsEmptyShouldThrow(String emptyName) {
			assertThatThrownBy(() -> new Client(emptyName, VALID_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, emptyName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Non-alphabet names")
		@ValueSource(strings = {
			"1234", "3ario", "Mari0", "Ma7io",					// numbers
			"R0ss1", "70ssi", "Ro55i", "70ss1",					// numbers
			"£$°&()", "Mario!", ".Mario", "¿Mario?", "M@rio",	// special characters
			"^*=[]", "Rossi@", "#Rossi", "%Rossi%", "Ro\"si"	// special characters
		})
		void testConstructorWhenNameContainsNonAlphabetCharactersShouldThrow(
				String nonAlphabetName) {
			assertThatThrownBy(() -> new Client(nonAlphabetName, VALID_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, nonAlphabetName))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's last name must contain only alphabet letters.");
		}

		@Test
		@DisplayName("Alphabetic names")
		void testConstructorWhenNamesAreAlphabeticShouldNotThrow() {
			assertThatNoException().isThrownBy(
					() -> new Client(VALID_FIRST_NAME, VALID_LAST_NAME));
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Accented names")
		@ValueSource(strings = {
			"èéàòìù", "Seán", "Ruairí", "Sørina", "Adrián", "François",
			"Mónica", "Mátyás", "Jokūbas", "Siân", "Zoë", "KŠthe"
		})
		void testConstructorWhenNameAreAccentedShouldNotThrow(String accentedName) {
			assertThatNoException().isThrownBy(
					() -> new Client(accentedName, VALID_LAST_NAME));
			assertThatNoException().isThrownBy(
					() -> new Client(VALID_FIRST_NAME, accentedName));
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Names with spaces")
		@ValueSource(strings = {
			"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
			"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
		})
		void testConstructorWhenNameContainsSpacesShouldNotThrow(String spacedName) {
			assertThatNoException().isThrownBy(
					() -> new Client(spacedName, VALID_LAST_NAME));
			assertThatNoException().isThrownBy(
					() -> new Client(VALID_FIRST_NAME, spacedName));
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
				() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
				() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME)
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
	
	// TODO: 1) test add e remove reservation
	
	
	// TODO: 2) test copie difensive
	@Nested
	@DisplayName("Attributes are not alterable outside Client class")
	class DefensiveCopy {
		
		// Java String Objects are immutable
		
		// UUID Objects are immutable
		
		@Test
		@DisplayName("Empty list returned from 'getReservations' is modified.")
		void testGetReservationsNameWhenModifyReturnedEmptyListShouldNotChangeAttributeValue() {
			Client client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
			Collection<Reservation> returnedReservations = client.getReservations();
			
			returnedReservations.add(new Reservation(client, "2023-02-08"));
			
			assertThat(client.getReservations()).isEqualTo(new ArrayList<Reservation>());
		}
		
		// TODO: quando addReservation è impostato

	}
	
	// TODO: 3) test equal
	@Nested
	@DisplayName("Equality for Clients.")
	class ClientsEquality {

		@Test
		@DisplayName("Same names without reservations equality")
		void testEqualsWhenClientsHaveSameNamesShouldPass() {
			Client client1 = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
			Client client2 = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
			
			assertThat(client1).isEqualTo(client2);
			assertThat(client1.hashCode()).isEqualTo(client2.hashCode());
		}
		
		// TODO: complete after manageReservation methods

	}
	
	
	// TODO: controllare le copie difensive dei get modificando il valore datogli
	// TODO: aggiungere rimuovere prenotazioni
}
