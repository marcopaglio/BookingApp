package io.github.marcopaglio.booking.transaction.code;

import java.util.function.Function;

import io.github.marcopaglio.booking.repository.ReservationRepository;

/*
 * This interface provides the ReservationRepository's method to execute in a single transaction.
 */
public interface ReservationTransactionCode<R> extends Function<ReservationRepository, R> {

}
