package io.github.marcopaglio.booking.model;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@DisplayName("Tests for Reservation entity.")
class ReservationTest {
	private static final Client VALID_CLIENT = new Client("Mario", "Rossi");
	private static final UUID VALID_UUID = UUID.randomUUID();
	private static final String VALID_STRING_DATE = "2022-12-22";
	private static final LocalDate VALID_DATE = LocalDate.of(2023, 4, 24);

	@Nested
	@DisplayName("Check constructor inputs")
	class ConstructorInput {
		@Nested
		@DisplayName("Constructor without conversions")
		class PlainConstructorTest {
			@Test
			@DisplayName("Null clientUUID")
			void testPlainConstructorWhenUUIDIsNullShouldThrow() {
				assertThatThrownBy(() -> new Reservation(null, VALID_DATE))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null client uuid.");
			}

			@Test
			@DisplayName("Null date")
			void testPlainConstructorWhenDateIsNullShouldThrow() {
				assertThatThrownBy(() -> new Reservation(VALID_UUID, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null date.");
			}

			@Test
			@DisplayName("Not null parameters")
			void testPlainConstructorWhenParametersAreNotNullShouldNotThrow() {
				assertThatNoException().isThrownBy(
						() -> new Reservation(VALID_UUID, VALID_DATE));
			}
		}

		@Nested
		@DisplayName("Constructor with conversions")
		class ConstructorWithLogicTest {
			@Test
			@DisplayName("Null client")
			void testConstructorWithLogicWhenClientIsNullShouldThrow() {
				assertThatThrownBy(() -> new Reservation(null, VALID_STRING_DATE))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null client.");
			}

			@Test
			@DisplayName("Null date")
			void testConstructorWithLogicWhenDateIsNullShouldThrow() {
				assertThatThrownBy(() -> new Reservation(VALID_CLIENT, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null date.");
			}
			
			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-numeric date")
			@ValueSource(strings = {
				"2022-12-dd", "2022-MM-21", "aAAa-12-21",			// alphabetic characters
				"2022-12-2@", "2022-1!-21", "2O?2-12-21",			// special characters
				"2022.12.21", "2022_12_21", "2022/12/21",			// different separators
				" 022-12-21", "202\r-12-21 ", "2022-1\f-21",		// single space
				"2\n22-12-21", "2022-12-\t1",						// single space
				"\n  \t-\f -\r ", "\n   -  -\r ", "    -\r -\n ",	// mixed spaces
				"    -\r\n-  ", " \n\r -  -  ","  \r\n- \n-\r ",	// mixed spaces
				"\n\r\n\r-  -  ", "    -\n\r-\r\n"					// mixed spaces
			})
			void testConstructorWithLogicWhenDateContainsNonNumericCharactersShouldThrow(
					String nonNumericDate) {
				assertThatThrownBy(() -> new Reservation(VALID_CLIENT, nonNumericDate))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a only numeric date.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Wrong date format")
			@ValueSource(strings = {
				"", "20221221", "2022-1221", "202212-21", "20-22-12-21",// different subdivisions
				"21-12-2022", "12-21-2022", "12-2022-21", "21-2022-12",	// different locations
				"20226-12-21", "2022-126-21", "2022-12-216",			// different number of digits
				"202-212-21", "20221-2-12", "2022-122-1", "2022-1-221"	// different number of digits
			})
			void testConstructorWithLogicWhenStringDateFormatIsWrongShouldThrow(
					String wrongFormatDate) {
				assertThatThrownBy(() -> new Reservation(VALID_CLIENT, wrongFormatDate))
					.isInstanceOf(NumberFormatException.class)
					.hasMessage("Reservation needs a date in format aaaa-mm-dd.");
			}

			@Test
			@DisplayName("Wrong month")
			void testConstructorWithLogicWhenMonthInDateIsWrongShouldThrow() {
				String wrongMonthDate = "2022-13-12";
				assertThatThrownBy(() -> new Reservation(VALID_CLIENT, wrongMonthDate))
					.isInstanceOf(DateTimeParseException.class);
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Wrong day")
			@ValueSource(strings = {
				"2022-01-32",	// January
				"2022-02-29",	// February in not leap year
				"2024-02-30",	// February in leap year
				"2022-03-32",	// March
				"2022-04-31",	// April
				"2022-05-32",	// May
				"2022-06-31",	// June
				"2022-07-32",	// July
				"2022-08-32",	// August
				"2022-09-31",	// September
				"2022-10-32",	// October
				"2022-11-31",	// November
				"2022-12-32"	// December 
			})
			void testConstructorWithLogicWhenDayInDateIsWrongShouldThrow(String wrongDayDate) {
				assertThatThrownBy(() -> new Reservation(VALID_CLIENT, wrongDayDate))
					.isInstanceOf(DateTimeParseException.class);
			}

			@Test
			@DisplayName("Right date")
			void testConstructorWithLogicWhenDateIsCorrectShouldNotThrow() {
				assertThatNoException().isThrownBy(
					() -> new Reservation(VALID_CLIENT, VALID_STRING_DATE));
			}
		}
	}
	
	// LocalDate is immutable
	
	// UUID is immutable
}
