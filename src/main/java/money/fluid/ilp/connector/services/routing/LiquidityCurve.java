package money.fluid.ilp.connector.services.routing;

import money.fluid.ilp.connector.model.ids.ConnectorId;

/**
 * An interface that models a liquidity rate-curve for a pair of ILP Ledger addresses.  For example, if ledgerA deal in
 * USD, and ledgerB deals in euro, then this object would contain a series of points that map a price from ledgerA to a
 * price in LedgerB, or, from USD to EUR.  For example,
 * <p>
 * //TODO: Expand this doco.
 *
 * Search for SIMPLIFY_POINTS in ilp.  It's 10 for the simplify method.
 *
 * @see "https://github.com/hgoebl/simplify-java"
 * @see "https://github.com/metteo/jts"
 * @see "http://web.cs.sunyit.edu/~poissad/projects/Curve/about_algorithms/whyatt"\
 * @see "https://github.com/code42day/vis-why"
 *
 */
public interface LiquidityCurve extends Comparable<LiquidityCurve> {

    ConnectorId getConnectorId();

}
