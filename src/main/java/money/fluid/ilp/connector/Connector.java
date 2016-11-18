package money.fluid.ilp.connector;

import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.model.ConnectorInfo;

/**
 * An interface that defines an ILP connector.
 */
public interface Connector {

    ConnectorInfo getConnectorInfo();

    RoutingService getRoutingService();

    LedgerManager getLedgerManager();

    // TODO: Add startup and shutdown hooks?

    void shutdown();


}
