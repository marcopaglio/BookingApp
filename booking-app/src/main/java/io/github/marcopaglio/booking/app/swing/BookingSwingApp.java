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

@Command(mixinStandardHelpOptions = true)
public class BookingSwingApp implements Callable<Void> {

	@Option(names = { "--database", "-database", "-db" },
			description = "Name of the DBMS accepted: ${COMPLETION-CANDIDATES}")
	private Database database = Database.MONGO;

	@Option(names = { "--host", "-host", "-h" }, description = "Host name of the database to connect to")
	private String host = "localhost";

	@Option(names = { "--port", "-port", "-p" }, description = "Port number of the database to connect to")
	private int port = 27017;

	private EntityManagerFactory emf;
	private MongoClient mongoClient;

	public static void main(String[] args) {
		new CommandLine(new BookingSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				TransactionHandlerFactory transactionHandlerFactory = new TransactionHandlerFactory();
				ClientRepositoryFactory clientRepositoryFactory = new ClientRepositoryFactory();
				ReservationRepositoryFactory reservationRepositoryFactory = new ReservationRepositoryFactory();
				
				TransactionManager transactionManager = createTransactionManager(transactionHandlerFactory,
						clientRepositoryFactory, reservationRepositoryFactory);
				
				BookingService bookingService = new TransactionalBookingService(transactionManager);
				ClientValidator clientValidator = new RestrictedClientValidator();
				ReservationValidator reservationValidator = new RestrictedReservationValidator();
				
				BookingSwingView bookingView = new BookingSwingView(); //TODO: use BookingView
				BookingPresenter bookingPresenter = new ServedBookingPresenter(bookingView,
						bookingService, clientValidator, reservationValidator);
				bookingView.setBookingPresenter(bookingPresenter);
				bookingView.setVisible(true);
				bookingPresenter.allClients();
				bookingPresenter.allReservations();
			} catch(Exception e) {
				//
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (database == Database.MONGO) {
					if (mongoClient != null)
						mongoClient.close();
				}
				if (database == Database.POSTGRES) {
					if (emf != null && emf.isOpen())
						emf.close();
				}
				
			}
		});
		
		return null;
	}

	private TransactionManager createTransactionManager(TransactionHandlerFactory transactionHandlerFactory,
			ClientRepositoryFactory clientRepositoryFactory,
			ReservationRepositoryFactory reservationRepositoryFactory) {
		TransactionManager transactionManager = null;
		if (database == Database.MONGO) {
			mongoClient = getClient(String.format("mongodb://%s:%d", host, port));
			transactionManager = new TransactionMongoManager(mongoClient, transactionHandlerFactory,
					clientRepositoryFactory, reservationRepositoryFactory);
		}
		if (database == Database.POSTGRES) {
			String jdbcUrl = String.format("jdbc:postgresql://%s:%d/BookingApp_db", host, port);
			emf = Persistence.createEntityManagerFactory("postgres-app",
					Map.of("jakarta.persistence.jdbc.url", jdbcUrl));
			transactionManager = new TransactionPostgresManager(emf, transactionHandlerFactory,
					clientRepositoryFactory, reservationRepositoryFactory);
		}
		return transactionManager;
	}

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
	enum Database {
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