package org.interledgerx.ilp.core;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.LedgerAccountManager;
import org.interledger.cryptoconditions.Fulfillment;

/**
 * The Common Ledger API is a RESTful API served by a ledger (or an adapter), which provides functionality necessary for
 * ILP compatibility. The Common Ledger API provides a single standard API that a ledger can serve in order to ease
 * integration with other Interledger Protocol components and applications, such as the reference ILP Client and ILP
 * Connector. This is not the only way a ledger can become ILP-enabled, but it provides a template that minimizes the
 * integration work necessary for compatibility with ILP software.
 *
 * @see "https://github.com/interledger/rfcs/blob/7a6a4b0723c347759c0366836c48c546efc9f268/0012-common-ledger-api/0012-common-ledger-api.md"
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
     * @param ilpTransactionId
     * @param fulfillment      the fulfillment for this transfer
     */
    void fulfillCondition(IlpTransactionId ilpTransactionId, Fulfillment fulfillment);

    /**
     * Submit an optimistic-mode fulfillment to a ledger.
     * <p>
     * The ledger will execute all transfers for the specified {@link IlpTransactionId} since the ILP transaction is an
     * optimistic-mode transfer, and no fulfillment is required.
     */
    //void fulfillCondition(IlpTransactionId ilpTransactionId);

    // Accessors for managers that a Ledger must support.

    LedgerConnectionManager getLedgerConnectionManager();

    LedgerAccountManager getLedgerAccountManager();

}
