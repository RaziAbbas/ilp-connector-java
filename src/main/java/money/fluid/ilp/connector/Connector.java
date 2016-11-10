package money.fluid.ilp.connector;

import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.InMemoryLedgerClient;
import money.fluid.ilp.ledgerclient.LedgerClient;

import java.util.Optional;
import java.util.Set;

/**
 * An interface that defines an ILP connector.
 */
public interface Connector {

    ConnectorInfo getConnectorInfo();

    Set<LedgerClient> getLedgerClients();

    RoutingService getRoutingService();

    // TODO: Add startup and shutdown hooks?

    void shutdown();

    /**
     * Given the specified {@link LedgerId}, find any instances of {@link LedgerClient} for which this connector is
     * listening to events for.  In general, a Conenctor will have only a single client listening to a given ledger, but
     * it's possible there are more than one.
     *
     * @param ledgerId The {@link LedgerId} that uniquely identifies the {@link InMemoryLedgerClient} to return.
     * @return
     */
    default Optional<LedgerClient> getLedgerClient(final LedgerId ledgerId) {
        return this.getLedgerClients().stream()
                .filter(ledgerClient -> ledgerClient.getLedgerInfo().getLedgerId().equals(ledgerId))
                .findAny();
    }
}
