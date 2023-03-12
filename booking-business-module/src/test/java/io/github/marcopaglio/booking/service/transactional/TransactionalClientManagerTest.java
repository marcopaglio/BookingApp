package io.github.marcopaglio.booking.service.transactional;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

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
			inOrder.verify(transactionManager).doInTransaction(ArgumentMatchers.<ClientTransactionCode<?>>any());
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
	
	@AfterEach
	void releaseMocks() throws Exception {
		closeable.close();
	}
}
