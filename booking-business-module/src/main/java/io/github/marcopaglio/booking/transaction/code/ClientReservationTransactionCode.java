package io.github.marcopaglio.booking.transaction.code;

import java.util.function.BiFunction;

import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;

/*
 * This interface provides code that involves both ClientRepository's and ReservationRepository's methods.
 */
public interface ClientReservationTransactionCode<R>
extends BiFunction<ClientRepository, ReservationRepository, R> {

}
