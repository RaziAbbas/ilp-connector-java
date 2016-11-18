package money.fluid.ilp.connector.managers.ledgers;

import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.DeliveredLedgerTransfer;
import org.interledgerx.ilp.core.ForwardedLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An interface that defines how a Connector can communicate with and manage any connected {@link Ledger} to facilitate
 * ILP transactions, including ledger transfers whose ILP destination address exists on a directly connected {@link
 * Ledger} (i.e., locally delivered payment), as well as transfers that must be serviced by a different connector (i.e.,
 * a forwarded payment).
 * <p>
 * In ILP, a connector will complete its part of an interledger payment by reacting to funds received on one ledger and
 * sending funds to another account on a different ledger to fullfil its portion of ILP.  Because this type of activity
 * involves holding Connector funds on multiple ledgers (i.e., tying up Connector liquidity), this service also handles
 * ledger transaction timeouts by tracking pending payments, reversing timed-out escrows, and executing escrows in
 * response to a valid fulfillment.
 */
public interface LedgerManager {

    /**
     * Begin the process of initiating a transfer.  This includes the following steps:
     * <p>
     * <pre>
     *  <ol>
     *      <li>Create a local ledger transfer, including the cryptographic condition, and authorize this transfer on
     * the local ledger.</li>
     *      <li>Wait for the local ledger to put the sender's funds on hold and notify this connector that this has been
     * completed.</li>
     *      <li>Receive the notification from the Ledger, and extract the ILP packet to determine if the payment should
     * be forwarded.</li>
     *
     *  </ol>
     * </pre>
     */
    //void initiateTransfer();

    /**
     * Get the {@link ConnectorId} for the Connector this {@link LedgerManager} is operating for.
     *
     * @return
     */
    ConnectorId getConnectorId();

    Set<LedgerClient> getLedgerClients();

    /**
     * Delivery occurs when the best matching routing table entry is a local ledger.  This method facilitates such a
     * payment to the appropriate locally connected ledger.
     *
     * @param sourceLedgerId The {@link LedgerId} of the ledger that should be notified when the {@code ledgerTransfer}
     *                       is either fulfilled, rejected, or timed-out.
     * @param ledgerTransfer
     * @see "https://github.com/interledger/rfcs/issues/77"
     */
    void deliverPayment(final LedgerId sourceLedgerId, final DeliveredLedgerTransfer ledgerTransfer);

    /**
     * Forwarding occurs when the best matching (longest prefix) routing table entry names another connector. In other
     * words, when a connector has no direct access to the destination ledger.
     *
     * @param sourceLedgerId The {@link LedgerId} of the ledger that should be notified when the {@code ledgerTransfer}
     *                       is either fulfilled, rejected, or timed-out.
     * @param ledgerTransfer
     */
    void forwardPayment(final LedgerId sourceLedgerId, final ForwardedLedgerTransfer ledgerTransfer);

    /**
     * Rejection of a payment occurs when the routing table entry identifies an account that this ledger cannot route
     * to.  In this case, any ILP transactions are unwound and all asset transfers are reversed.
     */
    void rejectPayment(
            final IlpTransactionId ilpTransactionId, final LedgerTransferRejectedReason ledgerTransferRejectedReason
    );

    /**
     * Given an {@link IlpTransactionId}, return the {@link LedgerId} that initiated the transfer.  This method is used
     * to fulfill and reject pending transfers.
     *
     * @param ilpTransactionId
     * @return
     */
    Optional<LedgerId> getOriginatingLedgerId(final IlpTransactionId ilpTransactionId);

    /**
     * Given the specified {@link LedgerId}, find any instances of {@link LedgerClient} for which this connector is
     * listening to events for.  In general, a Conenctor will have only a single client listening to a given ledger, but
     * it's possible there are more than one.
     *
     * @param ledgerId The {@link LedgerId} that uniquely identifies the {@link LedgerClient} to return.
     * @return
     */
    default Optional<LedgerClient> findLedgerClient(final LedgerId ledgerId) {
        Objects.requireNonNull(ledgerId);
        return this.getLedgerClients().stream()
                .filter(ledgerClient -> ledgerClient.getLedgerInfo().getLedgerId().equals(ledgerId))
                .filter(ledgerClient -> ledgerClient.isConnected())
                .findAny();
    }

    /**
     * Helper method to get the proper {@link LedgerClient} for the indicated {@link LedgerId}, or throw an
     * exception if the Ledger cannot be found, is disconnected, or is otherwise not available.
     *
     * @param ledgerId
     * @return
     * @throws RuntimeException if no connected {@link LedgerClient} can be found.
     */
    default LedgerClient findLedgerClientSafely(final LedgerId ledgerId) {
        return this.findLedgerClient(ledgerId).orElseThrow(
                () -> new RuntimeException(String.format("No LedgerClient found for LedgerId: ", ledgerId)));
    }

    /**
     * For a given {@link LedgerId}, return the {@link IlpAddress} of the account that the connector should transact
     * with for that ledger.
     *
     * @param ledgerId
     */
    default IlpAddress getConnectorAccountOnLedger(final LedgerId ledgerId) {
        return this.findLedgerClientSafely(ledgerId).getConnectionInfo().getLedgerAccountId();
    }
}
