package io.github.marcopaglio.booking.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

@DisplayName("Tests for TransactionalBookingService class")
@ExtendWith(MockitoExtension.class)
class TransactionalBookingServiceTest {
	private static final String A_FIRST_NAME = "Mario";
	private static final String A_LAST_NAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRST_NAME, A_LAST_NAME);
	private static final UUID A_CLIENT_UUID = A_CLIENT.getId();
	private static final LocalDate A_LOCALDATE = LocalDate.parse("2023-04-24");
	private static final Reservation A_RESERVATION = new Reservation(A_CLIENT_UUID, A_LOCALDATE);

	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private ClientRepository clientRepository;
	
	@Mock
	private ReservationRepository reservationRepository;
	
	@InjectMocks
	private TransactionalBookingService transactionalBookingService;
	
	@Nested
	@DisplayName("Null inputs on methods")
	class NullInputTest {

		@Test
		@DisplayName("Null names on 'findClientNamed'")
		void testFindClientNamedWhenNamesAreNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.findClientNamed(null, A_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.findClientNamed(A_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.findClientNamed(null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ClientTransactionCode<?>>any());
		}

		@Test
		@DisplayName("Null client on 'insertNewClient'")
		void testInsertNewClientWhenClientIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.insertNewClient(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to insert cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ClientTransactionCode<?>>any());
		}

		@Test
		@DisplayName("Null date on 'findReservationOn'")
		void testFindReservationOnWhenDateIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.findReservationOn(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Date of reservation to find cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ReservationTransactionCode<?>>any());
		}

		@Test
		@DisplayName("Null names on 'removeClientNamed'")
		void testRemoveClientNamedWhenNamesAreNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.removeClientNamed(null, A_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.removeClientNamed(null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
		}

		@Test
		@DisplayName("Null reservation on 'insertNewReservation'")
		void testInsertNewReservationWhenReservationIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.insertNewReservation(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to insert cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
		}

		@Test
		@DisplayName("Null date on 'removeReservationOn'")
		void testRemoveReservationOnWhenDateIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalBookingService.removeReservationOn(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Date of reservation to remove cannot be null.");
			
			verify(transactionManager, never()).doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
		}
	}

	@Nested
	@DisplayName("Methods using only ClientRepository")
	class ClientRepositoryTest {

		@BeforeEach
		void doStubbing() throws Exception {
			// make sure the lambda passed to the TransactionManager
			// is executed, using the mock repository
			when(transactionManager.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any()))
				.thenAnswer(
					answer((ClientTransactionCode<?> code) -> code.apply(clientRepository)));
		}

		@Nested
		@DisplayName("Tests for 'findAllClients'")
		class FindAllClientsTest {

			@Test
			@DisplayName("No clients to retrieve")
			void testFindAllClientsWhenThereAreNotClientsToRetrieveShouldReturnEmptyList() {
				// default stubbing for clientRepository.findAll()
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				// verify state
				assertThat(transactionalBookingService.findAllClients()).isEqualTo(new ArrayList<>());
				
				// verify interactions
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}

			@Test
			@DisplayName("Single client to retrieve")
			void testFindAllClientsWhenThereIsASingleClientToRetrieveShouldReturnTheClientAsList() {
				List<Client> clients = Arrays.asList(A_CLIENT);
				when(clientRepository.findAll()).thenReturn(clients);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThat(transactionalBookingService.findAllClients()).isEqualTo(clients);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}

			@Test
			@DisplayName("Several clients to retrieve")
			void testFindAllClientsWhenThereAreSeveralClientsToRetrieveShouldReturnClientsAsList() {
				Client another_client = new Client("Maria", "De Lucia");
				List<Client> clients = Arrays.asList(A_CLIENT, another_client);
				when(clientRepository.findAll()).thenReturn(clients);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThat(transactionalBookingService.findAllClients()).isEqualTo(clients);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}
		}

		@Nested
		@DisplayName("Tests for 'findClientNamed'")
		class FindClientNamedTest {

			@Test
			@DisplayName("Client exists")
			void testFindClientNamedWhenClientExistsShouldReturnTheClient() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThat(transactionalBookingService.findClientNamed(A_FIRST_NAME, A_LAST_NAME))
					.isEqualTo(A_CLIENT);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testFindClientNamedWhenClientDoesNotExistShouldThrow() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.findClientNamed(A_FIRST_NAME, A_LAST_NAME))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage(
						"There is no client named \"" + A_FIRST_NAME
						+ " " + A_LAST_NAME + "\" in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}
		}

		@Nested
		@DisplayName("Tests for 'insertNewClient'")
		class InsertNewClientTest {

			@Test
			@DisplayName("Client is new")
			void testInsertNewClientWhenClientDoesNotAlreadyExistShouldInsertAndReturn() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
				when(clientRepository.save(A_CLIENT)).thenReturn(A_CLIENT);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThat(transactionalBookingService.insertNewClient(A_CLIENT)).isEqualTo(A_CLIENT);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(clientRepository).save(A_CLIENT);
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}

			@Test
			@DisplayName("Client already exists")
			void testInsertNewClientWhenClientAlreadyExistsShouldThrow() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewClient(A_CLIENT))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(
						"Client [" + A_FIRST_NAME + " " + A_LAST_NAME + "] is already in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
			}
		}
	}

	@Nested
	@DisplayName("Methods using only ReservationRepository")
	class ReservationRepositoryTest {

		@BeforeEach
		void doStubbing() throws Exception {
			// make sure the lambda passed to the TransactionManager
			// is executed, using the mock repository
			when(transactionManager
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any()))
				.thenAnswer(
					answer((ReservationTransactionCode<?> code) -> code.apply(reservationRepository)));
		}

		@Nested
		@DisplayName("Tests for 'findAllReservations'")
		class FindAllReservationsTest {

			@Test
			@DisplayName("No reservations to retrieve")
			void testFindAllReservationsWhenThereAreNotReservationsToRetrieveShouldReturnEmptyList() {
				// default stubbing for reservationRepository.findAll()
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThat(transactionalBookingService.findAllReservations())
					.isEqualTo(new ArrayList<>());
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}

			@Test
			@DisplayName("Single reservation to retrieve")
			void testFindAllReservationsWhenThereIsASingleReservationToRetrieveShouldReturnReservationAsList() {
				List<Reservation> reservations = Arrays.asList(A_RESERVATION);
				when(reservationRepository.findAll()).thenReturn(reservations);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThat(transactionalBookingService.findAllReservations()).isEqualTo(reservations);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}

			@Test
			@DisplayName("Several reservations to retrieve")
			void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationsAsList() {
				LocalDate another_localdate = LocalDate.parse("2023-09-05");
				Reservation another_reservation = new Reservation(UUID.randomUUID(), another_localdate);
				List<Reservation> reservations = Arrays.asList(A_RESERVATION, another_reservation);
				when(reservationRepository.findAll()).thenReturn(reservations);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThat(transactionalBookingService.findAllReservations()).isEqualTo(reservations);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}
		}

		@Nested
		@DisplayName("Tests for 'findReservationOn'")
		class FindReservationOnTest {

			@DisplayName("Reservation exists")
			@Test
			void testFindReservationOnWhenReservationExistsShouldReturnTheReservation() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.of(A_RESERVATION));
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThat(transactionalBookingService.findReservationOn(A_LOCALDATE))
					.isEqualTo(A_RESERVATION);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testFindReservationOnWhenReservationDoesNotExistShouldThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.findReservationOn(A_LOCALDATE))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage("There is no reservation on \"" + A_LOCALDATE + "\" in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}
		}

		@Nested
		@DisplayName("Tests for 'removeReservationOn'")
		class RemoveReservationOnTest {

			@Test
			@DisplayName("Reservation exists")
			void testRemoveReservationOnWhenReservationExistsShouldRemove() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.of(A_RESERVATION));
				// default stubbing for reservationRepository.delete(reservation)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeReservationOn(A_LOCALDATE));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(reservationRepository).delete(A_LOCALDATE);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}

			@Test
			@DisplayName("Reservation doesn't exist")
			void testRemoveReservationOnWhenReservationDoesNotExistShouldThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.removeReservationOn(A_LOCALDATE))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage("There is no reservation on \"" + A_LOCALDATE + "\" in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}
		}
	}

	@Nested
	@DisplayName("Methods using both repositories")
	class BothRepositoriesTest {

		@BeforeEach
		void doStubbing() throws Exception {
			when(transactionManager.doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any()))
			.thenAnswer(
				answer((ClientReservationTransactionCode<?> code) -> 
					code.apply(clientRepository, reservationRepository)));
		}

		@Nested
		@DisplayName("Tests for 'removeClientNamed'")
		class RemoveClientNamedTest {

			@Test
			@DisplayName("Client exists without reservations")
			void testRemoveClientNamedWhenClientExistsWithoutExistingReservationsShouldRemove() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				// default stubbing for reservationRepository.findByClient(clientUUID)
				// default stubbing for clientRepository.delete(firstName, lastName)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, clientRepository, reservationRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
				inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
			}

			@Test
			@DisplayName("Client exists with single existing reservation")
			void testRemoveClientNamedWhenClientExistsWithSingleExistingReservationShouldRemove() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				when(reservationRepository.findByClient(A_CLIENT_UUID))
					.thenReturn(Arrays.asList(A_RESERVATION));
				// default stubbing for reservationRepository.findByClient(clientUUID)
				// default stubbing for clientRepository.delete(firstName, lastName)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, clientRepository, reservationRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
				inOrder.verify(reservationRepository).delete(A_LOCALDATE);
				inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
			}

			@Test
			@DisplayName("Client exists with several existing reservations")
			void testRemoveClientNamedWhenClientExistsWithSeveralExistingReservationsShouldRemove() {
				LocalDate another_localdate = LocalDate.parse("2023-03-16");
				Reservation another_reservation = new Reservation(A_CLIENT_UUID, another_localdate);
				List<Reservation> several_reservation_list =
						new ArrayList<>(Arrays.asList(A_RESERVATION, another_reservation));
				
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				when(reservationRepository.findByClient(A_CLIENT_UUID)).thenReturn(several_reservation_list);
				// default stubbing for reservationRepository.delete(date)
				// default stubbing for clientRepository.delete(firstName, lastName)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, clientRepository, reservationRepository);
				
				ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
				inOrder.verify(reservationRepository, times(2)).delete(dateCaptor.capture());
				assertThat(dateCaptor.getAllValues()).containsExactly(A_LOCALDATE, another_localdate);
				inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
			}

			@Test
			@DisplayName("Client doesn't exist")
			void testRemoveClientNamedWhenClientDoesNotExistShouldThrow() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage(
						"There is no client named \"" + A_FIRST_NAME + " "
						+ A_LAST_NAME + "\" in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository);
				verifyNoInteractions(reservationRepository);
			}
		}

		@Nested
		@DisplayName("Tests for 'insertNewReservation'")
		class InsertNewReservationTest {

			@DisplayName("Reservation is new and client exists")
			@Test
			void testInsertNewReservationWhenReservationDoesNotAlreadyExistAndAssociatedClientExistsShouldUpdateInsertAndReturn() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.of(A_CLIENT));
				when(reservationRepository.save(A_RESERVATION)).thenReturn(A_RESERVATION);
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository);
				
				assertThat(transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isEqualTo(A_RESERVATION);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
				inOrder.verify(reservationRepository).save(A_RESERVATION);
			
				verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
			}

			@Test
			@DisplayName("Reservation is new and client doesn't exist")
			void testInsertNewReservationWhenReservationDoesNotAlreadyExistAndAssociatedClientDoesNotExistShouldNotInsertAndThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage(
						"The client with id: " + A_CLIENT_UUID + ", associated to the reservation to insert "
						+ "is not in the database. Please, insert the client before the reservation.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
			
				verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
			}

			@Test
			@DisplayName("Reservation already exists")
			void testInsertNewReservationWhenReservationAlreadyExistsShouldThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.of(A_RESERVATION));
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isInstanceOf(InstanceAlreadyExistsException.class)
					.hasMessage(
						"Reservation [date=" + A_LOCALDATE.toString() + "] is already in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
			
				verifyNoMoreInteractions(transactionManager, reservationRepository);
				verifyNoInteractions(clientRepository);
			}
		}
	}
}
