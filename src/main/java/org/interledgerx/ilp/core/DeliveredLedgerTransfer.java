package org.interledgerx.ilp.core;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

/**
 * An extension of {@link LedgerTransfer} that represents a transfer where the best-matching routing table entry is a
 * local ledger.  In other words, a Connector can initiate a transfer of this type if no further ILP Connectors are
 * required to fulfill an ILP transaction.
 *
 * @param <DATA>         The type of object that {@link #getOptData()} should return.
 * @param <NOTE_TO_SELF> The type of object that the {@link #getOptNoteToSelf()} should return.
 * @see "https://github.com/interledger/rfcs/issues/77"
 */
public interface DeliveredLedgerTransfer<DATA, NOTE_TO_SELF> extends LedgerTransfer<DATA, NOTE_TO_SELF> {

    /**
     * Get the identifier of the {@link Ledger} that this transfer will be completed in.
     *
     * @return
     */
    LedgerId getLedgerId();

    /**
     * Get {@link LedgerAccountId} for the local account that funds are being credited to.  This is not an instance of
     * {@link IlpAddress} because a transfer should operate on only a single Ledger.
     *
     * @return An {@link LedgerAccountId} for the local destination account.
     */
    IlpAddress getLocalDestinationAddress();



}
