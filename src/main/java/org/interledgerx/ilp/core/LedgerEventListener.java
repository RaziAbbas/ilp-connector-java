package org.interledgerx.ilp.core;

import org.interledgerx.ilp.core.events.LedgerEvent;
import org.interledgerx.ilp.core.events.LedgerEventHandler;

/**
 * An interface that defines how any listener may register handlers for events emitted by a {@link Ledger}.
 * This interface might be implemented by a Ledger running in ILP ledger-space, or a LedgerClient running in ILP
 * connector-space, for example.
 * <p>
 * NOTE: It is intended that each instance of this interface should operate on behalf of a single ledger-to-connector
 * relationship.  In other words, a single instance of {@link LedgerEventListener} should not be emitting events to more
 * than a single Connector and/or Ledger.
 * <p>
 * TODO: Propose this interface to ILPCore.
 */
public interface LedgerEventListener {

    /**
     * Register an event handler to subscribe to events from a single Ledger.  This method will either succeed, or throw
     * a {@link RuntimeException} if registration fails for some unforseen reason.
     *
     * @param handler An instance of {@link LedgerEventHandler} that will handle events emitted from a {@link Ledger}.
     */
    void registerEventHandler(LedgerEventHandler<?> handler);

    /**
     * Unregister a particular event handlers identified by {@code handler}.  This method will either succeed, or throw
     * a {@link RuntimeException} if registration fails for some unforseen reason.
     *
     * @param handler
     */
    void unRegisterEventHandler(LedgerEventHandler<?> handler);

    /**
     * Unregister all event handlers.  This method will either succeed, or throw a {@link RuntimeException} if
     * unregistration fails for some unforseen reason.
     */
    void unRegisterEventHandlers();

    /**
     * Posts a {@link LedgerEvent} to all registered subscribers. This method will return successfully after the event
     * has been posted to all subscribers, and regardless of any exceptions thrown by subscribers.
     * <p>
     * This method will either succeed, or throw a {@link RuntimeException} if registration fails for some unforseen
     * reason.
     * <p>
     * If no subscribers have been subscribed for {@code ledgerEvent}'s class, it is left to the implementation to
     * decided how to handle the event.  For example, one implementation might choose to simply ignore unhandled events,
     * possibly with a warning log emission.  Conversely, other implementations may decide to throw an exception in
     * these cases..
     *
     * @param ledgerEvent {@link LedgerEvent} to post.
     */
    void notifyEventHandlers(final LedgerEvent ledgerEvent);


    /**
     * The ledger that this listener is listening to.
     *
     * @return
     */
    LedgerInfo getLedgerInfo();

    /**
     * The connector that this listener is listening on behalf of.  This is necessary for the consumers of this listener
     * to properly route events
     *
     * @return
     */
    //ConnectorInfo getConnectorInfo();

    /**
     * The {@link IlpAddress} of the client (Connector, Wallet, etc) that this listener is listening on behalf of.  This
     * is necessary for the consumers of this listener to properly route events.
     *
     * @return
     */
    IlpAddress getListeningIlpAddress();

}
