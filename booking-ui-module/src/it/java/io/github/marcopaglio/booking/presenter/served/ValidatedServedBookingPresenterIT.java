package io.github.marcopaglio.booking.presenter.served;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;
import io.github.marcopaglio.booking.view.BookingView;

@DisplayName("Integration tests for ServedBookingPresenter, RestrictedClientValidator "
		+ "and RestrictedReservationValidator")
class ValidatedServedBookingPresenterIT {

	private RestrictedClientValidator restrictedClientValidator;
	private RestrictedReservationValidator restrictedReservationValidator;

	@Mock
	private BookingService bookingService;

	@Mock
	private BookingView view;

	private ServedBookingPresenter presenter;

	private AutoCloseable closeable;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		restrictedClientValidator = new RestrictedClientValidator();
		restrictedReservationValidator = new RestrictedReservationValidator();
		
		presenter = new ServedBookingPresenter(view, bookingService,
				restrictedClientValidator, restrictedReservationValidator);
	}

	@AfterEach
	void tearDown() throws Exception {
		closeable.close();
	}

	@Nested
	@DisplayName("Integration tests for RestrictedClientValidator")
	class RestrictedClientValidatorIT {
		private final String validFirstName = "Mario";
		private final String validLastName = "Rossi";

		@Nested
		@DisplayName("Integration tests for 'addClient'")
		class AddClientIT {

			@Test
			@DisplayName("Valid names")
			void testAddClientWhenNamesAreValidShouldDelegateToServiceWithUnchangedNames() {
				Client clientWithValidNames = new Client(validFirstName, validLastName);
				
				when(bookingService.insertNewClient(clientWithValidNames))
					.thenReturn(clientWithValidNames);
				
				assertThatNoException().isThrownBy(() ->
					presenter.addClient(validFirstName, validLastName));
				
				verify(bookingService).insertNewClient(clientWithValidNames);
			}

			@Test
			@DisplayName("Names to fix")
			void testAddClientWhenNamesAreToFixShouldDelegateToServiceWithFixedNames() {
				String firstNameToFix = " Mario";
				String lastNameToFix = "Rossi   ";
				Client clientWithFixedNames = new Client(validFirstName, validLastName);
				
				when(bookingService.insertNewClient(clientWithFixedNames))
					.thenReturn(clientWithFixedNames);
				
				assertThatNoException().isThrownBy(() ->
					presenter.addClient(firstNameToFix, lastNameToFix));
				
				verify(bookingService).insertNewClient(clientWithFixedNames);
			}

			@Test
			@DisplayName("Invalid name")
			void testAddClientWhenNameIsNotValidShouldShowErrorAndNotInsert() {
				String invalidFirstName = "Mari0";
				
				presenter.addClient(invalidFirstName, validLastName);
				
				verify(view).showFormError("Client's name [" + invalidFirstName + "] is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}

			@Test
			@DisplayName("Invalid surname")
			void testAddClientWhenSurnameIsNotValidShouldShowErrorAndNotInsert() {
				String invalidLastName = "Rossi!";
				
				presenter.addClient(validFirstName, invalidLastName);
				
				verify(view).showFormError("Client's surname [" + invalidLastName + "] is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'renameClient'")
		class RenameClientIT {
			private String validNewFirstName = "Maria";
			private String validNewLastName = "De Lucia";
			private Client clientToRename;
			private Client renamedClient = new Client(validNewFirstName, validNewLastName);
			private UUID client_id = UUID.fromString("18a2795d-d4ef-4c90-ad8c-aafbda231262");

			@BeforeEach
			void setupClient() throws Exception {
				clientToRename = new Client(validFirstName, validLastName);
				clientToRename.setId(client_id);
			}

			@Test
			@DisplayName("Valid new names")
			void testRenameClientWhenNewNamesAreValidShouldDelegateToServiceWithUnchangedNewNames() {
				when(bookingService.renameClient(client_id, validNewFirstName, validNewLastName))
					.thenReturn(renamedClient);
				
				presenter.renameClient(clientToRename, validNewFirstName, validNewLastName);
				
				verify(bookingService).renameClient(client_id, validNewFirstName, validNewLastName);
			}

			@Test
			@DisplayName("New names to fix")
			void testRenameClientWhenNewNamesAreToFixShouldDelegateToServiceWithFixedNewNames() {
				String firstNameToFix = " Maria";
				String lastNameToFix = "De   Lucia";
				
				when(bookingService.renameClient(client_id, validNewFirstName, validNewLastName))
					.thenReturn(renamedClient);
				
				presenter.renameClient(clientToRename, firstNameToFix, lastNameToFix);
				
				verify(bookingService).renameClient(client_id, validNewFirstName, validNewLastName);
			}

			@Test
			@DisplayName("Invalid new name")
			void testRenameClientWhenNewNameIsNotValidShouldShowErrorAndNotRename() {
				String invalidNewFirstName = "";
				
				presenter.renameClient(clientToRename, invalidNewFirstName, validNewLastName);
				
				verify(view).showFormError("Client's name [" + invalidNewFirstName + "] is not valid.");
				verify(bookingService, never())
					.renameClient(same(client_id), anyString(), anyString());
			}

			@Test
			@DisplayName("Invalid new surname")
			void testRenameClientWhenNewSurnameIsNotValidShouldShowErrorAndNotRename() {
				String invalidNewLastName = "D3 Lucia";
				
				presenter.renameClient(clientToRename, validNewFirstName, invalidNewLastName);
				
				verify(view).showFormError("Client's surname [" + invalidNewLastName + "] is not valid.");
				verify(bookingService, never())
					.renameClient(same(client_id), anyString(), anyString());
			}
		}
	}

	@Nested
	@DisplayName("Integration tests for RestrictedReservationValidator")
	class RestrictedReservationValidatorIT {
		private final UUID validClientId = UUID.fromString("8a3408a8-b81e-4e42-bd64-01dd05238df5");
		private final String validDate = "2022-12-22";

		@Nested
		@DisplayName("Integration tests for 'addReservation'")
		class AddReservationIT {
			private Client client;

			@BeforeEach
			void setupClient() {
				client = new Client("Mario", "Rossi");
				client.setId(validClientId);
			}

			@Test
			@DisplayName("Valid inputs")
			void testAddReservationWhenInputsAreValidShouldDelegateToServiceWithUnchangedValues() {
				Reservation validReservation = new Reservation(validClientId, LocalDate.parse(validDate));
				
				when(bookingService.insertNewReservation(validReservation)).thenReturn(validReservation);
				
				presenter.addReservation(client, validDate);
				
				verify(bookingService).insertNewReservation(validReservation);
			}

			@Test
			@DisplayName("Invalid clientId")
			void testAddReservationWhenClientIdIsNotValidShouldShowErrorAndNotInsert() {
				UUID invalidId = null;
				client.setId(invalidId);
				
				presenter.addReservation(client, validDate);
				
				verify(view).showFormError("Reservation's client ID [" + invalidId + "] is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}

			@Test
			@DisplayName("Invalid date")
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndNotInsert() {
				String invalidDate = "2022-08-32";
				
				presenter.addReservation(client, invalidDate);
				
				verify(view).showFormError("Reservation's date [" + invalidDate + "] is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'rescheduleReservation'")
		class RescheduleReservationIT {
			private Reservation reservationToReschedule;
			private UUID reservation_id = UUID.fromString("04e55ea5-e3df-4dae-9c49-a681a2c00833");

			@BeforeEach
			void setupReservation() {
				reservationToReschedule = new Reservation(validClientId, LocalDate.parse(validDate));
				reservationToReschedule.setId(reservation_id);
			}

			@Test
			@DisplayName("Valid new date")
			void testRescheduleReservationWhenNewDateIsValidShouldDelegateToServiceWithUnchangedNewDate() {
				String validNewDate = "2023-09-05";
				LocalDate validNewLocalDate = LocalDate.parse(validNewDate);
				Reservation rescheduledReservation = new Reservation(validClientId, validNewLocalDate);
				
				when(bookingService.rescheduleReservation(reservation_id, validNewLocalDate))
					.thenReturn(rescheduledReservation);
				
				presenter.rescheduleReservation(reservationToReschedule, validNewDate);
				
				verify(bookingService).rescheduleReservation(reservation_id, validNewLocalDate);
			}

			@Test
			@DisplayName("Invalid new date")
			void testRescheduleReservationWhenNewDateIsNotValidShouldShowErrorAndNotReschedule() {
				String invalidNewDate = "2023-0J-05";
				
				presenter.rescheduleReservation(reservationToReschedule, invalidNewDate);
				
				verify(view).showFormError("Reservation's date [" + invalidNewDate + "] is not valid.");
				verify(bookingService, never())
					.rescheduleReservation(same(reservation_id), any(LocalDate.class));
			}
		}
	}
}