package money.fluid.ilp.connector.services.routing;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.joda.time.DateTime;

import java.util.Optional;

@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DefaultRoute implements Route {

    private final LiquidityCurve liquidityCurve = null;

    // For a given ILP transaction, this is the ledgerAddress on the source-ledger that a Connector will transfer money
    // out of (into escrow) to initiate the next hop operation.
    private final IlpAddress sourceAddress = null;

    // TODO: Is this needed for routing?  Aren't there cases where we don't actually know the next ledger address, but only know the destination?
    //private final LedgerAddress nextLedgerAddress;

    // The ultimate final destination for an ILP payment...
    private final IlpAddress destinationAddress = null;

    // An ordered list of Connector hops that this route should proceed through.  Is this knowable ahead of time?
    //private final TreeSet<ConnectorId> hops;

    // TODO: Do routes always expire?  In other words, might this be optional?
    private final Optional<DateTime> optExpiresAt = null;

	@Override
	public LiquidityCurve getLiquidityCurve() {
		return this.liquidityCurve;
	}

	@Override
	public IlpAddress getSourceAddress() {
		return this.sourceAddress;
	}

	@Override
	public IlpAddress getDestinationAddress() {
		return this.destinationAddress;
	}

	@Override
	public Optional<DateTime> getOptExpiresAt() {
		return this.optExpiresAt;
	}

//    private final RouteInfo routeInfo;

    //private final long minMessageWindow;

    // TODO: Add this, default to absent!
    //private final Optional<Object> additionalInfo;


//////////////
    // Un-necessary...
//////////////

    // The local account identifier for the source ledger.  E.g., "mark" or "123"
    //private final LedgerAccountId sourceLedgerAccountId;
    //private final LedgerId destinationLedgerId;

    //private final ConnectorId connectorId;  Un-necessary because this route lives on a Connector.

//    /**
//     * @param sourceLedgerId
//     * @param hops
//     * @param expiresAt
//     * @deprecated Remove this constructor and use the required-args constructor, possibly via a Factory.
//     */
//    @Deprecated
//    public DefaultRoute(
//            LedgerAddress sourceLedgerId, TreeSet<ConnectorId> hops, DateTime expiresAt
//    ) {
//        this.liquidityCurve = new LiquidityCurve() {
//            //TODO
//        };
//        this.sourceLedgerId = sourceLedgerId;
//        this.hops = hops;
//        this.expiresAt = expiresAt;
//        this.routeInfo = new RouteInfo() {
//            //TODO
//        };
//    }

//    public DefaultRoute(
//            LiquidityCurve liquidityCurve, LedgerId sourceLedgerId, TreeSet<ConnectorId> hops, DateTime expiresAt,
//            RouteInfo routeInfo
//    ) {
//        this.liquidityCurve = liquidityCurve;
//        this.sourceLedgerId = sourceLedgerId;
//        this.hops = hops;
//        this.expiresAt = expiresAt;
//        this.routeInfo = routeInfo;
//    }
//
//    this.sourceLedger =hops[0]
//            this.nextLedger =hops[1]
//            this.destinationLedger =hops[hops.length -1]
//
//            this.minMessageWindow =info.minMessageWindow
//
//    this.expiresAt =info.expiresAt
//    this.additionalInfo =info.additionalInfo
//
//    this.connector =info.connector
//    this.sourceAccount =info.sourceAccount
//    this.destinationAccount =info.destinationAccount
//

}
