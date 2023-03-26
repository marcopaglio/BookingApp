package io.github.marcopaglio.booking.transaction.code;

import java.util.function.Function;

import io.github.marcopaglio.booking.repository.ClientRepository;

/**
 * This interface provides code that involves the {@code ClientRepository}'s method(s).
 *
 * @param <R> the returned type of executed code.
 */
@FunctionalInterface
public interface ClientTransactionCode<R> extends Function<ClientRepository, R> {

}
