package money.fluid.ilp.connector.services.routing;

import money.fluid.ilp.connector.model.ids.LedgerAddressPrefix;
import org.interledgerx.ilp.core.IlpAddress;

import javax.money.MonetaryAmount;
import java.util.Optional;

/**
 * A service for computing routes from one ILP account to another.
 */
public interface RoutingService {

    /**
     * Add a new route to the routing table.
     * <p>
     * NOTE: A connector may advertise more than a single route for a given destination prefix.
     *
     * @param destinationLedgerAddressPrefix   The {@link LedgerAddressPrefix} of the ledger/ledger type that this route
     *                                         will route towards.  This might be an entire {@link IlpAddress}, such
     *                                         as "fed.us.chase", or it might just be an ILP address prefix, like "fed"
     *                                         or "fed.us".
     * @param nextHopIlpAddressForConnector The ILP {@link IlpAddress} of the connector account on a given ledger
     *                                         that can accept funds to complete an ILP transfer.  In other words, this
     *                                         is the "source account" on the indicated ledger that the next-hop ledger
     *                                         will use to initiate the next hop ILP transaction to complete this
     *                                         overall transfer.
     * @param routeRate                        An instance of {@link RouteRate} that provides concrete pricing for a
     *                                         pair of ledgers.  For example, if a route from A->C can be serviced via
     *                                         ledgers A and B, then A and B would be the source ledgers, respectively.
     *                                         The ultimate "rate" for a given Route is based upon multiple pieces of
     *                                         data points such as the overall transfer amount, execution speed, etc.
     */
    void addRoute(
            final LedgerAddressPrefix destinationLedgerAddressPrefix,
            final IlpAddress nextHopIlpAddressForConnector,
            final RouteRate routeRate
    );


    /**
     * Removes all routes for a given destination and connector.
     * <p>
     * NOTE: A connector may advertise more than a single route for a given destination prefix.
     *
     * @param destinationPrefix
     * @param nextHopIlpAddressForConnector
     */
    void removeAllRoutes(
            final LedgerAddressPrefix destinationPrefix, final IlpAddress nextHopIlpAddressForConnector
    );

    /**
     * Removes a particular routes for a given destination and connector.
     * <p>
     * NOTE: A connector may advertise more than a single route for a given destination prefix.
     *
     * @param destinationPrefix
     * @param nextHopIlpAddressForConnector
     */
    void removeRoute(
            final LedgerAddressPrefix destinationPrefix, final IlpAddress nextHopIlpAddressForConnector,
            final RouteRate routeRate
    );

    /**
     * Computes a {@link Route}.
     * <p>
     * The "best hop" is the ILPAddress (i.e., {@LedgetAddress}) for the connector's account on the source ledger
     * indicated in this route.
     *
     * @return
     */
    Optional<Route> bestHopForSourceAmount(final IlpAddress destinationAddress, final MonetaryAmount sourceAmount);


    Optional<Route> bestHopForDestinationAmount(
            final IlpAddress destinationAddress, final MonetaryAmount destinationAmount
    );

