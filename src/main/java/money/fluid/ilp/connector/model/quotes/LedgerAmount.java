package money.fluid.ilp.connector.model.quotes;

import money.fluid.ilp.ledger.model.LedgerId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * An interface that models a ledger identifier and a quanity/amount of the asset tracked by that ledger.  Since in ILP,
 * a ledger can only contain a single asset type, so only the ledger id is required to specify an asset and its
 * quantity.
 */
public interface LedgerAmount {

    /**
     * Retrieves the {@link LedgerId} associated with this {@link QuoteRequest}.
     *
     * @return An instance of {@link LedgerId}.
     */
    LedgerId getLedgerId();

    /**
     * Retrieves the optionally present decimal amount of the asset associated with this {@link LedgerAmount}.
     *
     * @return An instance of {@link BigDecimal}.
     */
    Optional<BigDecimal> getOptAmount();
}
