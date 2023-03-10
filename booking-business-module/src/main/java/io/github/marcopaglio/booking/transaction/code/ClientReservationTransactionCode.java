package io.github.marcopaglio.booking.transaction.code;

import java.util.function.BiFunction;

import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;

/*
 * This interface provides both ClientRepository's and ReservationRepository's
 * methods to execute in a single transaction.
 */
public interface ClientReservationTransactionCode<R>
extends BiFunction<ClientRepository, ReservationRepository, R> {

}
