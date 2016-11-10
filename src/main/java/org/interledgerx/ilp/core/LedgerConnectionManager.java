package org.interledgerx.ilp.core;

import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.ledger.model.ConnectionInfo;
import org.interledgerx.ilp.core.events.LedgerEvent;
import org.interledgerx.ilp.core.events.LedgerEventHandler;

/**
 * An interface that manages how outside processed can connect to a Ledger.
 * <p>
 * Examples of outside processed include a Connector, Connector-plugin, browser, etc.
 */
public interface LedgerConnectionManager {
    void connect(ConnectionInfo connectionInfo);

    void disconnect(ConnectorId connectorId);

    /**
     * Registers a {@link LedgerEventHandler} with a {@link Ledger}, associated to a particular connector.
     *
     * @param connectorId        A unique connector id.
     * @param ledgerEventHandler
     */
    void registerEventHandler(ConnectorId connectorId, LedgerEventHandler ledgerEventHandler);

    void notifyEventListeners(ConnectorId targetedConnectorId, LedgerEvent ledgerEvent);
}
