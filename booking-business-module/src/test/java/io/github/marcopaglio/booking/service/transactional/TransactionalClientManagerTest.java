package io.github.marcopaglio.booking.service.transactional;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

@DisplayName("Tests for TransactionalClientManager class")
class TransactionalClientManagerTest {
	private AutoCloseable closeable;
	
	@InjectMocks
	private TransactionalClientManager transactionalClientManager;
	
	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private ClientRepository clientRepository;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		transactionalClientManager = new TransactionalClientManager(transactionManager);
	}
	
	@Nested
	@DisplayName("Tests for 'findAllClients'")
	class FindAllClientsTest {
		
		@BeforeEach
		void doStubbing() throws Exception {
			// make sure the lambda passed to the TransactionManager
			// is executed, using the mock repository
			when(transactionManager.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any()))
				.thenAnswer(
					answer((ClientTransactionCode<?> code) -> code.apply(clientRepository)));
		}
		
		@Test
		@DisplayName("No Clients to retrieve")
		void testFindAllClientsWhenThereAreNotClientsToRetrieve() {
			// setup
			List<Client> clients = new ArrayList<>();
			
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
		@DisplayName("Single Client to retrieve")
		void testFindAllClientsWhenThereIsASingleClientToRetrieve() {
			// setup
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			List<Client> clients = Arrays.asList(client);
			
			when(clientRepository.findAll()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			// verify state
			assertThat(transactionalClientManager.findAllClients()).isEqualTo(clients);
			
			// verify interactions
			inOrder.verify(transactionManager).doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findAll();
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("Multiple Clients to retrieve")
		void testFindAllClientsWhenThereAreMultipleClientsToRetrieve() {
			// setup
			Client client1 = new Client("Mario", "Rossi", new ArrayList<>());
			Client client2 = new Client("Maria", "De Lucia", new ArrayList<>());
			List<Client> clients = Arrays.asList(client1, client2);
			
			when(clientRepository.findAll()).thenReturn(clients);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			// verify state
			assertThat(transactionalClientManager.findAllClients()).isEqualTo(clients);
			
			// verify interactions
			inOrder.verify(transactionManager).doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findAll();
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'findClientNamed'")
	class FindClientNamedTest {
		
		@BeforeEach
		void doStubbing() throws Exception {
			// make sure the lambda passed to the TransactionManager
			// is executed, using the mock repository
			when(transactionManager.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any()))
				.thenAnswer(
					answer((ClientTransactionCode<?> code) -> code.apply(clientRepository)));
		}
		
		@Test
		@DisplayName("The Client doesn't exist")
		void testFindClientNamedWhenTheClientDoesNotExist() {
			when(clientRepository.findByName("Luigi", "Bianchi"))
				.thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalClientManager.findClientNamed("Luigi", "Bianchi"))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Client named \"Luigi Bianchi\" is not present in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName("Luigi", "Bianchi");
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("The Client exists")
		void testFindClientNamedWhenTheClientExists() {
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			
			when(clientRepository.findByName("Mario", "Rossi"))
				.thenReturn(Optional.of(client));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThat(transactionalClientManager.findClientNamed("Mario", "Rossi")).isEqualTo(client);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName("Mario", "Rossi");
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'insertNewClient'")
	class InsertNewClientTest {
		
		@BeforeEach
		void doStubbing() throws Exception {
			// make sure the lambda passed to the TransactionManager
			// is executed, using the mock repository
			when(transactionManager.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any()))
				.thenAnswer(
					answer((ClientTransactionCode<?> code) -> code.apply(clientRepository)));
		}
		
		@Test
		@DisplayName("Client input is null")
		void testInsertNewClientWhenClientIsNull() {
			assertThatThrownBy(
					() -> transactionalClientManager.insertNewClient(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Client to insert cannot be null.");
		}
		
		@Test
		@DisplayName("The Client is actually new")
		void testInsertNewClientWhenTheClientIsNotInDatabase() {
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			
			when(clientRepository.findByName("Mario", "Rossi")).thenReturn(Optional.empty());
			when(clientRepository.save(client)).thenReturn(client);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalClientManager.insertNewClient(client));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName("Mario", "Rossi");
			inOrder.verify(clientRepository).save(client);
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@Test
		@DisplayName("The Client already exists")
		void testInsertNewClientWhenTheClientIsAlreadyInDatabase() {
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			
			when(clientRepository.findByName("Mario", "Rossi")).thenReturn(Optional.of(client));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalClientManager.insertNewClient(client))
				.isInstanceOf(InstanceAlreadyExistsException.class)
				.hasMessage("Client [Mario Rossi] already exists in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
			inOrder.verify(clientRepository).findByName("Mario", "Rossi");
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'removeClientNamed'")
	class RemoveClientNamedTest {

	}
	
	@AfterEach
	void releaseMocks() throws Exception {
		closeable.close();
	}
}
