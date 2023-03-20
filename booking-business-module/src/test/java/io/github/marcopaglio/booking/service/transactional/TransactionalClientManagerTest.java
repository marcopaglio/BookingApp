package io.github.marcopaglio.booking.service.transactional;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;

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
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

@DisplayName("Tests for TransactionalClientManager class")
class TransactionalClientManagerTest {
	private static final String A_FIRST_NAME = "Mario";
	private static final String A_LAST_NAME = "Rossi";
	private static final Client A_CLIENT = new Client(A_FIRST_NAME, A_LAST_NAME, new ArrayList<>());

	private AutoCloseable closeable;
	
	@InjectMocks
	private TransactionalClientManager transactionalClientManager;
	
	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private ClientRepository clientRepository;
	
	@Mock
	private ReservationRepository reservationRepository;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		transactionalClientManager = new TransactionalClientManager(transactionManager);
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
		void testFindAllClientsWhenThereAreNotClientsToRetrieve() {
			// setup
			List<Client> clients = new ArrayList<>();
			when(clientRepository.findAll()).thenReturn(clients); // as default
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			// verify state
			assertThat(transactionalClientManager.findAllClients()).isEqualTo(clients);
			
			// verify interactions
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("Single client to retrieve")
		void testFindAllClientsWhenThereIsASingleClientToRetrieve() {
			// setup
			List<Client> clients = Arrays.asList(A_CLIENT);
			when(clientRepository.findAll()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			// verify state
			assertThat(transactionalClientManager.findAllClients()).isEqualTo(clients);
			
			// verify interactions
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("Several clients to retrieve")
		void testFindAllClientsWhenThereAreSeveralClientsToRetrieve() {
			// setup
			Client another_client = new Client("Maria", "De Lucia", new ArrayList<>());
			List<Client> clients = Arrays.asList(A_CLIENT, another_client);
			when(clientRepository.findAll()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			// verify state
			assertThat(transactionalClientManager.findAllClients()).isEqualTo(clients);
			
			// verify interactions
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'findClientNamed'")
	class FindClientNamedTest {
		
		@Test
		@DisplayName("Names input are null")
		void testFindClientNamedWhenNamesAreNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalClientManager.findClientNamed(null, A_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalClientManager.findClientNamed(A_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalClientManager.findClientNamed(null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to find cannot be null.");
		}
		
		@Test
		@DisplayName("The client exists")
		void testFindClientNamedWhenClientExists() {
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThat(transactionalClientManager.findClientNamed(A_FIRST_NAME, A_LAST_NAME))
				.isEqualTo(A_CLIENT);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("The client doesn't exist")
		void testFindClientNamedWhenClientDoesNotExist() {
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalClientManager.findClientNamed(A_FIRST_NAME, A_LAST_NAME))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage(
					"There is no client named \"" + A_FIRST_NAME + " " + A_LAST_NAME + "\" in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'insertNewClient'")
	class InsertNewClientTest {
		
		@Test
		@DisplayName("Client input is null")
		void testInsertNewClientWhenClientIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalClientManager.insertNewClient(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to insert cannot be null.");
		}
		
		@Test
		@DisplayName("The client is actually new")
		void testInsertNewClientWhenClientIsNotYetInDatabase() {
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
			when(clientRepository.save(A_CLIENT)).thenReturn(A_CLIENT); // TODO: serve?
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThat(transactionalClientManager.insertNewClient(A_CLIENT)).isEqualTo(A_CLIENT);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(clientRepository).save(A_CLIENT);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("The client already exists")
		void testInsertNewClientWhenClientIsAlreadyInDatabase() {
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(A_CLIENT));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalClientManager.insertNewClient(A_CLIENT))
				.isInstanceOf(InstanceAlreadyExistsException.class)
				.hasMessage(
					"Client [" + A_FIRST_NAME + " " + A_LAST_NAME + "] is already in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'removeClientNamed'")
	class RemoveClientNamedTest {
		private final LocalDate a_localdate = LocalDate.parse("2023-04-24");
		private final LocalDate another_localdate = LocalDate.parse("2023-03-16");
		private final Reservation a_reservation = new Reservation(UUID.randomUUID(), a_localdate);
		private final Reservation another_reservation = new Reservation(UUID.randomUUID(), another_localdate);
		private final List<Reservation> single_reservation_list =
				new ArrayList<>(Arrays.asList(a_reservation));
		private final List<Reservation> several_reservation_list =
				new ArrayList<>(Arrays.asList(a_reservation, another_reservation));
		
		@BeforeEach
		void doStubbing() throws Exception {
			when(transactionManager.doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any()))
			.thenAnswer(
				answer((ClientReservationTransactionCode<?> code) -> 
					code.apply(clientRepository, reservationRepository)));
		}
		
		@Test
		@DisplayName("Names input are null")
		void testRemoveClientNamedWhenNamesAreNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalClientManager.removeClientNamed(null, A_LAST_NAME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
			
			assertThatThrownBy(
					() -> transactionalClientManager.removeClientNamed(null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Names of client to remove cannot be null.");
		}
		
		@Test
		@DisplayName("The client is in database without reservations")
		void testRemoveClientNamedWhenClientIsInDatabaseWithoutReservations() {
			UUID clientUUID = A_CLIENT.getUuid();
			
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(A_CLIENT));
			// reservationRepository.findByClient(isA(UUID.class)) returns empty collection as default
			// TODO esplicita i default
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(reservationRepository).findByClient(clientUUID);
			inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@Test
		@DisplayName("The client is in database with a single reservation in database")
		void testRemoveClientNamedWhenClientIsInDatabaseWithSingleReservationInDatabase() {
			Client client = new Client(A_FIRST_NAME, A_LAST_NAME, single_reservation_list);
			UUID clientUUID = client.getUuid();
			
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(client));
			when(reservationRepository.findByClient(clientUUID)).thenReturn(single_reservation_list);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(reservationRepository).findByClient(clientUUID);
			inOrder.verify(reservationRepository).delete(a_localdate);
			inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@Test
		@DisplayName("The client is in database with several reservations in database")
		void testRemoveClientNamedWhenClientIsInDatabaseWithSeveralReservationsInDatabase() {
			Client client = new Client(A_FIRST_NAME, A_LAST_NAME, several_reservation_list);
			UUID clientUUID = client.getUuid();
			
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(client));
			when(reservationRepository.findByClient(clientUUID)).thenReturn(several_reservation_list);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(reservationRepository).findByClient(clientUUID);
			inOrder.verify(reservationRepository).delete(a_localdate);
			inOrder.verify(reservationRepository).delete(another_localdate);
			inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@Test
		@DisplayName("The client is in database with a single reservation already deleted from database")
		void testRemoveClientNamedWhenClientIsInDatabaseWithSingleReservationAlreadyDeletedFromDatabase() {
			Client client = new Client(A_FIRST_NAME, A_LAST_NAME, single_reservation_list);
			UUID clientUUID = client.getUuid();
			
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(client));
			// reservationRepository.findByClient(isA(UUID.class)) returns empty collection as default
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(reservationRepository).findByClient(clientUUID);
			inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@Test
		@DisplayName("The client is in database with several reservations some of which already deleted from database")
		void testRemoveClientNamedWhenClientIsInDatabaseWithSeveralReservationsSomeOfWhichAlreadyDeletedFromDatabase() {
			Client client = new Client(A_FIRST_NAME, A_LAST_NAME, several_reservation_list);
			UUID clientUUID = client.getUuid();
			
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.of(client));
			when(reservationRepository.findByClient(clientUUID)).thenReturn(single_reservation_list);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			inOrder.verify(reservationRepository).findByClient(clientUUID);
			inOrder.verify(reservationRepository).delete(a_localdate);
			inOrder.verify(clientRepository).delete(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@Test
		@DisplayName("The Client is already been deleted")
		void testRemoveClientNamedWhenClientIsNotInDatabase() {
			when(clientRepository.findByName(A_FIRST_NAME, A_LAST_NAME)).thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalClientManager.removeClientNamed(A_FIRST_NAME, A_LAST_NAME))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage(
					"There is no client named \"" + A_FIRST_NAME + " " + A_LAST_NAME + "\" in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName(A_FIRST_NAME, A_LAST_NAME);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	// TODO: se service è unico, dunque i test vanno tutti insieme, allora raggrupparli per 
	// test che coinvolgono solo clientRepo, test solo reservationRepo e test di entrambi
	@AfterEach
	void releaseMocks() throws Exception {
		closeable.close();
	}
}
