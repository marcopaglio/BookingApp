package io.github.marcopaglio.booking.transaction.manager.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;

import io.github.marcopaglio.booking.exception.TransactionException;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.repository.mongo.ClientMongoRepository;
import io.github.marcopaglio.booking.repository.mongo.ReservationMongoRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ClientTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.handler.mongo.TransactionMongoHandler;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

/**
 * An implementation of {@code TransactionManager} for managing code executed
 * on MongoDB within transactions.
 */
public class TransactionMongoManager extends TransactionManager {
	/**
	 * Specifies that the reason the transaction fails is a commitment failure.
	 */
	private static final String COMMIT_FAILURE = "a commitment failure";

	/**
	 * Options used to configure transactions.
	 * Note: casually consistency is applied when both read and write concerns
	 * has value 'majority'.
	 */
	public static final TransactionOptions TXN_OPTIONS = TransactionOptions.builder()
			.readPreference(ReadPreference.primary())
			.readConcern(ReadConcern.MAJORITY)
			.writeConcern(WriteConcern.MAJORITY)
			.build();

	/**
	 * Used for executing code on {@code ClientRepository} and/or {@code ReservationRepository}
	 * into transactions.
	 * Particularly, it allows to create sessions, transactions and repositories.
	 */
	private MongoClient mongoClient;

	/**
	 * Name of the mongoDB database in which the repository works.
	 */
	private String databaseName;

	/**
	 * Constructs a manager for applying code that uses entity repositories 
	 * using MongoDB transactions.
	 * 
	 * @param mongoClient					the client connected to the MongoDB database.
	 * @param databaseName					the name of the MongoDB database.
	 * @param transactionHandlerFactory		the factory to create {@code ClientSession} instances.
	 * @param clientRepositoryFactory		the factory to create
	 * 										{@code ClientMongoRepository} instances.
	 * @param reservationRepositoryFactory	the factory to create
	 * 										{@code ReservationMongoRepository} instances.
	 */
	public TransactionMongoManager(MongoClient mongoClient, String databaseName,
			TransactionHandlerFactory transactionHandlerFactory,
			ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		super(transactionHandlerFactory, clientRepositoryFactory, reservationRepositoryFactory);
		this.databaseName = databaseName;
		this.mongoClient = mongoClient;
	}

	/**
	 * Prepares to execution of code that involves the {@code ClientRepository}'s method(s)
	 * on MongoDB in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if the execution or the commitment of the transaction fails.
	 */
	@Override
	public <R> R doInTransaction(ClientTransactionCode<R> code) throws TransactionException {
		TransactionMongoHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(mongoClient, TXN_OPTIONS);
		ClientMongoRepository clientRepository = clientRepositoryFactory
				.createClientRepository(mongoClient, sessionHandler.getHandler(), databaseName);
		try {
			return executeInTransaction(code, sessionHandler, clientRepository);
		} catch(MongoCommandException e) {
			LOGGER.warn(e.getMessage());
			throw new TransactionException(
					transactionFailureMsg(COMMIT_FAILURE), e.getCause());
		}
	}

	/**
	 * Prepares to execution of code that involves the {@code ReservationRepository}'s method(s)
	 * on MongoDB in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if the execution or the commitment of the transaction fails.
	 */
	@Override
	public <R> R doInTransaction(ReservationTransactionCode<R> code) throws TransactionException {
		TransactionMongoHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(mongoClient, TXN_OPTIONS);
		ReservationMongoRepository reservationRepository = reservationRepositoryFactory
				.createReservationRepository(mongoClient, sessionHandler.getHandler(), databaseName);
		try {
			return executeInTransaction(code, sessionHandler, reservationRepository);
		} catch(MongoCommandException e) {
			LOGGER.warn(e.getMessage());
			throw new TransactionException(
					transactionFailureMsg(COMMIT_FAILURE), e.getCause());
		}
	}

	/**
	 * Prepares to execution of code that involves both {@code ClientRepository}'s and
	 * {@code ReservationRepository}'s methods on MongoDB in a single transaction.
	 * 
	 * @param <R>					the returned type of executed code.
	 * @param code					the code to execute.
	 * @return						something depending on execution code.
	 * @throws TransactionException	if the execution or the commitment of the transaction fails.
	 */
	@Override
	public <R> R doInTransaction(ClientReservationTransactionCode<R> code) throws TransactionException {
		TransactionMongoHandler sessionHandler =
				transactionHandlerFactory.createTransactionHandler(mongoClient, TXN_OPTIONS);
		ClientMongoRepository clientRepository = clientRepositoryFactory
				.createClientRepository(mongoClient, sessionHandler.getHandler(), databaseName);
		ReservationMongoRepository reservationRepository = reservationRepositoryFactory
				.createReservationRepository(mongoClient, sessionHandler.getHandler(), databaseName);
		try {
			return executeInTransaction(code, sessionHandler, clientRepository, reservationRepository);
		} catch(MongoCommandException e) {
			LOGGER.warn(e.getMessage());
			throw new TransactionException(
					transactionFailureMsg(COMMIT_FAILURE), e.getCause());
		}
	}
}