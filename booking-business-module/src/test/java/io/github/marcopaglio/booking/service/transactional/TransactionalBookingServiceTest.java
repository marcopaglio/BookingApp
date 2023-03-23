package io.github.marcopaglio.booking.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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
class TransactionalBookingServiceTest {

	private static final String A_FIRST_NAME = "Mario";
	private static final String A_LAST_NAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRST_NAME, A_LAST_NAME, new ArrayList<>());
	private static final UUID A_CLIENT_UUID = A_CLIENT.getUuid();
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

	private AutoCloseable closeable;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
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
				Client another_client = new Client("Maria", "De Lucia", new ArrayList<>());
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
			@DisplayName("Names input are null")
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
						"There is no client named \"" + A_FIRST_NAME + " "
					+ A_LAST_NAME + "\" in the database.");
				
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
			@DisplayName("Client input is null")
			void testInsertNewClientWhenClientIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewClient(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Client to insert cannot be null.");
				
				verify(transactionManager, never()).doInTransaction(
						ArgumentMatchers.<ClientTransactionCode<?>>any());
			}

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

			@DisplayName("No reservations to retrieve")
			@Test
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

			@DisplayName("Single reservation to retrieve")
			@Test
			void testFindAllReservationsWhenThereIsASingleReservationToRetrieveShouldReturnTheReservationAsList() {
				List<Reservation> reservations = Arrays.asList(A_RESERVATION);
				when(reservationRepository.findAll()).thenReturn(reservations);
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThat(transactionalBookingService.findAllReservations()).isEqualTo(reservations);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findAll();
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
			}

			@DisplayName("Several reservations to retrieve")
			@Test
			void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationsAsList() {
				LocalDate another_localdate = LocalDate.parse("2023-03-16");
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

			@DisplayName("Date input is null")
			@Test
			void testFindReservationOnWhenDateIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.findReservationOn(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Date of reservation to find cannot be null.");
				
				verify(transactionManager, never()).doInTransaction(
						ArgumentMatchers.<ReservationTransactionCode<?>>any());
			}

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

			@DisplayName("Reservation doesn't exist")
			@Test
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
			private final UUID clientUUID = A_CLIENT.getUuid();
			private final LocalDate a_localdate = LocalDate.parse("2023-04-24");
			private final Reservation a_reservation = new Reservation(clientUUID, a_localdate);

			@Test
			@DisplayName("Names input are null")
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
				inOrder.verify(reservationRepository).findByClient(clientUUID);
				inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
			}

			@Test
			@DisplayName("Client exists with single existing reservation")
			void testRemoveClientNamedWhenClientExistsWithSingleExistingReservationShouldRemove() {
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				when(reservationRepository.findByClient(clientUUID))
					.thenReturn(Arrays.asList(a_reservation));
				// default stubbing for reservationRepository.findByClient(clientUUID)
				// default stubbing for clientRepository.delete(firstName, lastName)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, clientRepository, reservationRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(reservationRepository).findByClient(clientUUID);
				inOrder.verify(reservationRepository).delete(a_localdate);
				inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
				
				verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
			}

			@Test
			@DisplayName("Client exists with several existing reservations")
			void testRemoveClientNamedWhenClientExistsWithSeveralExistingReservationsShouldRemove() {
				LocalDate another_localdate = LocalDate.parse("2023-03-16");
				Reservation another_reservation = new Reservation(clientUUID, another_localdate);
				List<Reservation> several_reservation_list =
						new ArrayList<>(Arrays.asList(a_reservation, another_reservation));
				
				when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME))
					.thenReturn(Optional.of(A_CLIENT));
				when(reservationRepository.findByClient(clientUUID)).thenReturn(several_reservation_list);
				// default stubbing for reservationRepository.delete(date)
				// default stubbing for clientRepository.delete(firstName, lastName)
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, clientRepository, reservationRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
				inOrder.verify(reservationRepository).findByClient(clientUUID);
				// TODO: inOrder.verify(reservationRepository, times(2)).delete(isA(LocalDate.class));
				inOrder.verify(reservationRepository).delete(a_localdate);
				inOrder.verify(reservationRepository).delete(another_localdate);
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

			@DisplayName("Reservation input is null")
			@Test
			void testInsertNewReservationWhenReservationIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewReservation(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Reservation to insert cannot be null.");
				
				verify(transactionManager, never()).doInTransaction(
						ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			}

			@DisplayName("Reservation is new and client exists")
			@Test
			void testInsertNewReservationWhenReservationDoesNotAlreadyExistAndAssociatedClientExistsShouldUpdateInsertAndReturn() {
				Client spied_client = spy(A_CLIENT);
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.of(spied_client));
				doNothing().when(spied_client).addReservation(A_RESERVATION); // prevent bad behaviors
				when(clientRepository.save(spied_client)).thenReturn(spied_client); // TODO: serve?
				when(reservationRepository.save(A_RESERVATION)).thenReturn(A_RESERVATION);
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository, spied_client);
				
				assertThat(transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isEqualTo(A_RESERVATION);
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
				inOrder.verify(spied_client).addReservation(A_RESERVATION);
				inOrder.verify(clientRepository).save(spied_client);
				inOrder.verify(reservationRepository).save(A_RESERVATION);
			
				verifyNoMoreInteractions(
						transactionManager, reservationRepository, clientRepository, spied_client);
			}

			@DisplayName("Reservation is new and client doesn't exist")
			@Test
			void testInsertNewReservationWhenReservationDoesNotAlreadyExistAndAssociatedClientDoesNotExistShouldNotInsertAndThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage(
						"The client with uuid: " + A_CLIENT_UUID + ", associated to the reservation to insert "
						+ "is not in the database. Please, insert the client before the reservation.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
			
				verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
			}

			@DisplayName("Reservation already exists")
			@Test
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

		@Nested
		@DisplayName("Tests for 'removeReservationOn'")
		class RemoveReservationOnTest {

			@DisplayName("Date input is null")
			@Test
			void testRemoveReservationOnWhenDateIsNullShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.removeReservationOn(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Date of reservation to remove cannot be null.");
				
				verify(transactionManager, never()).doInTransaction(
						ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			}

			@DisplayName("Both reservation and client exist")
			@Test
			void testRemoveReservationOnWhenBothReservationAndAssociatedClientExistShouldRemoveAndUpdate() {
				Client spied_client = spy(A_CLIENT);
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.of(A_RESERVATION));
				// default stubbing for reservationRepository.delete(reservation)
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.of(spied_client));
				doNothing().when(spied_client).removeReservation(A_RESERVATION); // prevent bad behaviors
				when(clientRepository.save(spied_client)).thenReturn(spied_client); // TODO: serve?
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository, spied_client);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeReservationOn(A_LOCALDATE));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(reservationRepository).delete(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
				inOrder.verify(spied_client).removeReservation(A_RESERVATION);
				inOrder.verify(clientRepository).save(spied_client);
				
				verifyNoMoreInteractions(
						transactionManager, reservationRepository, clientRepository, spied_client);
			}

			@DisplayName("Reservation exists and client doesn't exist")
			@Test
			void testRemoveReservationOnWhenReservationExistsAndAssociatedClientDoesNotExistShouldRemoveAndNotThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.of(A_RESERVATION));
				// default stubbing for reservationRepository.delete(reservation)
				when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(
						transactionManager, reservationRepository, clientRepository);
				
				assertThatNoException().isThrownBy(
						() -> transactionalBookingService.removeReservationOn(A_LOCALDATE));
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				inOrder.verify(reservationRepository).delete(A_LOCALDATE);
				inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
			}

			@DisplayName("Reservation doesn't exist")
			@Test
			void testRemoveReservationOnWhenReservationDoesNotExistShouldThrow() {
				when(reservationRepository.findByDate(A_LOCALDATE)).thenReturn(Optional.empty());
				
				InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
				
				assertThatThrownBy(
						() -> transactionalBookingService.removeReservationOn(A_LOCALDATE))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage("There is no reservation on \"" + A_LOCALDATE + "\" in the database.");
				
				inOrder.verify(transactionManager)
					.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
				inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				
				verifyNoMoreInteractions(transactionManager, reservationRepository);
				verifyNoInteractions(clientRepository);
			}
		}
	}

	@AfterEach
	void releaseMocks() throws Exception {
		closeable.close();
	}
}
