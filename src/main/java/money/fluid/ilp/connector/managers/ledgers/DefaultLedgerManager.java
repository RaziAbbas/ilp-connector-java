package money.fluid.ilp.connector.managers.ledgers;

import lombok.Getter;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.DeliveredLedgerTransfer;
import org.interledgerx.ilp.core.ForwardedLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A default implementation of {@link LedgerManager} that uses an in-memory cache to track pending transfers
 * and act upon them in a timely manner, as well as to allow them to be expired automatically.
 */
@Getter
public class DefaultLedgerManager implements LedgerManager {

    private final ConnectorId connectorId;
    private final Set<LedgerClient> ledgerClients;

    // The Connector needs to track the pending transfers, not the event handlers.  This is because event handler1 will
    // receive a transfer for 1 ledger and make another transfer on another ledger.  When the transfer executes, the 2nd
    // handler won't have the originating ledger's info, so it gets it from here.
    // TODO: Consider holding the entire transfer here, or loading-cache that is backed by a Database.
    // TODO: This should be backed by a datastore since these transfers should not be lost until they are expired or fulfilled.
    private final PendingTransferManager pendingTransferManager;

    /**
     * Required-args Constructor.
     *
     * @param connectorId
     * @param ledgerClients
     * @param pendingTransferManager
     */
    public DefaultLedgerManager(
            final ConnectorId connectorId,
            final Set<LedgerClient> ledgerClients,
            final PendingTransferManager pendingTransferManager
    ) {
        this.connectorId = Objects.requireNonNull(connectorId);
        this.ledgerClients = Objects.requireNonNull(ledgerClients);
        this.pendingTransferManager = Objects.requireNonNull(pendingTransferManager);
    }

    @Override
    public void deliverPayment(final LedgerId sourceLedgerId, final DeliveredLedgerTransfer ledgerTransfer) {
        Objects.requireNonNull(sourceLedgerId);
        Objects.requireNonNull(ledgerTransfer);

        // Track the transfer for later...
//        final NoteToSelf noteToSelf = NoteToSelf.builder().originatingLedgerId(
//                ledgerId.getLedgerInfo().getLedgerId()).build();

        // Because this is a delivery, the ledgerTransfer should have the local destination adress.
        this.findLedgerClientSafely(ledgerTransfer.getLedgerId()).send(ledgerTransfer);

        // Track the pending payment before sending to the ledger...
        this.pendingTransferManager.addPendingTransfer(
                PendingTransfer.of(
                        ledgerTransfer,
                        sourceLedgerId
                ));
    }

    @Override
    public void forwardPayment(final LedgerId sourceLedgerId, final ForwardedLedgerTransfer ledgerTransfer) {
        Objects.requireNonNull(sourceLedgerId);
        Objects.requireNonNull(ledgerTransfer);

        // Track the transfer for later...
//        final NoteToSelf noteToSelf = NoteToSelf.builder().originatingLedgerId(
//                ledgerId.getLedgerInfo().getLedgerId()).build();

        // TODO: This method is specifying the ledgerId as calculated by the Connector, but perhaps it should be determining the LedgerId?
        this.findLedgerClientSafely(ledgerTransfer.getLedgerId()).send(ledgerTransfer);

        // Track the pending payment before sending to the ledger...
        this.pendingTransferManager.addPendingTransfer(PendingTransfer.of(
                ledgerTransfer,
                sourceLedgerId
        ));
    }

    @Override
    public void rejectPayment(
            final IlpTransactionId ilpTransactionId, final LedgerTransferRejectedReason ledgerTransferRejectedReason
    ) {
        Objects.requireNonNull(ilpTransactionId);

        this.getOriginatingLedgerId(ilpTransactionId).ifPresent((ledgerId -> {
            this.findLedgerClientSafely(ledgerId).rejectTransfer(ilpTransactionId, ledgerTransferRejectedReason);
        }));
        // Remove the pending payment _after_ sending to the ledger...
        this.pendingTransferManager.removePendingTransfer(ilpTransactionId);
    }

    /**
     * Given an {@link IlpTransactionId}, return the {@link LedgerId} that initiated this transfer.  This method is used
     * to fulfill and reject pending transfers.
     *
     * @param ilpTransactionId
     * @return
     */
    @Override
    public Optional<LedgerId> getOriginatingLedgerId(final IlpTransactionId ilpTransactionId) {
        Objects.requireNonNull(ilpTransactionId);
        return this.getPendingTransferManager()
                .getPendingTransfer(ilpTransactionId)
                .map(pendingTransfer -> pendingTransfer.getLedgerId());
    }

    @Override
    public IlpAddress getConnectorAccountOnLedger(final LedgerId ledgerId) {
        return this.findLedgerClientSafely(ledgerId).getConnectionInfo().getLedgerAccountId();
    }
}
