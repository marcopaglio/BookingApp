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
	 */
	private static final int STARTUP_FAILURE_STATUS = -1;

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
		LOGGER.info("BookingApp is starting...");
		EventQueue.invokeLater(() -> {
			try {
				TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
				ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
				ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
				
				LOGGER.info(String.format("BookingApp is connecting with %s...", dbms));
				TransactionManager transactionManager = createTransactionManager(transactionHandlerFactory,
						clientRepositoryFactory, reservationRepositoryFactory);
				LOGGER.info(String.format("The connection to %s has been established.", dbms));
				
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
				LOGGER.info(String.format("BookingApp is closing connection with %s...", dbms));
				if (dbms == DBMS.MONGO && mongoClient != null)
					mongoClient.close();
				if (dbms == DBMS.POSTGRES && emf != null && emf.isOpen())
					emf.close();
				LOGGER.info(String.format("BookingApp is no longer connected to %s.", dbms));
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
}