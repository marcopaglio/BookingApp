package io.github.marcopaglio.booking.transaction.code;

import java.util.function.Function;

import io.github.marcopaglio.booking.repository.ReservationRepository;

/*
 * This interface provides code that involves the ReservationRepository's method(s).
 */
public interface ReservationTransactionCode<R> extends Function<ReservationRepository, R> {

}
