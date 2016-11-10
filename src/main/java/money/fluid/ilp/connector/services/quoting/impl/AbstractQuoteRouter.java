package money.fluid.ilp.connector.services.quoting.impl;

import com.google.common.annotations.VisibleForTesting;
import money.fluid.ilp.connector.exceptions.InvalidQuoteRequestException;
import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import money.fluid.ilp.connector.model.quotes.Transfer;
import money.fluid.ilp.connector.services.SupportedAssetsService;
import money.fluid.ilp.connector.services.quoting.QuoteRouter;
import money.fluid.ilp.connector.services.quoting.QuoteService;
import money.fluid.ilp.connector.services.quoting.QuoteService.LocalQuoteService;
import money.fluid.ilp.connector.services.quoting.QuoteService.RemoteQuoteService;
import money.fluid.ilp.connector.model.quotes.Quote;

import java.util.Objects;

/**
 * An abstract implementation of {@link QuoteRouter} that determines whether to route a quote request to a local or
 * remote implementation of {@link QuoteService}.
 */
public abstract class AbstractQuoteRouter implements QuoteRouter {

    private final SupportedAssetsService supportedAssetsService;
    private final LocalQuoteService localQuoteService;
    private final RemoteQuoteService remoteQuoteService;

    /**
     * Required-args Constructor.
     *
     * @param localQuoteService      An instance of {@link LocalQuoteService}.
     * @param remoteQuoteService     An instance of {@link RemoteQuoteService}.
     * @param supportedAssetsService An instance of {@link SupportedAssetsService}.
     */
    protected AbstractQuoteRouter(
            final SupportedAssetsService supportedAssetsService,
            final LocalQuoteService localQuoteService, final RemoteQuoteService remoteQuoteService
    ) {
        this.supportedAssetsService = Objects.requireNonNull(supportedAssetsService);
        this.localQuoteService = Objects.requireNonNull(localQuoteService);
        this.remoteQuoteService = Objects.requireNonNull(remoteQuoteService);
    }


    /**
     * The main entry-point for creating a quote for the supplied transfer requests.  This method must do several
     * things:
     * <p/>
     * <ol> <li> Determine which accounts on the sender and destination are owned by the connector. </li> <li>Determine
     * which account (sender or destination) will collect the commission.</li> </ol>
     * <p/>
     * Note: Asset accounts are increased with a "debit" and decreased with a "credit".
     *
     * @param sourceQuoteRequest      An instance of {@link Transfer} with information about the source of the asset
     *                                being transferred.
     * @param destinationQuoteRequest An instance of {@link Transfer} with information about the destination of the
     *                                asset being transferred.
     * @return
     */
    @Override
    public Quote getQuote(
            final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest
    ) {
        Objects.requireNonNull(sourceQuoteRequest);
        Objects.requireNonNull(destinationQuoteRequest);

        // 1. Validate the incoming request
        this.validateQuoteRequest(sourceQuoteRequest, destinationQuoteRequest);

        // 2. Determine if this quote can be fulfilled locally, or must be forwarded to a downstream connector
        if (this.supportedAssetsService.isLocallyServiced(sourceQuoteRequest, destinationQuoteRequest)) {
            return this.localQuoteService.getQuote(sourceQuoteRequest, destinationQuoteRequest);
        } else if (this.supportedAssetsService.isRemotelyServiced(
                sourceQuoteRequest, destinationQuoteRequest)) {
            return this.remoteQuoteService.getQuote(sourceQuoteRequest, destinationQuoteRequest);
        } else {
            throw new InvalidQuoteRequestException(
                    "The specified asset pairing is not supported by this connector!");
        }
    }

    /**
     * Valid the incoming payload for an ILP Quote request.  To be valid, each request must have a ledger id.
     * Additionally, at least one of the requests must have an amount specified.
     *
     * @param sourceQuoteRequest      An instance of {@linkn QuoteRequest} representing the asset transfer coming from
     *                                an ILP source.
     * @param destinationQuoteRequest An instance of {@linkn QuoteRequest} representing the asset transfer going to an
     *                                ILP destination.
     * @throws InvalidQuoteRequestException
     */
    @VisibleForTesting
    protected final void validateQuoteRequest(
            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
    ) throws InvalidQuoteRequestException {
        Objects.requireNonNull(sourceQuoteRequest);
        Objects.requireNonNull(destinationQuoteRequest);

        if (sourceQuoteRequest.getLedgerId() == null) {
            throw new InvalidQuoteRequestException("Source Ledger Id must be specified!");
        }

        if (destinationQuoteRequest.getLedgerId() == null) {
            throw new InvalidQuoteRequestException("Destination Ledger Id must be specified!");
        }

        if (!sourceQuoteRequest.getOptAmount().isPresent() && !destinationQuoteRequest.getOptAmount().isPresent()) {
            throw new InvalidQuoteRequestException("Either the Source or Destination amount must be specified!");
        }

        if (sourceQuoteRequest.getOptAmount().isPresent() && destinationQuoteRequest.getOptAmount().isPresent()) {
            throw new InvalidQuoteRequestException("Only the Source or Destination amount may be specified!");
        }

        performExtendedValidation(sourceQuoteRequest, destinationQuoteRequest);
    }

    /**
     * An extension point that implementors can use to perform extended validation on a quote request.
     *
     * @param sourceQuoteRequest      An instance of {@linkn QuoteRequest} representing the asset transfer coming from
     *                                an ILP source.
     * @param destinationQuoteRequest An instance of {@linkn QuoteRequest} representing the asset transfer going to an
     *                                ILP destination.
     * @throws InvalidQuoteRequestException
     */
    protected void performExtendedValidation(
            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
    ) throws InvalidQuoteRequestException {

    }

}
