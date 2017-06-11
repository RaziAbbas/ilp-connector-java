package money.fluid.ilp.ledger;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerTransfer;

import javax.money.MonetaryAmount;
import java.util.Objects;
import java.util.Optional;

/**
 * A service that queries each connector currently connected to this ledger and finds the best path for a given
 * destination address, amount, and any other data points pertinent to calculating a rate-curve.
 */
public interface QuotingService {

    /**
     * For a given {@link LedgerTransfer}, find the connector that is best able to service the request.
     *
     * @param destinationAddress An instance of {@link IlpAddress} that describes the final destination of the ILP
     *                           transaction.
     * @param destinationAmount  A {@link MonetaryAmount} describing the amount of money (in any currency) that is to be
     *                           sent to the final destination.  This amount may be denominated in the ledger's local
     *                           currency (i.e., a source-amount transfer), or it may be denominated in the destination
     *                           ledger's currency (a destination-amount transfer).
     * @return A {@link String} identifying a connector that is connected to this {@link Ledger}.
     */
    Optional<LedgerQuote> findBestConnector(
            final IlpAddress destinationAddress, final MonetaryAmount destinationAmount
    );

    /**
     * An implementation of {@link QuotingService} that merely finds the first connector with the lowest transaction
     * fee.
     */
    @RequiredArgsConstructor
    class Impl implements QuotingService {

        // TODO: Determine what th inputs to a quoting service are.  Ledger doesn't seem right.
//        @NonNull
//        private final Ledger ledger;

        // TODO: Turn this into a cache of some kind?
        // private

        @Override
        public Optional<LedgerQuote> findBestConnector(
                final IlpAddress destinationAddress, final MonetaryAmount destinationAmount
        ) {
            Objects.requireNonNull(destinationAddress);
            Objects.requireNonNull(destinationAmount);

            // First, find any connectors that can service the destination ledger.
            // Out of that group, get a Quote for the ledgerTransfer amount.
            // Using this, return the Connector that provides the best quote.
            // Also, return the exchange rate that the ledger should use.

            return Optional.empty();
            //return ledger.getConnectors().values().stream().findFirst();
        }

        /**
         * TODO: This method merely simulates a quoting function.  Instead, it should actually query the connector to
         * get a quote via ILQP.
         *
         * @param connectorId
         * @return
         */
//        private Quote getQuote(final ConnectorId connectorId, final LedgerTransfer ledgerTransfer) {
//            Objects.requireNonNull(connectorId);
//            Objects.requireNonNull(ledgerTransfer);
//
//            return new Quote.Builder()
//
//                    .withSourceLedgerId(ledgerTransfer.getLocalSourceAddress())
//
//
//                    .build();
//            //.feeAssetId(this.ledger.getLedgerInfo().getCurrencyCode())
//            //.originalAmount(ledgerTransfer.getDestinationAmount())
//            //.
//        }


    }

    @Getter
    @RequiredArgsConstructor
    @Builder
    @ToString
    @EqualsAndHashCode
    class LedgerQuote {
        private final MonetaryAmount transferAmount;
        private final ConnectorInfo destinationConnectorInfo;
    }

}
