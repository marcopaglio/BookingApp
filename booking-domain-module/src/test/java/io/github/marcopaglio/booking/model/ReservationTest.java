package io.github.marcopaglio.booking.model;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for Reservation entity.")
class ReservationTest {

	@Nested
	@DisplayName("Check constructor inputs.")
	class ConstructorInput {
		@Test
		@DisplayName("Constructor with conversions.")
		void testConstructorWhenNeedParameterConversionsShouldSetRightValues() {
			Client client = new Client("Maurizio", "Pizzicotti");
			String stringDate = "2022-12-22";
			Reservation reservation = new Reservation(client, stringDate);
			
			assertThat(reservation.getClientUUID()).isEqualTo(client.getUUID());
			assertAll(
				() -> assertThat(reservation.getDate()).isEqualTo(LocalDate.parse(stringDate)),
				() -> assertThat(reservation.getDate().toString()).isEqualTo(stringDate),
				() -> assertThat(reservation.getDate().getYear()).isEqualTo(2022),
				() -> assertThat(reservation.getDate().getMonthValue()).isEqualTo(12),
				() -> assertThat(reservation.getDate().getDayOfMonth()).isEqualTo(22)
			);
		}
	}
}
