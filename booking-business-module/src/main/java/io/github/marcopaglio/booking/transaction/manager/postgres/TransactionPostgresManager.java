package io.github.marcopaglio.booking.transaction.manager.postgres;

import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.repository.postgres.ClientPostgresRepository;
import io.github.marcopaglio.booking.repository.postgres.ReservationPostgresRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.handler.postgres.TransactionPostgresHandler;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * An implementation of {@code TransactionManager} for managing code executed
 * on PostgreSQL within transactions.
 */
public class TransactionPostgresManager extends TransactionManager {

	/**
	 * Used for executing code on {@code ClientRepository} and/or {@code ReservationRepository}
	 * into transactions.
	 * Particularly, it allows to create entity manager, transactions and repositories.
	 */
	private EntityManagerFactory emf;

	/**
	 * Used for creation of {@code EntityManager} instances.
	 */
	private TransactionHandlerFactory transactionHandlerFactory;

	/**
	 * Used for creation of {@code ClientPostgresRepository} instances.
	 */
	private ClientRepositoryFactory clientRepositoryFactory;

	/**
	 * Used for creation of {@code ReservationPostgresRepository} instances.
	 */
	private ReservationRepositoryFactory reservationRepositoryFactory;

	/**
	 * Constructs a manager for applying code that uses entity repositories 
	 * using PostgreSQL transactions.
	 * 
	 * @param emf							the entity manager factory used to interact
	 * 										with the persistence provider.
	 * @param transactionHandlerFactory		the factory for create instances
	 * 										of {@code EntityManager}.
	 * @param clientRepositoryFactory		the factory to create instances
	 * 										of {@code ClientPostgresRepository}.
	 * @param reservationRepositoryFactory	the factory to create instances
	 * 										of {@code ReservationPostgresRepository}.
	 */
	public TransactionPostgresManager(EntityManagerFactory emf,
			TransactionHandlerFactory transactionHandlerFactory,
			ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		super();
		this.emf = emf;
		this.transactionHandlerFactory = transactionHandlerFactory;
		this.clientRepositoryFactory = clientRepositoryFactory;
		this.reservationRepositoryFactory = reservationRepositoryFactory;
	}

	/**
	 * Prepares to execution of code that involves the {@code ClientRepository}'s method(s)
	 * on PostgreSQL in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		ClientPostgresRepository clientRepository = clientRepositoryFactory
				.createClientRepository(sessionHandler.getHandler());
		return executeInTransaction(code, sessionHandler, clientRepository);
	}

	/**
	 * Prepares to execution of code that involves the {@code ReservationRepository}'s method(s)
	 * on PostgreSQL in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		ReservationPostgresRepository reservationRepository = reservationRepositoryFactory
				.createReservationRepository(sessionHandler.getHandler());
		return executeInTransaction(code, sessionHandler, reservationRepository);
	}

	/**
	 * Prepares to execution of code that involves both {@code ClientRepository}'s and
	 * {@code ReservationRepository}'s methods on PostgreSQL in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if {@code code} throws {@code IllegalArgumentException},
	 * 								{@code UpdateFailureException},
	 * 								{@code NotNullConstraintViolationException} or
	 * 								{@code UniquenessConstraintViolationException}.
	 * @throws RuntimeException		if an unexpected {@code RuntimeException} occurs.
	 */
	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException {
		TransactionPostgresHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(emf);
		ClientPostgresRepository clientRepository = clientRepositoryFactory
				.createClientRepository(sessionHandler.getHandler());
		ReservationPostgresRepository reservationRepository = reservationRepositoryFactory
				.createReservationRepository(sessionHandler.getHandler());
		return executeInTransaction(code, sessionHandler, clientRepository, reservationRepository);
	}
}
