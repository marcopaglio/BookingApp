package io.github.marcopaglio.booking.transaction.code;

import java.util.function.BiFunction;

import io.github.marcopaglio.booking.repository.ClientRepository;
import io.github.marcopaglio.booking.repository.ReservationRepository;

/**
 * This interface provides code that involves both {@code ClientRepository}'s
 * and {@code ReservationRepository}'s methods.
 * 
 * @param <R> the returned type of executed code.
 */
public interface ClientReservationTransactionCode<R>
extends BiFunction<ClientRepository, ReservationRepository, R> {

}
