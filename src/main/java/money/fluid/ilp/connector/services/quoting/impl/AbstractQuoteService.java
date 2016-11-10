package money.fluid.ilp.connector.services.quoting.impl;


import money.fluid.ilp.connector.exceptions.InvalidQuoteRequestException;
import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import money.fluid.ilp.connector.model.quotes.Transfer;
import money.fluid.ilp.connector.services.quoting.QuoteService;
import money.fluid.ilp.connector.model.quotes.Quote;

import java.util.Objects;

/**
 * An abstract implementation of {@link QuoteService} that provides default functionality of a quoting service by
 * providing logic to detect if the quote request is for a fixed-source or fixed-destination quote.
 */
public abstract class AbstractQuoteService implements QuoteService {

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

        this.performExtendedValidation(sourceQuoteRequest, destinationQuoteRequest);
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

    /**
     * Assemble a {@link Quote} based upon a "fixed source" amount.  This means that the client requesting the quote
     * would like a fixed amount of asset transferred from the source, so the destination amount will vary depending on
     * fees and other attributes of this transfer.
     *
     * @param sourceQuoteRequest      An instance of {@link QuoteRequest} with information about the source transfer,
     *                                which is the transfer containing the Ledger crediting the connector's account (and
     *                                debiting the sender's account).
     * @param destinationQuoteRequest An instance of {@link QuoteRequest} with information about the destination
     *                                transfer, which is the transfer containing the Ledger debiting the connector's
     *                                account (and crediting the recipient's account).
     * @return
     */
    protected abstract Quote getQuoteForFixedSourceAmount(
            final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest
    );

    /**
     * Assemble a {@link Quote} based upon a "fixed destination" amount.  This means that the client requesting the
     * quote would like a fixed amount of asset transferred to  the destination, so the source amount will vary
     * depending on fees and other attributes of this transfer.
     *
     * @param sourceQuoteRequest      An instance of {@link QuoteRequest} with information about the source transfer,
     *                                which is the transfer containing the Ledger crediting the connector's account (and
     *                                debiting the sender's account).
     * @param destinationQuoteRequest An instance of {@link QuoteRequest} with information about the destination
     *                                transfer, which is the transfer containing the Ledger debiting the connector's
     *                                account (and crediting the recipient's account).
     * @return
     */
    protected abstract Quote getQuoteForFixedDestinationAmount(
            final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest
    );


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

        if (sourceQuoteRequest.getOptAmount().isPresent()) {
            return this.getQuoteForFixedSourceAmount(sourceQuoteRequest, destinationQuoteRequest);
        } else if (destinationQuoteRequest.getOptAmount().isPresent()) {
            return this.getQuoteForFixedDestinationAmount(sourceQuoteRequest, destinationQuoteRequest);
        } else {
            throw new RuntimeException("Either the source or destination amount must be specified!");
        }
    }
}
