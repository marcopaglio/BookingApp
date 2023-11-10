package io.github.marcopaglio.booking.app.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.github.marcopaglio.booking.presenter.BookingPresenter;
import io.github.marcopaglio.booking.presenter.served.ServedBookingPresenter;
import io.github.marcopaglio.booking.repository.factory.ClientRepositoryFactory;
import io.github.marcopaglio.booking.repository.factory.ReservationRepositoryFactory;
import io.github.marcopaglio.booking.service.BookingService;
import io.github.marcopaglio.booking.service.transactional.TransactionalBookingService;
import io.github.marcopaglio.booking.transaction.handler.factory.TransactionHandlerFactory;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;
import io.github.marcopaglio.booking.transaction.manager.mongo.TransactionMongoManager;
import io.github.marcopaglio.booking.transaction.manager.postgres.TransactionPostgresManager;
import io.github.marcopaglio.booking.validator.ClientValidator;
import io.github.marcopaglio.booking.validator.ReservationValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedClientValidator;
import io.github.marcopaglio.booking.validator.restricted.RestrictedReservationValidator;
import io.github.marcopaglio.booking.view.swing.BookingSwingView;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Implementation of BookingApp using Java Swing as GUI and MongoDB or PostgreSQL as DBMS.
 */
@Command(mixinStandardHelpOptions = true)
public class BookingSwingApp implements Callable<Void> {
	/**
	 * Creates meaningful logs on behalf of the class.
	 */
	private static final Logger LOGGER = LogManager.getLogger(BookingSwingApp.class);

	/**
	 * Argument value for DBMS choice. By default 'MongoDB' is used.
	 */
	@Option(names = { "--dbms", "-dbms" }, description = "Name of the DBMS accepted: ${COMPLETION-CANDIDATES}")
	private DBMS dbms = DBMS.MONGO;

	/**
	 * Argument value for host name choice. By default 'localhost' is used.
	 */
	@Option(names = { "--host", "-host", "-h" }, description = "Host name of the database to connect to")
	private String host = "localhost";

	/**
	 * Argument value for port number choice. By default '27017' is used.
	 */
	@Option(names = { "--port", "-port", "-p" }, description = "Port number of the database to connect to")
	private int port = 27017;

	/**
	 * Argument value for database name choice. By default 'BookingApp_db' is used.
	 */
	@Option(names = { "--name", "-name", "-n" }, description = "Name of the database to connect to")
	private String name = "BookingApp_db";

	/**
	 * Argument value for user name choice for logging into the database. By default 'postgres-user' is used.
	 * Note: currently ignored by MongoDB.
	 */
	@Option(names = { "--user", "-user", }, description = "Username for logging into the database")
	private String user = "postgres-user";

	/**
	 * Argument value for password choice for logging into the database. By default 'postgres-pswd' is used.
	 * Note: currently ignored by MongoDB.
	 */
	@Option(names = { "--pswd", "-pswd", }, description = "Password for logging into the database")
	private String pswd = "postgres-pswd";

	/**
	 * The entity manager factory used to interact with the persistence provider.
	 */
	private EntityManagerFactory emf;

	/**
	 * The client for connecting to the MongoDB database.
	 */
	private MongoClient mongoClient;

	/**
	 * Main method using Picocli framework for managing arguments.
	 * 
	 * @param args	arguments needed for starting the application.
	 */
	public static void main(String[] args) {
		new CommandLine(new BookingSwingApp()).execute(args);
	}

	/**
	 * Main content of the application.
	 */
	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
				ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
				ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
				
				LOGGER.info(() -> String.format("The application starts the connection with %s.", dbms));
				TransactionManager transactionManager = createTransactionManager(transactionHandlerFactory,
						clientRepositoryFactory, reservationRepositoryFactory);
				LOGGER.info(() -> String.format("The application is connected to %s.", dbms));
				
				BookingService bookingService = new TransactionalBookingService(transactionManager);
				ClientValidator clientValidator = new RestrictedClientValidator();
				ReservationValidator reservationValidator = new RestrictedReservationValidator();
				
				BookingSwingView bookingView = new BookingSwingView();
				BookingPresenter bookingPresenter = new ServedBookingPresenter(bookingView,
						bookingService, clientValidator, reservationValidator);
				bookingView.setBookingPresenter(bookingPresenter);
				bookingView.setVisible(true);
				bookingPresenter.allClients();
				bookingPresenter.allReservations();
				LOGGER.info(() -> "The application is ready to be used.");
			} catch(Exception e) {
				LOGGER.error(() -> "The application encountered an unexpected error.");
				LOGGER.debug(() -> String.format("The application encounteredthe following error: %s \n"
						+ "with the following message: %s", e.getClass(), e.getMessage()));
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info(() -> String.format("The application is closing the connection with %s.", dbms));
				if (dbms == DBMS.MONGO && mongoClient != null)
					mongoClient.close();
				if (dbms == DBMS.POSTGRES && emf != null && emf.isOpen())
					emf.close();
				LOGGER.info(() -> String.format("The application closed the connection with %s.", dbms));
			}
		});
		
		return null;
	}

	/**
	 * Starts connection to the chosen database and returns its transaction manager.
	 * 
	 * @param transactionHandlerFactory		the factory of {@code TransactionHandler} needed
	 * 										for the transaction manager.
	 * @param clientRepositoryFactory		the factory of {@code ClientRepository} needed
	 * 										for the transaction manager.
	 * @param reservationRepositoryFactory	the factory of {@code ReservationRepository} needed
	 * 										for the transaction manager.
	 * @return								a {@code TransactionManager} for the chosen database.
	 */
	private TransactionManager createTransactionManager(TransactionHandlerFactory transactionHandlerFactory,
			ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		TransactionManager transactionManager = null;
		if (dbms == DBMS.MONGO) {
			mongoClient = getClient(String.format("mongodb://%s:%d", host, port));
			transactionManager = new TransactionMongoManager(mongoClient, name,
					transactionHandlerFactory, clientRepositoryFactory, reservationRepositoryFactory);
		}
		if (dbms == DBMS.POSTGRES) {
			emf = Persistence.createEntityManagerFactory("postgres-app", Map.of(
					"jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://%s:%d/%s", host, port, name),
					"jakarta.persistence.jdbc.user", user,
					"jakarta.persistence.jdbc.password", pswd));
			transactionManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
					clientRepositoryFactory, reservationRepositoryFactory);
		}
		return transactionManager;
	}

	/**
	 * Returns a client connected to the MongoDB database.
	 * 
	 * @param connectionString	the MongoDB URL used for the connection.
	 * @return					a {@code MongoClient} connected to MongoDB.
	 */
	private static MongoClient getClient(String connectionString) {
		// define the CodecProvider for POJO classes
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.conventions(Arrays.asList(ANNOTATION_CONVENTION, USE_GETTERS_FOR_SETTERS))
				.automatic(true)
				.build();
		
		// define the CodecRegistry as codecs and other related information
		CodecRegistry pojoCodecRegistry =
				fromRegistries(getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider));
		
		// configure the MongoClient for using the CodecRegistry
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(connectionString))
				.uuidRepresentation(STANDARD)
				.codecRegistry(pojoCodecRegistry)
				.build();
		return MongoClients.create(settings);
	}

	/**
	 * Enumerated values accepted for {@code database} argument.
	 */
	enum DBMS {
		/**
		 * Defines the use of the MongoDB DBMS.
		 */
		MONGO,
	
		/**
		 * Defines the use of the PostgreSQL DBMS.
		 */
		POSTGRES
	}
}