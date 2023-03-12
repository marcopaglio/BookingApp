package io.github.marcopaglio.booking.service.transactional;

import java.util.List;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.service.ClientManager;

/*
 * Implements methods for operating on Client entities using transactions.
 */
public class TransactionalClientManager implements ClientManager {

	@Override
	public List<Client> getAllClients() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Client getClientNamed(String firstName, String lastName) {
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
