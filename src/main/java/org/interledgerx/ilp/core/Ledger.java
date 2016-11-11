package org.interledgerx.ilp.core;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.LedgerAccountManager;
import org.interledger.cryptoconditions.Fulfillment;

/**
 * An ILP Ledger that can send and receive ILP payment transactions.
 */
public interface Ledger {

    /**
     * Retrieve some meta-data about the ledger.
     *
     * @return <code>LedgerInfo</code>
     */
    LedgerInfo getLedgerInfo();

    /**
     * Initiates a ledger-local transfer.
     *
     * @param transfer <code>LedgerTransfer</code>
     */
    void send(LedgerTransfer transfer);

    /**
     * Reject a transfer
     * <p>
     * This should only be allowed if the entity rejecting the transfer is the
     * receiver
     *
     * @param reason
     */
    void rejectTransfer(IlpTransactionId ilpTransactionId, LedgerTransferRejectedReason reason);

    /**
     * Submit a fulfillment to a ledger.
     * <p>
     * The ledger will execute all transfers that are fulfilled by this
     * fulfillment.
     *
     * @param fulfillment the fulfillment for this transfer
     */
    void fulfillCondition(Fulfillment fulfillment);

    /**
     * Submit an optimistic-mode fulfillment to a ledger.
     * <p>
     * The ledger will execute all transfers for the specified {@link IlpTransactionId} since the ILP transaction is an
     * optimistic-mode transfer, and no fulfillment is required.
     */
    void fulfillCondition(final IlpTransactionId ilpTransactionId);

    // Accessors for managers that a Ledger must support.

    LedgerConnectionManager getLedgerConnectionManager();

    LedgerAccountManager getLedgerAccountManager();

}
