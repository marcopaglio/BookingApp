package io.github.marcopaglio.booking.presenter.served;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
		private String validFirstName = "Maria";
		private String validLastName = "De Lucia";
		private Client client = new Client(validFirstName, validLastName);

		@Nested
		@DisplayName("Integration tests for 'addClient'")
		class AddClientIT {

			@Test
			@DisplayName("Valid names")
			void testAddClientWhenNamesAreValidShouldDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewClient(client)).thenReturn(client);
				
				presenter.addClient(validFirstName, validLastName);
				
				verify(bookingService).insertNewClient(client);
				verify(view).clientAdded(client);
			}

			@Test
			@DisplayName("Fixed names")
			void testAddClientWhenNamesAreFixedShouldDelegateToServiceAndNotifyView() {
				String firstNameToFix = " Maria";
				String lastNameToFix = "De   Lucia";
				
				when(bookingService.insertNewClient(client)).thenReturn(client);
				
				presenter.addClient(firstNameToFix, lastNameToFix);
				
				verify(bookingService).insertNewClient(client);
				verify(view).clientAdded(client);
			}

			@Test
			@DisplayName("Name is not valid")
			void testAddClientWhenNameIsNotValidShouldShowErrorAndNotInsert() {
				String invalidFirstName = "";
				
				presenter.addClient(invalidFirstName, validLastName);
				
				verify(view).showFormError("Client's name is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}

			@Test
			@DisplayName("Surname is not valid")
			void testAddClientWhenSurnameIsNotValidShouldShowErrorAndNotInsert() {
				String invalidLastName = "D3 Lucia";
				
				presenter.addClient(validFirstName, invalidLastName);
				
				verify(view).showFormError("Client's surname is not valid.");
				verify(bookingService, never()).insertNewClient(any(Client.class));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'renameClient'")
		class RenameClientIT {
			private String validNewFirstName = "Mario";
			private String validNewLastName = "Rossi";

			@Test
			@DisplayName("New names are valid")
			void testRenameClientWhenNewNamesAreValidShouldDelegateToServiceAndNotifyView() {
				UUID client_id = UUID.fromString("18a2795d-d4ef-4c90-ad8c-aafbda231262");
				client.setId(client_id);
				Client renamedClient = new Client(validNewFirstName, validNewLastName);
				
				when(bookingService.renameClient(client_id, validNewFirstName, validNewLastName))
					.thenReturn(renamedClient);
				
				presenter.renameClient(client, validNewFirstName, validNewLastName);
				
				verify(bookingService).renameClient(client_id, validNewFirstName, validNewLastName);
				verify(view).clientRenamed(client, renamedClient);
			}

			@Test
			@DisplayName("New name is not valid")
			void testRenameClientWhenNewNameIsNotValidShouldShowErrorAndNotRename() {
				String invalidNewFirstName = "Mari0";
				
				presenter.renameClient(client, invalidNewFirstName, validNewLastName);
				
				verify(view).showFormError("Client's name is not valid.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}

			@Test
			@DisplayName("New surname is not valid")
			void testRenameClientWhenNewSurnameIsNotValidShouldShowErrorAndNotRename() {
				String invalidNewLastName = "Rossi!";
				
				presenter.renameClient(client, validNewFirstName, invalidNewLastName);
				
				verify(view).showFormError("Client's surname is not valid.");
				verify(bookingService, never())
					.renameClient(any(UUID.class), anyString(), anyString());
			}
		}
	}

	@Nested
	@DisplayName("Integration tests for RestrictedReservationValidator")
	class RestrictedReservationValidatorIT {
		UUID validClientId = UUID.fromString("8a3408a8-b81e-4e42-bd64-01dd05238df5");
		String validDate = "2022-12-22";
		Reservation reservation;
		Client client;
		
		@BeforeEach
		void setupEntities() {
			client = new Client("Mario", "Rossi");
			client.setId(validClientId);
			reservation = new Reservation(validClientId, LocalDate.parse(validDate));
		}

		@Nested
		@DisplayName("Integration tests for 'addReservation'")
		class AddReservationIT {

			@Test
			@DisplayName("Valid inputs")
			void testAddReservationWhenInputsAreValidShouldDelegateToServiceAndNotifyView() {
				when(bookingService.insertNewReservation(reservation))
					.thenReturn(reservation);
				
				presenter.addReservation(client, validDate);
				
				verify(bookingService).insertNewReservation(reservation);
				verify(view).reservationAdded(reservation);
			}

			@Test
			@DisplayName("ClientId is not valid")
			void testAddReservationWhenClientIdIsNotValidShouldShowErrorAndNotInsert() {
				client.setId(null);
				
				presenter.addReservation(client, validDate);
				
				verify(view).showFormError("Client's identifier associated with reservation is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}

			@Test
			@DisplayName("Date is not valid")
			void testAddReservationWhenDateIsNotValidShouldShowErrorAndNotInsert() {
				String invalidDate = "2022-08-32";
				
				presenter.addReservation(client, invalidDate);
				
				verify(view).showFormError("Reservation's date is not valid.");
				verify(bookingService, never()).insertNewReservation(any(Reservation.class));
			}
		}

		@Nested
		@DisplayName("Integration tests for 'rescheduleReservation'")
		class RescheduleReservationIT {

			@Test
			@DisplayName("New date is valid")
			void testRescheduleReservationWhenNewDateIsValidShouldDelegateToServiceAndNotifyView() {
				UUID reservation_id = UUID.fromString("04e55ea5-e3df-4dae-9c49-a681a2c00833");
				reservation.setId(reservation_id);
				String validNewDate = "2023-09-05";
				LocalDate validNewLocalDate = LocalDate.parse(validNewDate);
				Reservation rescheduledReservation = new Reservation(validClientId, validNewLocalDate);
				
				when(bookingService.rescheduleReservation(reservation_id, validNewLocalDate))
					.thenReturn(rescheduledReservation);
				
				presenter.rescheduleReservation(reservation, validNewDate);
				
				verify(bookingService).rescheduleReservation(reservation_id, validNewLocalDate);
				verify(view).reservationRescheduled(reservation, rescheduledReservation);
			}

			@Test
			@DisplayName("New date is not valid")
			void testRescheduleReservationWhenNewDateIsNotValidShouldShowErrorAndNotReschedule() {
				String invalidNewDate = "2023-0J-05";
				
				presenter.rescheduleReservation(reservation, invalidNewDate);
				
				verify(view).showFormError("Reservation's date is not valid.");
				verify(bookingService, never())
					.rescheduleReservation(any(UUID.class), any(LocalDate.class));
			}
		}
	}
}