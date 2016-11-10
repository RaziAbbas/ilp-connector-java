package money.fluid.ilp.connector.services.quoting;


import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import money.fluid.ilp.connector.model.quotes.Quote;

/**
 * A service for routing quote requests to the appropriate implementation of {@link QuoteService}.
 * <p>
 * There are various types of asset transfer request.  The first is an intra-ledger transfer, and generally involves
 * neither ILP nor a transaction fee since the ledger provider can handle the entire transfer inside of a single
 * ledger.
 * <p>
 * ILP comes into play when a transfer is requested to be performed across two different ledgers that contain the same
 * type of asset.  From the perspective of a Connector, there are three possible scenarios that might exist:
 * <p>
 * 1.) Disconnected Source/Disconnected Destination: The connector has neither an account on the source ledger nor on
 * destination ledger.  Thus, the connector cannot provide a quote, but mainly because the source is not connected to
 * it.  To simplify ILP quoting, we restrict connectors that are not connected to the source from issuing a quote, even
 * though this type of connector might be able to find a negative path back to the source connector via some other
 * connector that might be connected to the source.
 * <p>
 * 2.) Connected Source/Connected Destination: The connector has an account on the source ledger, has an account on the
 * destination ledger, and supports transfers between the two ledgers.  Quoting will be calculated entirely by the
 * connector, and will depend on various factors including liquidity and timeliness.
 * <p>
 * 3.) Connected Source/Disconnected Destination: The connector has an account on the source ledger, but does not have
 * an account on the destination ledger.  In this case, the Connector will need to provide liquidity to the source by
 * allowing money to transfer from the account on the source ledger to an account controlled by the connector on the
 * source ledger.  Additionally, the connector will need to find another connector that can transfer from the
 * connector's account on the source ledger to the connector's account on a destination ledger.  This type of transfer
 * will be rare because it's likely there is another route between the source and destination connector, and that route
 * will likely be cheaper than a route that traverses multiple hops.
 */
public interface QuoteRouter {
    /**
     * Route a quote request based upon the supplied information, and return a {@link Quote}.
     *
     * @param sourceQuoteRequest      An instance of {@link QuoteRequest} with information about the source of the asset
     *                                being transferred.
     * @param destinationQuoteRequest An instance of {@link QuoteRequest} with information about the destination of the
     *                                asset being transferred.
     * @return An instance of {@link Quote}.
     */
    Quote getQuote(final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest);
}
