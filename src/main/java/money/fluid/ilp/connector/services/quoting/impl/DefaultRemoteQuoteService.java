package money.fluid.ilp.connector.services.quoting.impl;


import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import money.fluid.ilp.connector.services.quoting.QuoteService.RemoteQuoteService;
import money.fluid.ilp.connector.model.quotes.Quote;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * An implementation of {@link RemoteQuoteService} that assumes this connector has an account on the source ledger, but
 * not on the remote ledger.   As such, this service will delegate out to a different connector and ask it for a Quote.
 * <p>
 * WARNING: This implementation is a WorkInProgress and should not be used in production.
 * <p>
 * TODO: Quote caching (Hysterix?) TODO: ILQP implementation to talk to remote systems? TODO: How does ILQP discovery
 * work?  Related to ILP Routing (?)
 */
@Service
@Qualifier("default")
public class DefaultRemoteQuoteService extends AbstractQuoteService implements RemoteQuoteService {

    //TODO: Make the attributes of this connector configurable so that installers may set various attributes.
    //TODO: Make this implementation pluggable so that implementors can supply their own implementations.

    @Override
    protected Quote getQuoteForFixedSourceAmount(
            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
    ) {
        return null;
    }

    @Override
    protected Quote getQuoteForFixedDestinationAmount(
            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
    ) {
        return null;
    }
}
