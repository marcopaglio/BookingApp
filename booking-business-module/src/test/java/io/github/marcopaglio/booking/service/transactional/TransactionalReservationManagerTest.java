package io.github.marcopaglio.booking.service.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.github.marcopaglio.booking.exception.InstanceAlreadyExistsException;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;
import io.github.marcopaglio.booking.transaction.code.ClientReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.code.ReservationTransactionCode;
import io.github.marcopaglio.booking.transaction.manager.TransactionManager;

@DisplayName("Tests for TransactionalReservationManager class")
class TransactionalReservationManagerTest {
	private AutoCloseable closeable;
	
	private TransactionalReservationManager transactionalReservationManager;
	
	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private ReservationRepository reservationRepository;
	
	@Mock
	private ClientRepository clientRepository;

	@BeforeEach
	void setUp() throws Exception {
		closeable = MockitoAnnotations.openMocks(this);
		
		transactionalReservationManager = new TransactionalReservationManager(transactionManager);
		// make sure the lambda passed to the TransactionManager
		// is executed, using the mock repository
		when(transactionManager.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any()))
			.thenAnswer(
				answer((ReservationTransactionCode<?> code) -> code.apply(reservationRepository)));
	}

	@Nested
	@DisplayName("Tests for 'findAllReservations'")
	class FindAllReservationsTest {
		
		@DisplayName("No reservations to retrieve")
		@Test
		void testFindAllReservationsWhenThereAreNotReservationsToRetrieve() {
			List<Reservation> reservations = new ArrayList<>();
			when(reservationRepository.findAll()).thenReturn(reservations); // as default
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThat(transactionalReservationManager.findAllReservations()).isEqualTo(reservations);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@DisplayName("Single reservation to retrieve")
		@Test
		void testFindAllReservationsWhenThereIsASingleReservationToRetrieve() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(UUID.randomUUID(), a_localdate);
			List<Reservation> reservations = Arrays.asList(reservation);
			when(reservationRepository.findAll()).thenReturn(reservations);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThat(transactionalReservationManager.findAllReservations()).isEqualTo(reservations);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@DisplayName("Several reservations to retrieve")
		@Test
		void testFindAllReservationsWhenThereAreSeveralReservationsToRetrieve() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			LocalDate another_localdate = LocalDate.parse("2023-03-16");
			Reservation a_reservation = new Reservation(UUID.randomUUID(), a_localdate);
			Reservation another_reservation = new Reservation(UUID.randomUUID(), another_localdate);
			List<Reservation> reservations = Arrays.asList(a_reservation, another_reservation);
			when(reservationRepository.findAll()).thenReturn(reservations);
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThat(transactionalReservationManager.findAllReservations()).isEqualTo(reservations);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findAll();
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
		}
	}

	@Nested
	@DisplayName("Tests for 'findReservationOn'")
	class FindReservationOnTest {
		
		@DisplayName("Date input is null")
		@Test
		void testFindReservationOnWhenDateIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalReservationManager.findReservationOn(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Date of reservation to find cannot be null.");
		}
		
		@DisplayName("The reservation exists")
		@Test
		void testFindReservationOnWhenReservationExists() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(UUID.randomUUID(), a_localdate);
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.of(reservation));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThat(transactionalReservationManager.findReservationOn(a_localdate)).isEqualTo(reservation);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
		}
		
		@DisplayName("The reservation doesn't exist")
		@Test
		void testFindReservationOnWhenReservationDoesNotExist() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThatThrownBy(
					() -> transactionalReservationManager.findReservationOn(a_localdate))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("There is no reservation on \"" + a_localdate + "\" in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'insertNewReservation'")
	class InsertNewReservationTest {
		
		@BeforeEach
		void doStubbing() throws Exception {
			when(transactionManager.doInTransaction(
					ArgumentMatchers.<ClientReservationTransactionCode<?>>any()))
			.thenAnswer(
				answer((ClientReservationTransactionCode<?> code) -> 
					code.apply(clientRepository, reservationRepository)));
		}
		
		@DisplayName("Reservation input is null")
		@Test
		void testInsertNewReservationWhenReservationIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalReservationManager.insertNewReservation(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Reservation to insert cannot be null.");
			
			// TODO: change to zero se c'è
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@DisplayName("The reservation is actually new and the associated client exists")
		@Test
		void testInsertNewReservationWhenReservationIsNotYetInDatabaseAndAssociatedClientExists() {
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			UUID clientUUID = client.getUuid();
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(clientUUID, a_localdate);
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.empty());
			when(clientRepository.findById(clientUUID)).thenReturn(Optional.of(client));
			when(reservationRepository.save(reservation)).thenReturn(reservation); // TODO: serve?
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository, clientRepository);
			
			assertThat(transactionalReservationManager.insertNewReservation(reservation)).isEqualTo(reservation);
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			inOrder.verify(clientRepository).findById(clientUUID);
			inOrder.verify(reservationRepository).save(reservation);
		
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@DisplayName("Reservation is new and client doesn't exist")
		@Test
		void testInsertNewReservationWhenReservationIsNotYetInDatabaseAndAssociatedClientDoesNotExists() {
			Client client = new Client("Mario", "Rossi", new ArrayList<>());
			UUID clientUUID = client.getUuid();
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(clientUUID, a_localdate);
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.empty());
			when(clientRepository.findById(clientUUID)).thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository, clientRepository);
			
			assertThatThrownBy(
					() -> transactionalReservationManager.insertNewReservation(reservation))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage(
					"The client with uuid: " + clientUUID + ", associated to the reservation to insert "
					+ "is not in the database. Please, insert the client before the reservation.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			inOrder.verify(clientRepository).findById(clientUUID);
		
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@DisplayName("The reservation already exists")
		@Test
		void testInsertNewReservationWhenReservationIsAlreadyInDatabase() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(UUID.randomUUID(), a_localdate);
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.of(reservation));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThatThrownBy(
					() -> transactionalReservationManager.insertNewReservation(reservation))
				.isInstanceOf(InstanceAlreadyExistsException.class)
				.hasMessage(
					"Reservation [date=" + a_localdate.toString() + "] is already in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ClientReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
		
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository);
		}
	}
	
	@Nested
	@DisplayName("Tests for 'removeReservationOn'")
	class RemoveReservationOnTest {
		
		@DisplayName("Date input is null")
		@Test
		void testRemoveReservationOnWhenDateIsNullShouldThrow() {
			assertThatThrownBy(
					() -> transactionalReservationManager.removeReservationOn(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Date of reservation to remove cannot be null.");
			
			// TODO: change to zero se c'è
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository);
		}
		
		@DisplayName("The reservation is in database")
		@Test
		void testRemoveReservationOnWhenReservationIsInDatabase() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			Reservation reservation = new Reservation(UUID.randomUUID(), a_localdate);
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.of(reservation));
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThatNoException().isThrownBy(
					() -> transactionalReservationManager.removeReservationOn(a_localdate));
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			inOrder.verify(reservationRepository).delete(a_localdate);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository); // zero
		}
		
		@DisplayName("The reservation is already been deleted")
		@Test
		void testRemoveReservationOnWhenReservationIsNotInDatabase() {
			LocalDate a_localdate = LocalDate.parse("2023-04-24");
			when(reservationRepository.findByDate(a_localdate)).thenReturn(Optional.empty());
			
			InOrder inOrder = Mockito.inOrder(transactionManager, reservationRepository);
			
			assertThatThrownBy(
					() -> transactionalReservationManager.removeReservationOn(a_localdate))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("There is no reservation on \"" + a_localdate + "\" in the database.");
			
			inOrder.verify(transactionManager)
				.doInTransaction(ArgumentMatchers.<ReservationTransactionCode<?>>any());
			inOrder.verify(reservationRepository).findByDate(a_localdate);
			
			verifyNoMoreInteractions(transactionManager);
			verifyNoMoreInteractions(reservationRepository);
			verifyNoMoreInteractions(clientRepository); // zero
		}
	}
	
	@AfterEach
	void releaseMocks() throws Exception {
		closeable.close();
	}
}
