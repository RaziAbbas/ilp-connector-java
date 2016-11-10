package money.fluid.ilp.connector.services.routing;

import org.interledgerx.ilp.core.IlpAddress;
import org.joda.time.DateTime;

import java.util.Optional;

/**
 * A route from one account to another using ILP.
 */
public interface Route {
    LiquidityCurve getLiquidityCurve();

    // For a given ILP transaction, this is the ledgerAddress on the source-ledger that a Connector will transfer money
    // out of (into escrow) to initiate the next hop operation.
    IlpAddress getSourceAddress();

    // TODO: Is this needed for routing?  Aren't there cases where we don't actually know the next ledger address, but only know the destination?
    //private final LedgerAddress nextLedgerAddress;

    // The ultimate final destination for an ILP payment...
    IlpAddress getDestinationAddress();

    // An ordered list of Connector hops that this route should proceed through.  Is this knowable ahead of time?
    //private final TreeSet<ConnectorId> hops;

    // TODO: Do routes always expire?  In other words, might this be optional?
    Optional<DateTime> getOptExpiresAt();
}
