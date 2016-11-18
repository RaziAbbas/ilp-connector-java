package org.interledgerx.ilp.core.events;

import money.fluid.ilp.connector.Connector;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.model.LedgerId;
import org.interledgerx.ilp.core.LedgerInfo;

/**
 * An interface that models a connection between a single Connector and a single source-ledger where a connector can
 * initiate next-hop payments to satisfy an ILP transaction, and listen for ledger events in an asynchronous manner.
 * <p>
 * This interface allows implementations to be created that will handle only a single type of {@link LedgerEvent}, as
 * well as a single implementation that will handle all types of {@link LedgerEvent}.
 *
 * @param <T>
 */
public interface LedgerEventHandler<T extends LedgerEvent> {

    /**
     * Emits {@code event} to this handler for processing by any subscribers.
     *
     * @param event
     */
    void onLedgerEvent(T event);

    /**
     * @return An instance of {@link ConnectorId} that represents the connector listening to events.
     * <p>
     * TODO: Consider restricting this to just ConnectorInfo, or removing entirely.
     */
    Connector getListeningConnector();

    LedgerInfo getSourceLedgerInfo();
}
