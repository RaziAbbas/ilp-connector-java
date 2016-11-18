package money.fluid.ilp.connector.managers.ledgers;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;

import java.util.Optional;

/**
 * A service that tracks pending transfers.
 * <p>
 * Pending transfers are transfers that a Connector has made on a connected ledger in order to complete its own portion
 * of the ILP protocol.  Because each pending transfer reduces the liquidity of the connector, it is important for the
 * Connector to be able to properly track these transfers at all times.
 * <p>
 * This service is responsible for persistent storage and caching of these pending transfers.
 */
public interface PendingTransferManager {

    /**
     * Add a {@link PendingTransfer} to this manager for future management.
     *
     * @param pendingTransfer
     */
    void addPendingTransfer(PendingTransfer pendingTransfer);

    /**
     * Remove a {@link PendingTransfer} from this manager.
     *
     * @param ilpTransactionId
     */
    void removePendingTransfer(IlpTransactionId ilpTransactionId);

    /**
     * Get a pending transfer by {@link IlpTransactionId}.
     *
     * @param ilpTransactionId
     * @return
     */
    Optional<PendingTransfer> getPendingTransfer(IlpTransactionId ilpTransactionId);
}