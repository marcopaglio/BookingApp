package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests for Client entity")
class ClientTest {
	private static final String VALID_FIRST_NAME = "Mario";
	private static final String ANOTHER_VALID_FIRST_NAME = "Maria";
	private static final String VALID_LAST_NAME = "Rossi";
	private static final String ANOTHER_VALID_LAST_NAME = "De Lucia";
	private static final String VALID_DATE = "2023-04-24";

	@Nested
	@DisplayName("Check constructor inputs")
	class ConstructorTest {
		@Test
		@DisplayName("Valid parameters")
		void testConstructorWhenParametersAreValidShouldInsertValues() {
			Client client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
				() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME),
				() -> assertThat(client.getUUID()).isNotNull(),
				() -> assertThat(client.getReservations().isEmpty())
			);
		}

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
		@DisplayName("Constructor correctly deletes side spaces")
		@CsvSource({
			"' Mario', '\tRossi'",
			"'Mario\t', 'Rossi '",
			"' Mario ', '  Rossi'",
			"'\t Mario  ', 'Rossi  '"
		})
		void testConstructorWhenInputsContainSideSpacesShouldRemove(
				String sideSpacedFirstName, String sideSpacedLastName) {
			Client client = new Client(sideSpacedFirstName, sideSpacedLastName);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
				() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME)
			);
		}

		@ParameterizedTest(name = "{index}: ''{0}''''{1}'' => ''{2}''''{3}''")
		@DisplayName("Constructor correctly converts multiple spaces into single space")
		@CsvSource({
			"'Maria  Luisa', 'De  Lucia', 'Maria Luisa', 'De Lucia'",
			"'Mario  Maria  Mario', 'De  Lucio  Lucia', 'Mario Maria Mario', 'De Lucio Lucia'",
			"'Mario   Maria   Mario', 'De   Lucio   Lucia', 'Mario Maria Mario', 'De Lucio Lucia'"
		})
		void testConstructorWhenInputsContainTooMuchSpacesShouldReduce(
				String actualFirstName, String actualLastName,
				String expectedFirstName, String expectedLastName) {
			Client client = new Client(actualFirstName, actualLastName);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(expectedFirstName),
				() -> assertThat(client.getLastName()).isEqualTo(expectedLastName)
			);
		}
	}

	@Nested
	@DisplayName("Tests on methods")
	class MethodTests {
		private Client client;
		private Reservation reservation;

		@BeforeEach
		void setUp() {
			// TODO: alternativa: metterli in @BeforeAll, 
			// renderli statici e usare set package-private
			// per impostare la lista vuota all'inizio di ogni test
			// In questo modo non dipende dal fatto che il costruttore 
			// imposti la lista iniziale vuota (cioè se cambia, questi test continuano a fare)
			client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
			reservation = new Reservation(client, VALID_DATE);
		}

		@Nested
		@DisplayName("Equality for Clients")
		class ClientsEquality {
			@Test
			@DisplayName("Same names and reservations")
			void testEqualsWhenClientsHaveSameNameAndReservationsShouldPass() {
				Client client2 = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
				
				assertThat(client).isEqualTo(client2);
				assertThat(client.hashCode()).isEqualTo(client2.hashCode());
			}

			@Test
			@DisplayName("Same names and different reservations")
			void testEqualsWhenClientsHaveSameNamesAndDifferentReservationsShouldPass() {
				setUpReservationList();
				Client client2 = new Client(VALID_FIRST_NAME, VALID_LAST_NAME);
				
				assertThat(client).isEqualTo(client2);
				assertThat(client.hashCode()).isEqualTo(client2.hashCode());
			}

			@Test
			@DisplayName("Different first names")
			void testEqualsWhenClientsHaveDifferentFirstNamesShouldFail() {
				Client client2 = new Client(ANOTHER_VALID_FIRST_NAME, VALID_LAST_NAME);
				
				assertThat(client).isNotEqualTo(client2);
				assertThat(client.hashCode()).isNotEqualTo(client2.hashCode());
			}

			@Test
			@DisplayName("Different last names")
			void testEqualsWhenClientsHaveDifferentLastNamesShouldFail() {
				Client client2 = new Client(VALID_FIRST_NAME, ANOTHER_VALID_LAST_NAME);
				
				assertThat(client).isNotEqualTo(client2);
				assertThat(client.hashCode()).isNotEqualTo(client2.hashCode());
			}
		}

		@Nested
		@DisplayName("Check that attributes are not alterable outside Client class")
		class DefensiveCopyTests {
			@Test
			@DisplayName("Empty list returned from 'getCopyOfReservations' is modified")
			void testGetCopyOfReservationsWhenReturnedEmptyListIsModifiedShouldNotChangeAttributeValue() {
				Collection<Reservation> returnedReservations = client.getCopyOfReservations();
				
				returnedReservations.add(reservation);
				
				assertThat(client.getReservations()).isEqualTo(new ArrayList<Reservation>());
			}

			@Test
			@DisplayName("Reservation list returned from 'getCopyOfReservations' is modified")
			void testGetCopyOfReservationsWhenReturnedListIsModifyShouldNotChangeAttributeValue() {
				Collection<Reservation> reservations = new ArrayList<Reservation>();
				reservations.add(reservation);
				client.setReservations(reservations);
				// TODO: si può setUpReservationList?
				Collection<Reservation> returnedReservations = client.getCopyOfReservations();
				
				returnedReservations.remove(reservation);
				
				assertThat(client.getReservations()).isEqualTo(reservations);
			}
		}

		@Nested
		@DisplayName("Tests for reservation managing")
		class ManageReservationsTest {
			@Nested
			@DisplayName("Tests for 'addReservation'")
			class AddReservationTest {
				@Test
				@DisplayName("Insert new reservation")
				void testAddReservationWhenReservationIsValidShouldInsert() {
					client.addReservation(reservation);
					
					assertThat(client.getReservations()).containsOnly(reservation);
				}

				@Test
				@DisplayName("Insert pre-existing reservation")
				void testAddReservationWhenReservationIsAlreadyExistingShouldThrow() {
					setUpReservationList();
					
					assertThatThrownBy(
							() -> client.addReservation(reservation))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation [date=" + VALID_DATE + "] to add is already in Client ["
								+ VALID_FIRST_NAME + " " + VALID_LAST_NAME + "]'s list.");
				}

				@Test
				@DisplayName("Insert null reservation")
				void testAddReservationWhenReservationIsNullShouldThrow() {
					assertThatThrownBy(() -> client.addReservation(null))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation to add can't be null.");
				}
			}

			@Nested
			@DisplayName("Tests for 'removeReservation'")
			class RemoveReservationTest {
				@Test
				@DisplayName("Delete existing reservation")
				void testRemoveReservationWhenReservationExistsShouldRemove() {
					setUpReservationList();
					
					client.removeReservation(reservation);
					
					assertThat(client.getReservations()).doesNotContain(reservation);
				}

				@Test
				@DisplayName("Delete non-existent reservation")
				void testRemoveReservationWhenReservationDoesNotExistShouldThrow() {
					assertThatThrownBy(
							() -> client.removeReservation(reservation))
						.isInstanceOf(NoSuchElementException.class)
						.hasMessage("Reservation [date=" + VALID_DATE + "] to delete is not in Client ["
								+ VALID_FIRST_NAME + " " + VALID_LAST_NAME + "]'s list.");
				}

				@Test
				@DisplayName("Null reservation")
				void testRemoveReservationWhenReservationIsNullShouldThrow() {
					assertThatThrownBy(
							() -> client.removeReservation(null))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation to delete can't be null.");
				}
			}
		}

		private void setUpReservationList() {
			Collection<Reservation> reservations = new ArrayList<Reservation>();
			reservations.add(reservation);
			client.setReservations(reservations);
		}
	}
}
