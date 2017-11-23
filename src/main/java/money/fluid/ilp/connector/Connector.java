package money.fluid.ilp.connector;

import org.interledgerx.ilp.core.IlpAddress;

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

	boolean isTransferLocallyDeliverable(IlpAddress destinationAddress);

	boolean isTransferRemotelyForwardable(IlpAddress destinationAddress);
}
