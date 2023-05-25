package io.github.marcopaglio.booking.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;

class ReservationValidatorTest {
	private static final Client VALID_CLIENT = new Client("Mario", "Rossi");
	private static final UUID VALID_UUID = VALID_CLIENT.getUuid();
	private static final String VALID_STRING_DATE = "2022-12-22";
	private static final LocalDate VALID_DATE = LocalDate.parse(VALID_STRING_DATE);

	@Nested
	@DisplayName("Tests for 'newValidatedReservation'")
	class NewValidatedReservationTest {

		@Test
		@DisplayName("Valid parameters")
		void testNewValidatedReservationWithConversionsWhenParametersAreValidShouldReturnReservation() {
			Reservation reservation = RestrictedReservationValidator
					.newValidatedReservation(VALID_CLIENT, VALID_STRING_DATE);
			
			assertAll(
				() -> assertThat(reservation.getClientUUID()).isEqualTo(VALID_UUID),
				() -> assertThat(reservation.getDate()).isEqualTo(VALID_DATE)
			);
		}

		@Nested
		@DisplayName("Null parameters")
		class NullParametersTest {

			@Test
			@DisplayName("Null client")
			void testNewValidatedReservationWhenClientIsNullShouldThrow() {
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(null, VALID_STRING_DATE))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null client.");
			}

			@Test
			@DisplayName("Null clientUUID")
			void testNewValidatedReservationWhenUUIDIsNullShouldThrow() {
				Client spied_client = spy(VALID_CLIENT);
				when(spied_client.getUuid()).thenReturn(null);
				
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(spied_client, VALID_STRING_DATE))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null client identifier.");
			}

			@Test
			@DisplayName("Null date")
			void testNewValidatedReservationWhenDateIsNullShouldThrow() {
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(VALID_CLIENT, null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a not null date.");
			}
		}

		@Nested
		@DisplayName("Invalid dates")
		class InvalidDatesTest {

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Non-numeric date")
			@ValueSource(strings = {
				"2022-12-dd", "2022-MM-21", "aAAa-12-21",			// alphabetic characters
				"2022-12-2@", "2022-1!-21", "2O?2-12-21",			// special characters
				"2022.12.21", "2022_12_21", "2022/12/21",			// different separators
				" 022-12-21", "2022-12-\t1", "2022-1\f-21",			// single space
				"\r022-12-21", "202\u2028-12-21", "2022-\r\n-21",	// single line separator
				"2022-12-2\u0085", "2\n22-12-21", "2022-1\u2029-21",// single line separator
				" 022-1 -21", "2022-12-\t\t", "2022-\f\f-21",		// double same space
				"202\r-12-\r1 ", "20\u0085\u0085-12-21",			// double line separator
				"2\n22-1\n-21", "2022-1\u2028-2\u2028",				// double line separator
				"2022-\r\n-\r\n", "202\u2029-1\u2029-21"			// double line separator
			})
			void testNewValidatedReservationWhenDateContainsNonNumericCharactersShouldThrow(
					String nonNumericDate) {
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(VALID_CLIENT, nonNumericDate))
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
			void testConstructorWithConversionsWhenStringDateFormatIsWrongShouldThrow(
					String wrongFormatDate) {
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(VALID_CLIENT, wrongFormatDate))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation needs a date in format aaaa-mm-dd.");
			}

			@ParameterizedTest(name = "{index}: ''{0}''")
			@DisplayName("Date out of range")
			@ValueSource(strings = {
				"2022-13-12",	// Month
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
			void testConstructorWithConversionsWhenDayInDateIsWrongShouldThrow(String outOfRangeDate) {
				assertThatThrownBy(() -> RestrictedReservationValidator
						.newValidatedReservation(VALID_CLIENT, outOfRangeDate))
					.isInstanceOf(IllegalArgumentException.class);
			}
		}
	}
}
