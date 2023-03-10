package io.github.marcopaglio.booking.transaction.code;

import java.util.function.Function;

import io.github.marcopaglio.booking.repository.ClientRepository;

/*
 * This interface provides the ClientRepository's method to execute in a single transaction.
 */
@FunctionalInterface
public interface ClientTransactionCode<R> extends Function<ClientRepository, R> {

}