    // TODO: Create unit tests for this implementation!
    //class Default implements RoutingService {

//        // A TRIE structure that allows for efficient prefix searching.  This data structure allows us to perform a get-by-prefix
//        // in the following manner:
//        // Example:
//        // map.insert("foo", 1)
//        // map.insert("bar", 2)
//        // map.get("foo")     // ⇒ 1
//        // map.get("foo.bar") // ⇒ 1 ("foo" is the longest known prefix of "foo.bar")
//        // map.get("bar")     // ⇒ 2
//        // map.get("bar.foo") // ⇒ 2 ("bar" is the longest known prefix of "bar.foo")
//        // map.get("random")  // ⇒ null
//
//        // Each element in the PatriciaTrie is a Multimap that contains an ordered Set of 1 or more RouteRates (ordered
//        // by RateCurve).  Each RouteRate contains a Route and a LiquidityCurve that can potentially be used for quoting
//        // purposes.
//
//        // TODO: Handle Route Expiration!
//
//        private final PatriciaTrie<SortedSetMultimap<ConnectorId, RouteRate>> destinations;
//
//        public Default(
//                //final Map<LedgerAddressPrefix, Multimap<ConnectorId, LiquidityCurve>> destinations
//                final PatriciaTrie<SortedSetMultimap<ConnectorId, RouteRate>> destinations
//        ) {
//            this.destinations = Objects.requireNonNull(destinations);
//        }
//
//        @Override
//        public synchronized void addRoute(
//                final LedgerAddressPrefix destinationPrefix, final ConnectorId nextHopConnectorId,
//                final RouteRate routeRate
//        ) {
//            Objects.requireNonNull(destinationPrefix);
//            Objects.requireNonNull(nextHopConnectorId);
//            Objects.requireNonNull(routeRate);
//
//            // Locate the sub-map, or intiallize a new one if it doesn't exist.
//            final Multimap<ConnectorId, RouteRate> routesByConnectorId = Optional.ofNullable(
//                    this.destinations.get(destinationPrefix.getId())
//            ).orElseGet(() -> {
//                // TODO: Implement a comparator for RouteRate so that the routes can be sorted in-memory.
//                final SortedSetMultimap<ConnectorId, RouteRate> subMap = TreeMultimap.create();
//                // Add the sub-map if it's not present...
//                destinations.put(destinationPrefix.getId(), subMap);
//                return subMap;
//            });
//
//            routesByConnectorId.put(nextHopConnectorId, routeRate);
//        }
//
//        @Override
//        public void removeAllRoutes(
//                final LedgerAddressPrefix destinationPrefix, final LedgerAddress nextHopLedgerAddressForConnector
//                ) {
//            Objects.requireNonNull(destinationPrefix);
//            Objects.requireNonNull(nextHopLedgerAddressForConnector);
//
//            // Locate the sub-map and remove any objects that have nextHopConnectorId as a key.
//            Optional.ofNullable(this.destinations.get(destinationPrefix.getId())).ifPresent(
//                    routesByConnectorId -> routesByConnectorId.removeAll(nextHopLedgerAddressForConnector)
//            );
//        }
//
//        @Override
//        public void removeRoute(
//                final LedgerAddressPrefix destinationPrefix, final LedgerAddress nextHopLedgerAddressForConnector,
//                final RouteRate routeRate
//        ) {
//            Objects.requireNonNull(destinationPrefix);
//            Objects.requireNonNull(nextHopLedgerAddressForConnector);
//            Objects.requireNonNull(routeRate);
//
//            Optional.ofNullable(this.destinations.get(destinationPrefix.getId())).ifPresent(
//                    routesByConnectorId -> {
//                        // Only remove a sinle Route...
//                        routesByConnectorId.remove(nextHopLedgerAddressForConnector, routeRate);
//                    }
//            );
//        }
//
//
//        @Override
//        public Optional<RouteRate> bestHopForSourceAmount(
//                final LedgerAddress destinationAddress, final MonetaryAmount sourceAmount
//        ) {
//            Objects.requireNonNull(destinationAddress);
//            Objects.requireNonNull(sourceAmount);
//
//            final LedgerAddressPrefix ledgerAddressPrefix = LedgerAddressPrefix.of(
//                    destinationAddress.getLedgerId().getId()
//            );
//
//            // For all routes in the table, we want to first find the routes that map to a given prefix.  This may return
//            // 0 or more connector accounts that can service the route.  Each Connector might advertise 0 or more routes for a
//            // given prefix, so we need to first group the results by connector, sort them, and then return the min of
//            // that result.
//
//
//            Optional<Optional<RouteRate>> map =
//                    // Map all destinations to just the destinations for a given prefix.
//                    Optional.ofNullable(this.destinations.get(ledgerAddressPrefix.getId()))
//                            // Group all
//                            .map(routesByConnectorId -> routesByConnectorId.va)
//            //.map(routesByConnectorId -> routesByConnectorId.values().stream().min(RouteRate::compare));
//        }
//
//        @Override
//        public Optional<Route> bestHopForDestinationAmount(
//                LedgerAddress destinationAddress, MonetaryAmount destinationAmount
//        ) {
//            // Find the best route...there may be multiple routes to the indicated connector.  For now, we simply return the first one.
//            // TODO: Implement proper rate-curves per js-ilp-routing.
//            return this.destinations.get(
//                    LedgerAddressPrefix.of(destinationAddress.getLedgerId().getId())).stream().findFirst();
//        }
    //  }
}
