package org.interledgerx.ilp.core;

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

    void disconnect(IlpAddress ilpAddress);

    /**
     * Registers a {@link LedgerEventHandler} with a {@link Ledger}, associated to a particular connector.
     *
     * @param ilpAddress         A unique client id.
     * @param ledgerEventHandler
     */
    void registerEventHandler(IlpAddress ilpAddress, LedgerEventHandler ledgerEventHandler);

    void notifyEventListeners(IlpAddress ilpAddress, LedgerEvent ledgerEvent);
}
