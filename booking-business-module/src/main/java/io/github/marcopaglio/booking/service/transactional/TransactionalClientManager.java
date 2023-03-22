package io.github.marcopaglio.booking.service.transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.service.ClientManager;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/*
 * Implements methods for operating on Client repository using transactions.
 */
public class TransactionalClientManager implements ClientManager {
	private TransactionManager transactionManager;

	/*
	 * The constructor.
	 * @param transactionManager	the {@code TransactionManager} used for applying transactions.
	 */
	public TransactionalClientManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * This method is used to retrieve all the clients saved in the database within a transaction.
	 * @return	the list of clients found in the database.
	 */
	@Override
	public List<Client> findAllClients() {
		return transactionManager.doInTransaction(ClientRepository::findAll);
	}

	/*
	 * This method is used to retrieve a client with specified name and surname
	 * from the database within a transaction.
	 * @param firstName					the name of the client to find.
	 * @param lastName					the surname of the client to find.
	 * @return							the {@code Client} named {@code firstName lastName}.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	@Override
	public Client findClientNamed(String firstName, String lastName) {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to find cannot be null.");
		
		Optional<Client> possibleClient = transactionManager.doInTransaction(
				(ClientRepository clientRepository) -> clientRepository.findByName(firstName, lastName));
		if (possibleClient.isPresent())
			return possibleClient.get();
		throw new NoSuchElementException(
			"There is no client named \"" + firstName + " " + lastName + "\" in the database.");
	}

	/*
	 * This method is used to add a new client in the database within a transaction.
	 * This method checks if the client is present in the database before inserting.
	 * @param client							the client to be inserting.
	 * @return									the {@code Client} inserted.
	 * @throws IllegalArgumentException			if {@code client} is null.
	 * @throws InstanceAlreadyExistsException	if {@code client} is already in the database.
	 */
	@Override
	public Client insertNewClient(Client client) {
		if (client == null)
			throw new IllegalArgumentException("Client to insert cannot be null.");
		
		return transactionManager.doInTransaction(
			(ClientRepository clientRepository) -> {
				Optional<Client> possibleClient = clientRepository
						.findByName(client.getFirstName(), client.getLastName());
				if (possibleClient.isEmpty()) {
					return clientRepository.save(client);
				}
				throw new InstanceAlreadyExistsException(
					client.toString() + " is already in the database.");
			}
		);
	}

	/*
	 * This method is used to remove the client with specified name and surname
	 * and all his reservation from the database.
	 * This method checks if the client is present in the database before removing.
	 * @param firstName					the name of the client to remove.
	 * @param lastName					the surname of the client to remove.
	 * @throws IllegalArgumentException	if {@code firstName} or {@code lastName} are null.
	 * @throws NoSuchElementException	if there is no client with those names in database.
	 */
	@Override
	public void removeClientNamed(String firstName, String lastName) {
		if (firstName == null || lastName == null)
			throw new IllegalArgumentException("Names of client to remove cannot be null.");
		
		transactionManager.doInTransaction(
			(ClientRepository clientRepository, ReservationRepository reservationRepository) -> {
				Optional<Client> possibleClient = clientRepository.findByName(firstName, lastName);
				if (possibleClient.isPresent()) {
					List<Reservation> reservationList = reservationRepository
						.findByClient(possibleClient.get().getUuid());
					for (Reservation reservation : reservationList)
						reservationRepository.delete(reservation.getDate());
					clientRepository.delete(firstName, lastName);
					return null;
				}
				throw new NoSuchElementException(
					"There is no client named \"" + firstName + " " + lastName + "\" in the database.");
			}
		);
	}
}
