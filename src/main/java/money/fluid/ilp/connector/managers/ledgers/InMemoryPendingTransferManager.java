package money.fluid.ilp.connector.managers.ledgers;

import lombok.RequiredArgsConstructor;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An implementation of {@link PendingTransferManager} that stores all pending transfers in-memory, and expires them
 * after {@code defaultExpirationSeconds} seconds.
 * <p>
 * This implementation uses a simple Guava cache to hold any pending transfers, and expires entries at the
 * appropriate time, but all at once (as opposed to on a per-transfer basis).
 * <p>
 * WARNING: This implementation should not be used in a production environment since it does NOT utilize a
 * persistent datastore to store pending transfers.  This has a variety of implications, but for example, if a
 * Connector using this implementation were restarted, it would lose its ability to expire pending transfers, which
 * could cause a Connector to lose money.
 */
@RequiredArgsConstructor
public class InMemoryPendingTransferManager implements PendingTransferManager {

    //private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<IlpTransactionId, PendingTransfer> pendingTransfers;

    /**
     * No-args Constructor.
     */
    public InMemoryPendingTransferManager() {
        this.pendingTransfers = new HashMap<>();
    }

    @Override
    public void addPendingTransfer(final PendingTransfer pendingTransfer) {
        this.pendingTransfers.put(
                pendingTransfer.getLedgerTransfer().getInterledgerPacketHeader().getIlpTransactionId(),
                pendingTransfer
        );
    }

    @Override
    public void removePendingTransfer(final IlpTransactionId ilpTransactionId) {
        this.pendingTransfers.remove(ilpTransactionId);
    }

    @Override
    public Optional<PendingTransfer> getPendingTransfer(IlpTransactionId ilpTransactionId) {
        return Optional.ofNullable(this.pendingTransfers.get(ilpTransactionId));
    }
}
