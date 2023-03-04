package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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
	private static final String VALID_LAST_NAME = "Rossi";
	private static final String VALID_DATE = "2023-04-24";
	private static final List<Reservation> EMPTY_LIST = new ArrayList<>();
	private static final Reservation RESERVATION = 
			new Reservation(UUID.randomUUID(), LocalDate.parse(VALID_DATE));
	private static final List<Reservation> NON_EMPTY_LIST = 
			new ArrayList<>(Arrays.asList(RESERVATION));

	@Nested
	@DisplayName("Check constructor inputs")
	class ConstructorTest {
		@Test
		void testToFail() {
			fail();
		}

		@Test
		@DisplayName("Valid parameters and empty reservations' list")
		void testConstructorWhenParametersAreValidAndListIsEmptyShouldInsertValues() {
			Client client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME, EMPTY_LIST);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
				() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME),
				() -> assertThat(client.getUuid()).isNotNull(),
				() -> assertThat(client.getReservations().isEmpty())
			);
		}

		@Test
		@DisplayName("Valid parameters and non-empty reservations' list")
		void testConstructorWhenParametersAreValidAndListIsNotEmptyShouldInsertValues() {
			Client client = new Client(VALID_FIRST_NAME, VALID_LAST_NAME, NON_EMPTY_LIST);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(VALID_FIRST_NAME),
				() -> assertThat(client.getLastName()).isEqualTo(VALID_LAST_NAME),
				() -> assertThat(client.getUuid()).isNotNull(),
				() -> assertThat(client.getReservations()).isEqualTo(NON_EMPTY_LIST)
			);
		}

		@Test
		@DisplayName("Null names")
		void testConstructorWhenNameIsNullShouldThrow() {
			assertThatThrownBy(() -> new Client(null, VALID_LAST_NAME, EMPTY_LIST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null first name.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, null, EMPTY_LIST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null last name.");
		}

		@Test
		@DisplayName("Null reservations' list")
		void testConstructorWhenListIsNullShouldThrow() {
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, VALID_LAST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a not null reservations' list.");
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Empty or only-spaces names")
		@ValueSource(strings = {"", " ", "  ", "\t ", " \n", " \r ", "\f  "})
		void testConstructorWhenNameIsEmptyShouldThrow(String emptyName) {
			assertThatThrownBy(() -> new Client(emptyName, VALID_LAST_NAME, EMPTY_LIST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty first name.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, emptyName, EMPTY_LIST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client needs a non-empty last name.");
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Non-alphabet names")
		@ValueSource(strings = {"Mari0", "Ro55i", "Mario!", "Rossi@"})
		void testConstructorWhenNameContainsNonAlphabetCharactersShouldThrow(
				String nonAlphabetName) {
			assertThatThrownBy(() -> new Client(nonAlphabetName, VALID_LAST_NAME, EMPTY_LIST))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client's first name must contain only alphabet letters.");
			
			assertThatThrownBy(() -> new Client(VALID_FIRST_NAME, nonAlphabetName, EMPTY_LIST))
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
					() -> new Client(accentedName, VALID_LAST_NAME, EMPTY_LIST));
			assertThatNoException().isThrownBy(
					() -> new Client(VALID_FIRST_NAME, accentedName, EMPTY_LIST));
		}

		@ParameterizedTest(name = "{index}: ''{0}''")
		@DisplayName("Names with spaces")
		@ValueSource(strings = {
			"De Lucia", "De Lucio Lucia", " Maria", "Maria ", " Maria ",		// single space
			"De  Lucia", "De  Lucio  Lucia", "  Maria", "Maria  ", "  Maria  "	// multiple spaces
		})
		void testConstructorWhenNameContainsSpacesShouldNotThrow(String spacedName) {
			assertThatNoException().isThrownBy(
					() -> new Client(spacedName, VALID_LAST_NAME, EMPTY_LIST));
			assertThatNoException().isThrownBy(
					() -> new Client(VALID_FIRST_NAME, spacedName, EMPTY_LIST));
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
			Client client = new Client(sideSpacedFirstName, sideSpacedLastName, EMPTY_LIST);
			
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
			Client client = new Client(actualFirstName, actualLastName, EMPTY_LIST);
			
			assertAll(
				() -> assertThat(client.getFirstName()).isEqualTo(expectedFirstName),
				() -> assertThat(client.getLastName()).isEqualTo(expectedLastName)
			);
		}
	}

	@Nested
	@DisplayName("Tests on methods")
	class MethodTest {
		private Client emptyListClient;
		private Client nonEmptyListClient;

		@BeforeEach
		void setUp() {
			emptyListClient = new Client(VALID_FIRST_NAME, VALID_LAST_NAME, EMPTY_LIST);
			nonEmptyListClient = new Client(VALID_FIRST_NAME, VALID_LAST_NAME, NON_EMPTY_LIST);
		}

		@Nested
		@DisplayName("Equality for Clients")
		class ClientsEqualityTest {

			@Test
			@DisplayName("Same names and reservations")
			void testEqualsWhenClientsHaveSameNameAndReservationsShouldPass() {
				Client anotherEmptyListClient =
						new Client(VALID_FIRST_NAME, VALID_LAST_NAME, EMPTY_LIST);
				
				assertAll(
					() -> assertThat(emptyListClient).isEqualTo(anotherEmptyListClient),
					() -> assertThat(emptyListClient).hasSameHashCodeAs(anotherEmptyListClient)
				);
			}

			@Test
			@DisplayName("Same names and different reservations")
			void testEqualsWhenClientsHaveSameNamesAndDifferentReservationsShouldPass() {
				assertAll(
					() -> assertThat(emptyListClient).isEqualTo(nonEmptyListClient),
					() -> assertThat(emptyListClient).hasSameHashCodeAs(nonEmptyListClient)
				);
			}

			@Test
			@DisplayName("Different first names")
			void testEqualsWhenClientsHaveDifferentFirstNamesShouldFail() {
				String anotherFirstName = "Maria";
				Client anotherClient =
						new Client(anotherFirstName, VALID_LAST_NAME, EMPTY_LIST);
				
				assertAll(
					() -> assertThat(emptyListClient).isNotEqualTo(anotherClient),
					() -> assertThat(emptyListClient.hashCode())
							.isNotEqualTo(anotherClient.hashCode())
				);
			}

			@Test
			@DisplayName("Different last names")
			void testEqualsWhenClientsHaveDifferentLastNamesShouldFail() {
				String anotherLastName = "De Lucia";
				Client anotherClient =
						new Client(VALID_FIRST_NAME, anotherLastName, EMPTY_LIST);
				
				assertAll(
					() -> assertThat(emptyListClient).isNotEqualTo(anotherClient),
					() -> assertThat(emptyListClient.hashCode())
							.isNotEqualTo(anotherClient.hashCode())
				);
			}
		}

		@Nested
		@DisplayName("Check that attributes are not alterable outside Client class")
		class DefensiveCopyTest {

			@Test
			@DisplayName("Empty list returned from 'getCopyOfReservations' is modified")
			void testGetCopyOfReservationsWhenReturnedEmptyListIsModifiedShouldNotChangeAttributeValue() {
				List<Reservation> returnedReservations = emptyListClient.getCopyOfReservations();
				
				returnedReservations.add(RESERVATION);
				
				assertThat(emptyListClient.getReservations()).isEqualTo(EMPTY_LIST);
			}

			@Test
			@DisplayName("Reservation list returned from 'getCopyOfReservations' is modified")
			void testGetCopyOfReservationsWhenReturnedListIsModifyShouldNotChangeAttributeValue() {
				List<Reservation> returnedReservations = nonEmptyListClient.getCopyOfReservations();
				
				returnedReservations.remove(RESERVATION);
				
				assertThat(nonEmptyListClient.getReservations()).isEqualTo(NON_EMPTY_LIST);
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
					emptyListClient.addReservation(RESERVATION);
					
					assertThat(emptyListClient.getReservations()).containsOnly(RESERVATION);
				}

				@Test
				@DisplayName("Insert pre-existing reservation")
				void testAddReservationWhenReservationIsAlreadyExistingShouldThrow() {
					assertThatThrownBy(
							() -> nonEmptyListClient.addReservation(RESERVATION))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation [date=" + VALID_DATE + "] to add is already in Client ["
								+ VALID_FIRST_NAME + " " + VALID_LAST_NAME + "]'s list.");
				}

				@Test
				@DisplayName("Insert null reservation")
				void testAddReservationWhenReservationIsNullShouldThrow() {
					assertThatThrownBy(() -> emptyListClient.addReservation(null))
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
					nonEmptyListClient.removeReservation(RESERVATION); 
					
					assertThat(nonEmptyListClient.getReservations()).doesNotContain(RESERVATION);
				}

				@Test
				@DisplayName("Delete non-existent reservation")
				void testRemoveReservationWhenReservationDoesNotExistShouldThrow() {
					assertThatThrownBy(
							() -> emptyListClient.removeReservation(RESERVATION))
						.isInstanceOf(NoSuchElementException.class)
						.hasMessage("Reservation [date=" + VALID_DATE + "] to delete is not in Client ["
								+ VALID_FIRST_NAME + " " + VALID_LAST_NAME + "]'s list.");
				}

				@Test
				@DisplayName("Null reservation")
				void testRemoveReservationWhenReservationIsNullShouldThrow() {
					assertThatThrownBy(
							() -> emptyListClient.removeReservation(null))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation to delete can't be null.");
					
					assertThatThrownBy(
							() -> nonEmptyListClient.removeReservation(null))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Reservation to delete can't be null.");
				}
			}
		}
	}
}
