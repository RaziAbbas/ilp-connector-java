package money.fluid.ilp.connector.services.routing;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;

/**
 * A class that contains both a {@link Route} and a {@link LiquidityCurve} that allows a Connector to set rates for a
 * pairs of Ledgers that represent a source and destination for funds that this connector can service via ILP.
 */
@Getter
@RequiredArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
// TODO Make this an interface
public class RouteRate implements Comparable<RouteRate> {

    @NonNull
    private final LedgerId sourceLedgerId;

    @NonNull
    private final LedgerId destinationLedgerId;

    @NonNull
    private final Route route;

    @NonNull
    private final LiquidityCurve liquidityCurve;

    // TODO: Unit Tests!
    @Override
    public int compareTo(RouteRate o) {
        return compare(this, o);
    }

    // TODO: Unit Tests!
    public static int compare(RouteRate o1, RouteRate o2) {
        return o1.compareTo(o2);
    }

// No ConnectorId needed here because these objects should be stored by Connector.
}
