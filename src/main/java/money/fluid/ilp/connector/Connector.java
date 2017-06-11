package money.fluid.ilp.connector;

import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.model.ConnectorInfo;
import money.fluid.ilp.connector.services.ExchangeRateService;
import money.fluid.ilp.connector.services.routing.RoutingService;

/**
 * An interface that defines an ILP connector.
 */
public interface Connector {

    ConnectorInfo getConnectorInfo();

    RoutingService getRoutingService();

    LedgerManager getLedgerManager();

    ExchangeRateService getExchangeRateService();

    // TODO: Add startup and shutdown hooks?

    void shutdown();
}
