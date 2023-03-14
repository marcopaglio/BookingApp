package io.github.marcopaglio.booking.service.transactional;

import java.util.List;
import java.util.Optional;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.service.ClientManager;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/*
 * Implements methods for operating on Client entities using transactions.
 */
public class TransactionalClientManager implements ClientManager {
	private TransactionManager transactionManager;

	public TransactionalClientManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public List<Client> findAllClients() {
		return transactionManager.doInTransaction(ClientRepository::findAll);
	}

	@Override
	public Optional<Client> findClientNamed(String firstName, String lastName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertNewClient(Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeClientNamed(String firstName, String lastName) {
		// TODO Auto-generated method stub
		
	}

}
