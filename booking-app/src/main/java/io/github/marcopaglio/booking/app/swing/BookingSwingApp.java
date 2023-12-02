package io.github.marcopaglio.booking.app.swing;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.UuidRepresentation.STANDARD;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;
import static org.bson.codecs.pojo.Conventions.USE_GETTERS_FOR_SETTERS;

import java.awt.EventQueue;
import java.awt.Frame;
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
	 * Status exit code indicating startup failure.
	 * Exit codes must be in the range [0;255] to work the same across all operating systems;
	 * otherwise some operating systems, such as Unix, may convert them with the 256 module.
	 */
	private static final int STARTUP_FAILURE_STATUS = 255;

	/**
	 * Argument value for DBMS choice. By default {@code POSTGRES} is used.
	 */
	@Option(names = { "--dbms", "-dbms" }, description = "Name of the DBMS accepted: ${COMPLETION-CANDIDATES}")
	private DBMS dbms = DBMS.POSTGRES;

	/**
	 * Argument value for host name choice. By default {@code localhost} is used.
	 */
	@Option(names = { "--host", "-host", "-h" }, description = "Host name of the database to connect to")
	private String host = "localhost";

	/**
	 * Argument value for port number choice. By default {@code 5432} is used.
	 */
	@Option(names = { "--port", "-port", "-p" }, description = "Port number of the database to connect to")
	private int port = 5432;

	/**
	 * Argument value for database name choice. By default {@code BookingApp_db} is used.
	 */
	@Option(names = { "--name", "-name", "-n" }, description = "Name of the database to connect to")
	private String name = "BookingApp_db";

	/**
	 * Argument value for user name choice for logging into the database. By default {@code postgres-user} is used.
	 * Note: currently ignored by MongoDB.
	 */
	@Option(names = { "--user", "-user", }, description = "Username for logging into the database")
	private String user = "postgres-user";

	/**
	 * Argument value for password choice for logging into the database. By default {@code postgres-pswd} is used.
	 * Note: currently ignored by MongoDB.
	 */
	@Option(names = { "--pswd", "-pswd", }, description = "Password for logging into the database")
	private String pswd = "postgres-pswd";

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
		DatabaseHelper dbHelper = createDatabaseHelper(dbms);
		
		LOGGER.info("BookingApp is starting...");
		EventQueue.invokeLater(() -> {
			try {
				TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
				ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
				ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
				
				LOGGER.info(String.format("BookingApp is connecting with %s...", dbHelper.getDBName()));
				dbHelper.openDatabaseConnection();
				LOGGER.info(String.format("The connection to %s has been established.", dbHelper.getDBName()));
				
				TransactionManager transactionManager = dbHelper.getTransactionDBManager(transactionHandlerFactory,
						clientRepositoryFactory, reservationRepositoryFactory);
				
				BookingService bookingService = new TransactionalBookingService(transactionManager);
				ClientValidator clientValidator = new RestrictedClientValidator();
				ReservationValidator reservationValidator = new RestrictedReservationValidator();
				
				BookingSwingView bookingSwingView = new BookingSwingView();
				BookingPresenter bookingPresenter = new ServedBookingPresenter(bookingSwingView,
						bookingService, clientValidator, reservationValidator);
				bookingSwingView.setBookingPresenter(bookingPresenter);
				bookingSwingView.setVisible(true);
				bookingPresenter.allClients();
				bookingPresenter.allReservations();
				LOGGER.info("BookingApp is ready to be used.");
			} catch(Exception e) {
				LOGGER.error(() -> "BookingApp startup fails due to an unexpected error.");
				LOGGER.debug(() -> String.format("BookingApp startup fails due to %s: %s", e.getClass(), e.getMessage()));
				
				LOGGER.info(() -> "BookingApp is closing windows...");
				closeWindows();
				LOGGER.info(() -> "All windows are now closed.");
				
				System.exit(STARTUP_FAILURE_STATUS);
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info(String.format("BookingApp is closing connection with %s...", dbHelper.getDBName()));
				dbHelper.closeDatabaseConnection();
				LOGGER.info(String.format("BookingApp is no longer connected to %s.", dbHelper.getDBName()));
			}
		});
		
		return null;
	}

	/**
	 * Creates an helper for the chosen database.
	 * 
	 * @param dbms						the chosen database.
	 * @return							a {@code DatabaseHelper} specific for the chosen database.
	 * @throws IllegalArgumentException	if there is no an helper for the specified {@code dbms}.
	 */
	private DatabaseHelper createDatabaseHelper(DBMS dbms) throws IllegalArgumentException {
		if (dbms == DBMS.MONGO)
			return new MongoHelper();
		if (dbms == DBMS.POSTGRES)
			return new PostgresHelper();
		throw new IllegalArgumentException(
				String.format("Cannot create a database helper for the given DBMS=%s", dbms));
	}

	/**
	 * Closes and cleans all displayable frames.
	 */
	private void closeWindows() {
		for (Frame f: Frame.getFrames()) {
			if (f.isDisplayable()) {
				f.setVisible(false);
				f.dispose();
			}
		}
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

	/**
	 * This interface provides methods for operating in the application using the DBMS functionality.
	 */
	interface DatabaseHelper {

		/**
		 * Retrieves the complete name of chosen database.
		 */
		public String getDBName();

		/**
		 * Opens the connection to the chosen database.
		 */
		public void openDatabaseConnection();

		/**
		 * Creates a transaction manager for the chosen database.
		 * 
		 * @param transactionHandlerFactory		the factory of {@code TransactionHandler} needed
		 * 										for the transaction manager.
		 * @param clientRepositoryFactory		the factory of {@code ClientRepository} needed
		 * 										for the transaction manager.
		 * @param reservationRepositoryFactory	the factory of {@code ReservationRepository} needed
		 * 										for the transaction manager.
		 * @return								a {@code TransactionManager} for the chosen database.
		 */
		public TransactionManager getTransactionDBManager(TransactionHandlerFactory transactionHandlerFactory,
				ClientRepositoryFactory clientRepositoryFactory,
				ReservationRepositoryFactory reservationRepositoryFactory);

		/**
		 * Closes the opened connection to the chosen database.
		 */
		public void closeDatabaseConnection();
	}

	/**
	 * Implements methods for operating in the application using the MongoDB functionality.
	 */
	class MongoHelper implements DatabaseHelper {
		/**
		 * Mongo complete name.
		 */
		private static final String MONGO_DB = "MongoDB";

		/**
		 * The client for connecting to the MongoDB database.
		 */
		private MongoClient mongoClient;

		/**
		 * Default constructor.
		 */
		public MongoHelper() {
			super();
			mongoClient = null;
		}

		/**
		 * Retrieves the complete name of MongoDB.
		 */
		public String getDBName() {
			return MONGO_DB;
		}

		/**
		 * Opens the connection to MongoDB through a {@code MongoClient}.
		 */
		@Override
		public void openDatabaseConnection() {
			mongoClient = getClient(String.format("mongodb://%s:%d", host, port));
		}

		/**
		 * Returns a client connected to the MongoDB database.
		 * 
		 * @param connectionString	the MongoDB URL used for the connection.
		 * @return					a {@code MongoClient} connected to MongoDB.
		 */
		private MongoClient getClient(String connectionString) {
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
		 * Creates a transaction manager for MongoDB.
		 * 
		 * @param transactionHandlerFactory		the factory of {@code TransactionHandler} needed
		 * 										for the transaction manager.
		 * @param clientRepositoryFactory		the factory of {@code ClientRepository} needed
		 * 										for the transaction manager.
		 * @param reservationRepositoryFactory	the factory of {@code ReservationRepository} needed
		 * 										for the transaction manager.
		 * @return								a {@code TransactionMongoManager} for MongoDB.
		 */
		@Override
		public TransactionManager getTransactionDBManager(TransactionHandlerFactory transactionHandlerFactory,
				ClientRepositoryFactory clientRepositoryFactory,
				ReservationRepositoryFactory reservationRepositoryFactory) {
			return new TransactionMongoManager(mongoClient, name, transactionHandlerFactory,
					clientRepositoryFactory, reservationRepositoryFactory);
		}

		/**
		 * Closes the opened {@code MongoClient} connection to MongoDB.
		 */
		@Override
		public void closeDatabaseConnection() {
			if (mongoClient != null)
				mongoClient.close();
		}
	}

	/**
	 * Implements methods for operating in the application using the PostgreSQL functionality.
	 */
	class PostgresHelper implements DatabaseHelper {
		/**
		 * Postgres complete name.
		 */
		private static final String POSTGRE_SQL = "PostgreSQL";

		/**
		 * The entity manager factory used to interact with the persistence provider.
		 */
		private EntityManagerFactory emf;

		/**
		 * Default constructor.
		 */
		public PostgresHelper() {
			super();
			emf = null;
		}

		/**
		 * Retrieves the complete name of PostgreSQL.
		 */
		public String getDBName() {
			return POSTGRE_SQL;
		}

		/**
		 * Opens the connection to PostgreSQL through a {@code EntityManagerFactory}.
		 */
		@Override
		public void openDatabaseConnection() {
			emf = Persistence.createEntityManagerFactory("postgres-app", Map.of(
					"jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://%s:%d/%s", host, port, name),
					"jakarta.persistence.jdbc.user", user,
					"jakarta.persistence.jdbc.password", pswd));
		}

		/**
		 * Creates a transaction manager for PostgreSQL.
		 * 
		 * @param transactionHandlerFactory		the factory of {@code TransactionHandler} needed
		 * 										for the transaction manager.
		 * @param clientRepositoryFactory		the factory of {@code ClientRepository} needed
		 * 										for the transaction manager.
		 * @param reservationRepositoryFactory	the factory of {@code ReservationRepository} needed
		 * 										for the transaction manager.
		 * @return								a {@code TransactionPostgresManager} for PostgreSQL.
		 */
		@Override
		public TransactionManager getTransactionDBManager(TransactionHandlerFactory transactionHandlerFactory,
				ClientRepositoryFactory clientRepositoryFactory,
				ReservationRepositoryFactory reservationRepositoryFactory) {
			return new TransactionPostgresManager(emf, transactionHandlerFactory,
					clientRepositoryFactory, reservationRepositoryFactory);
		}

		/**
		 * Closes the opened {@code EntityManagerFactory} connection to PostgreSQL.
		 */
		@Override
		public void closeDatabaseConnection() {
			if (emf != null && emf.isOpen())
				emf.close();
		}
	}
}