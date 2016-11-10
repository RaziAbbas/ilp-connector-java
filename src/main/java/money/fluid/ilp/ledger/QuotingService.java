package money.fluid.ilp.ledger;

import lombok.RequiredArgsConstructor;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerTransfer;

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
     * @param ledgerTransfer An instance of {@link LedgerTransfer} that has all appropriate information necessary to
     *                       find the best connector for an ILP transfer.
     * @return A {@link String} identifying a connector that is connected to this {@link Ledger}.
     */
    Optional<ConnectorInfo> findBestConnector(final LedgerTransfer ledgerTransfer);

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
        public Optional<ConnectorInfo> findBestConnector(final LedgerTransfer ledgerTransfer) {
            Objects.requireNonNull(ledgerTransfer);

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
//            //.originalAmount(ledgerTransfer.getAmount())
//            //.
//        }


    }
}
