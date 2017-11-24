package money.fluid.ilp.connector.web.controllers;


import money.fluid.ilp.connector.services.quoting.QuoteService;
import money.fluid.ilp.connector.model.QuoteJson;
import money.fluid.ilp.connector.model.quotes.Quote;
import org.interledgerx.ilp.core.Ledger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A REST endpoint for serving the /quote resource, which is a mechanism to provide a quote for a given connector
 * exchange transaction.
 */
// - Razi - Enable Later @RestController
public class QuoteController {
    private final QuoteService quoteService;

    /**
     * Required-args Constructor.
     *
     * @param quoteService An instance of {@link QuoteService} that handles all quote processing.
     */
    @Inject
    public QuoteController(final QuoteService quoteService) {
        this.quoteService = Objects.requireNonNull(quoteService);
    }

    /**
     * The main controller for providing Quotes to external callers.
     *
     * @param sourceAmount        A fixed amount to be debited from the Sender's account on the specified {@link
     *                            Ledger}.  This amount is set by the connector if the {@code destinationAmount} is
     *                            specified, and should not be specified in that case.
     * @param sourceLedgerId      The unique identifier of the source {@link Ledger}.
     * @param sourceExpiry        The number of milliseconds between when the destination transfer is proposed and when
     *                            it expires.  (Minimum allowed based on destination_expiry_duration.
     * @param destinationAmount   A fixed amount to be credited to the Receivers's account on the specified {@link
     *                            Ledger}.  This amount is set by the connector_if the {@code sourceAmount} is
     *                            specified, and should not be specified in that case.
     * @param destinationLedgerId The unique identifier of the destination {@link Ledger}.
     * @param destinationExpiry   The number of milliseconds between when the source transfer is proposed and when it
     *                            expires (Maximum allowed if unspecified).
     * @return An instance of {@link Quote}
     */
    @RequestMapping(path = "/quote", method = RequestMethod.GET)
    public Quote get(
            @RequestParam("source_amount") final BigDecimal sourceAmount,
            @RequestParam("source_ledger") final String sourceLedgerId,
            @RequestParam("source_expiry") final Long sourceExpiry,
            @RequestParam("destination_amount") final BigDecimal destinationAmount,
            @RequestParam("destination_ledger") final String destinationLedgerId,
            @RequestParam("destination_expiry") final Long destinationExpiry
    ) {

        Objects.requireNonNull(sourceLedgerId);
        Objects.requireNonNull(destinationLedgerId);

//        final Transfer sourceTransfer = new Transfer.Builder().withAmount(sourceAmount).withExpiryDuration(
//                sourceExpiry).withLedgerId(new StringId<>(sourceLedgerId)).withAmount(sourceAmount).build();
//        final Transfer destinationTransfer = new Transfer.Builder().withAmount(sourceAmount).withExpiryDuration(
//                destinationExpiry).withLedgerId(new StringId<>(destinationLedgerId)).withAmount(
//                destinationAmount).build();


        // TODO: FIXME!
        final Quote quote = null; //this.quoteService.getQuote(sourceTransfer, destinationTransfer);
        return new QuoteJson(quote);
    }
}
