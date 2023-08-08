package io.github.marcopaglio.booking.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import io.github.marcopaglio.booking.exception.DatabaseException;
import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.exception.InstanceNotFoundException;
import io.github.marcopaglio.booking.exception.TransactionException;
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
	private static final String A_FIRSTNAME = "Mario";
	private static final String A_LASTNAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRSTNAME, A_LASTNAME);

	private static final UUID A_CLIENT_UUID = UUID.fromString("bc49bffa-0766-4e5d-90af-d8a6ef516df4");
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
					() -> transactionalBookingService.findClientNamed(null, A_LASTNAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.findClientNamed(A_FIRSTNAME, null))
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
					() -> transactionalBookingService.removeClientNamed(null, A_LASTNAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalBookingService.removeClientNamed(A_FIRSTNAME, null))
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

		@Nested
		@DisplayName("Transaction is successful")
		class TransactionSuccessfulTest {

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
				void testFindAllClientsWhenThereAreNoClientsToRetrieveShouldReturnEmptyList() {
					// default stubbing for clientRepository.findAll()
					
					// verify state
					assertThat(transactionalBookingService.findAllClients())
						.isEqualTo(new ArrayList<>());
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					// verify interactions
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
					inOrder.verify(clientRepository).findAll();
					
					verifyNoMoreInteractions(transactionManager, clientRepository);
				}

				@Test
				@DisplayName("Several clients to retrieve")
				void testFindAllClientsWhenThereAreSeveralClientsToRetrieveShouldReturnClientsAsList() {
					List<Client> clients = Arrays.asList(A_CLIENT, new Client("Maria", "De Lucia"));
					
					when(clientRepository.findAll()).thenReturn(clients);
					
					assertThat(transactionalBookingService.findAllClients()).isEqualTo(clients);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
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
					when(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.thenReturn(Optional.of(A_CLIENT));
					
					assertThat(transactionalBookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME))
						.isEqualTo(A_CLIENT);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					
					verifyNoMoreInteractions(transactionManager, clientRepository);
				}

				@Test
				@DisplayName("Client doesn't exist")
				void testFindClientNamedWhenClientDoesNotExistShouldThrow() {
					// default stubbing for clientRepository.findByName(firstName, lastName)
					
					assertThatThrownBy(
							() -> transactionalBookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME))
						.isInstanceOf(InstanceNotFoundException.class)
						.hasMessage(
							"There is no client named \"" + A_FIRSTNAME
							+ " " + A_LASTNAME + "\" in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					
					verifyNoMoreInteractions(transactionManager, clientRepository);
				}
			}

			@Nested
			@DisplayName("Tests for 'insertNewClient'")
			class InsertNewClientTest {

				@Test
				@DisplayName("Client is new")
				void testInsertNewClientWhenClientDoesNotAlreadyExistShouldInsertAndReturn() {
					// default stubbing for clientRepository.findByName(firstName, lastName)
					when(clientRepository.save(A_CLIENT)).thenReturn(A_CLIENT);
					
					assertThat(transactionalBookingService.insertNewClient(A_CLIENT))
						.isEqualTo(A_CLIENT);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					inOrder.verify(clientRepository).save(A_CLIENT);
					
					verifyNoMoreInteractions(transactionManager, clientRepository);
				}

				@Test
				@DisplayName("Client already exists")
				void testInsertNewClientWhenClientAlreadyExistsShouldThrow() {
					when(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.thenReturn(Optional.of(A_CLIENT));
					
					assertThatThrownBy(
							() -> transactionalBookingService.insertNewClient(A_CLIENT))
						.isInstanceOf(InstanceAlreadyExistsException.class)
						.hasMessage(
							"Client [" + A_FIRSTNAME + " " + A_LASTNAME + "] is already in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					
					verify(clientRepository, never()).save(A_CLIENT);
					verifyNoMoreInteractions(transactionManager, clientRepository);
				}
			}
		}

		@Nested
		@DisplayName("Transaction is failure")
		class TransactionFailureTest {

			@BeforeEach
			void doStubbing() throws Exception {
				when(transactionManager.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any()))
					.thenThrow(new TransactionException());
			}

			@Test
			@DisplayName("Transaction fails on 'findAllClients'")
			void testFindAllClientsWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(() -> transactionalBookingService.findAllClients())
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}

			@Test
			@DisplayName("Transaction fails on 'findClientNamed'")
			void testFindClientNamedWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.findClientNamed(A_FIRSTNAME, A_LASTNAME))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}

			@Test
			@DisplayName("Transaction fails on 'insertNewClient'")
			void testInsertNewClientWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewClient(A_CLIENT))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}
		}
	}

	@Nested
	@DisplayName("Methods using only ReservationRepository")
	class ReservationRepositoryTest {

		@Nested
		@DisplayName("Transaction is successful")
		class TransactionSuccessfulTest {

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
					
					assertThat(transactionalBookingService.findAllReservations())
						.isEqualTo(new ArrayList<>());
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findAll();
					
					verifyNoMoreInteractions(transactionManager, reservationRepository);
				}
	
				@Test
				@DisplayName("Several reservations to retrieve")
				void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieveShouldReturnReservationsAsList() {
					List<Reservation> reservations = Arrays.asList(
							A_RESERVATION, new Reservation(A_CLIENT_UUID, LocalDate.parse("2023-09-05")));
					
					when(reservationRepository.findAll()).thenReturn(reservations);
					
					assertThat(transactionalBookingService.findAllReservations())
						.isEqualTo(reservations);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
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
					when(reservationRepository.findByDate(A_LOCALDATE))
						.thenReturn(Optional.of(A_RESERVATION));
					
					assertThat(transactionalBookingService.findReservationOn(A_LOCALDATE))
						.isEqualTo(A_RESERVATION);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
					
					verifyNoMoreInteractions(transactionManager, reservationRepository);
				}
	
				@Test
				@DisplayName("Reservation doesn't exist")
				void testFindReservationOnWhenReservationDoesNotExistShouldThrow() {
					// default stubbing for reservationRepository.findByDate(date)
					
					assertThatThrownBy(
							() -> transactionalBookingService.findReservationOn(A_LOCALDATE))
						.isInstanceOf(InstanceNotFoundException.class)
						.hasMessage("There is no reservation on \"" + A_LOCALDATE + "\" in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
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
					when(reservationRepository.findByDate(A_LOCALDATE))
						.thenReturn(Optional.of(A_RESERVATION));
					
					transactionalBookingService.removeReservationOn(A_LOCALDATE);
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
					inOrder.verify(reservationRepository).delete(A_RESERVATION);
					
					verifyNoMoreInteractions(transactionManager, reservationRepository);
				}
	
				@Test
				@DisplayName("Reservation doesn't exist")
				void testRemoveReservationOnWhenReservationDoesNotExistShouldThrow() {
					// default stubbing for reservationRepository.findByDate(date)
					
					assertThatThrownBy(
							() -> transactionalBookingService.removeReservationOn(A_LOCALDATE))
						.isInstanceOf(InstanceNotFoundException.class)
						.hasMessage("There is no reservation on \"" + A_LOCALDATE + "\" in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
					
					verifyNoMoreInteractions(transactionManager, reservationRepository);
				}
			}
		}

		@Nested
		@DisplayName("Transaction is failure")
		class TransactionFailureTest {

			@BeforeEach
			void doStubbing() throws Exception {
				when(transactionManager.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any()))
					.thenThrow(new TransactionException());
			}

			@Test
			@DisplayName("Transaction fails on 'findAllReservations'")
			void testFindAllReservationsWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(() -> transactionalBookingService.findAllReservations())
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}

			@Test
			@DisplayName("Transaction fails on 'findReservationOn'")
			void testFindReservationOnWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(() -> transactionalBookingService.findReservationOn(A_LOCALDATE))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}

			@Test
			@DisplayName("Transaction fails on 'removeReservationOn'")
			void testRemoveReservationOnWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(() -> transactionalBookingService.removeReservationOn(A_LOCALDATE))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}
		}
	}

	@Nested
	@DisplayName("Methods using both repositories")
	class BothRepositoriesTest {

		@Nested
		@DisplayName("Transaction is successful")
		class TransactionSuccessfulTest {
			private final Client spiedClient = spy(A_CLIENT);

			@BeforeEach
			void doStubbing() throws Exception {
				when(transactionManager.doInTransaction(
						ArgumentMatchers.<ClientReservationTransactionCode<?>>any()))
				.thenAnswer(
					answer((ClientReservationTransactionCode<?> code) -> 
						code.apply(clientRepository, reservationRepository)));
				
				when(spiedClient.getId()).thenReturn(A_CLIENT_UUID);
			}

			@Nested
			@DisplayName("Tests for 'removeClientNamed'")
			class RemoveClientNamedTest {

				@Test
				@DisplayName("Client exists without reservations")
				void testRemoveClientNamedWhenClientExistsWithoutExistingReservationsShouldRemove() {
					when(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.thenReturn(Optional.of(spiedClient));
					// default stubbing for reservationRepository.findByClient(clientUUID)
					
					transactionalBookingService.removeClientNamed(A_FIRSTNAME, A_LASTNAME);
					
					InOrder inOrder = Mockito.inOrder(
							transactionManager, clientRepository, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
					inOrder.verify(clientRepository).delete(spiedClient);
					
					verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
				}

				@Test
				@DisplayName("Client exists with single existing reservation")
				void testRemoveClientNamedWhenClientExistsWithSingleExistingReservationShouldRemove() {
					when(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.thenReturn(Optional.of(spiedClient));
					when(reservationRepository.findByClient(A_CLIENT_UUID))
						.thenReturn(Arrays.asList(A_RESERVATION));
					
					transactionalBookingService.removeClientNamed(A_FIRSTNAME, A_LASTNAME);
					
					InOrder inOrder = Mockito.inOrder(
							transactionManager, clientRepository, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
					inOrder.verify(reservationRepository).delete(A_RESERVATION);
					inOrder.verify(clientRepository).delete(spiedClient);
					
					verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
				}

				@Test
				@DisplayName("Client exists with several existing reservations")
				void testRemoveClientNamedWhenClientExistsWithSeveralExistingReservationsShouldRemove() {
					Reservation anotherReservation = new Reservation(
							A_CLIENT_UUID, LocalDate.parse("2023-03-16"));
					List<Reservation> severalReservationList = new ArrayList<>(Arrays.asList(
							A_RESERVATION, anotherReservation));
					
					when(clientRepository.findByName(A_FIRSTNAME, A_LASTNAME))
						.thenReturn(Optional.of(spiedClient));
					when(reservationRepository.findByClient(A_CLIENT_UUID))
						.thenReturn(severalReservationList);
					
					transactionalBookingService.removeClientNamed(A_FIRSTNAME, A_LASTNAME);
					
					InOrder inOrder = Mockito.inOrder(
							transactionManager, clientRepository, reservationRepository);
					
					ArgumentCaptor<Reservation> dateCaptor = ArgumentCaptor.forClass(Reservation.class);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					inOrder.verify(reservationRepository).findByClient(A_CLIENT_UUID);
					inOrder.verify(reservationRepository, times(2)).delete(dateCaptor.capture());
					assertThat(dateCaptor.getAllValues()).containsExactly(A_RESERVATION, anotherReservation);
					inOrder.verify(clientRepository).delete(spiedClient);
					
					verifyNoMoreInteractions(transactionManager, clientRepository, reservationRepository);
				}

				@Test
				@DisplayName("Client doesn't exist")
				void testRemoveClientNamedWhenClientDoesNotExistShouldThrow() {
					// default stubbing for clientRepository.findByName(firstName, lastName)
					
					assertThatThrownBy(
							() -> transactionalBookingService.removeClientNamed(A_FIRSTNAME, A_LASTNAME))
						.isInstanceOf(InstanceNotFoundException.class)
						.hasMessage(
							"There is no client named \"" + A_FIRSTNAME + " "
							+ A_LASTNAME + "\" in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(clientRepository).findByName(A_FIRSTNAME, A_LASTNAME);
					
					verifyNoMoreInteractions(transactionManager, clientRepository);
					verifyNoInteractions(reservationRepository);
				}
			}

			@Nested
			@DisplayName("Tests for 'insertNewReservation'")
			class InsertNewReservationTest {

				@DisplayName("Reservation is new and client exists")
				@Test
				void testInsertNewReservationWhenReservationIsNewAndAssociatedClientExistsShouldInsertAndReturn() {
					// default stubbing for reservationRepository.findByDate(date)
					when(clientRepository.findById(A_CLIENT_UUID)).thenReturn(Optional.of(A_CLIENT));
					when(reservationRepository.save(A_RESERVATION)).thenReturn(A_RESERVATION);
					
					assertThat(transactionalBookingService.insertNewReservation(A_RESERVATION))
						.isEqualTo(A_RESERVATION);
					
					InOrder inOrder = Mockito.inOrder(
							transactionManager, reservationRepository, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
					inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
					inOrder.verify(reservationRepository).save(A_RESERVATION);
				
					verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
				}

				@Test
				@DisplayName("Reservation is new and client doesn't exist")
				void testInsertNewReservationWhenReservationIsNewAndAssociatedClientDoesNotExistShouldNotInsertAndThrow() {
					// default stubbing for reservationRepository.findByDate(date)
					// default stubbing for clientRepository.findById(id)
					
					assertThatThrownBy(
							() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
						.isInstanceOf(InstanceNotFoundException.class)
						.hasMessage(
							"The client with id: " + A_CLIENT_UUID
							+ ", associated to Reservation [date=" + A_LOCALDATE
							+ "] is not in the database. Please, insert the client before the reservation.");
					
					InOrder inOrder = Mockito.inOrder(
							transactionManager, reservationRepository, clientRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
					inOrder.verify(clientRepository).findById(A_CLIENT_UUID);
					
					verify(reservationRepository, never()).save(A_RESERVATION);
					verifyNoMoreInteractions(transactionManager, reservationRepository, clientRepository);
				}

				@Test
				@DisplayName("Reservation already exists")
				void testInsertNewReservationWhenReservationAlreadyExistsShouldThrow() {
					when(reservationRepository.findByDate(A_LOCALDATE))
						.thenReturn(Optional.of(A_RESERVATION));
					
					assertThatThrownBy(
							() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
						.isInstanceOf(InstanceAlreadyExistsException.class)
						.hasMessage(
							"Reservation [date=" + A_LOCALDATE.toString()
							+ "] is already in the database.");
					
					InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
					
					inOrder.verify(transactionManager)
						.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
					inOrder.verify(reservationRepository).findByDate(A_LOCALDATE);
				
					verifyNoMoreInteractions(transactionManager, reservationRepository);
					verifyNoInteractions(clientRepository);
				}
			}
		}

		@Nested
		@DisplayName("Transaction is failure")
		class TransactionFailureTest {

			@BeforeEach
			void doStubbing() throws Exception {
				when(transactionManager.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any()))
					.thenThrow(new TransactionException());
			}

			@Test
			@DisplayName("Transaction fails on 'insertNewReservation'")
			void testInsertNewReservationWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.insertNewReservation(A_RESERVATION))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}

			@Test
			@DisplayName("Transaction fails on 'removeClientNamed'")
			void testRemoveClientNamedWhenTransactionFailsShouldThrow() {
				assertThatThrownBy(
						() -> transactionalBookingService.removeClientNamed(A_FIRSTNAME, A_LASTNAME))
					.isInstanceOf(DatabaseException.class)
					.hasMessage("A database error occurs: the request cannot be executed.");
			}
		}
	}
}
