package org.interledgerx.ilp.core;

import money.fluid.ilp.ledger.model.LedgerId;

/**
 * An extension of {@link LedgerTransfer} that represents a transfer where the best matching routing table entry names
 * another connector.   In other words, a connector will create a transfer of this type if it has no direct access to
 * the destination ledger.
 *
 * @param <DATA>         The type of object that {@link #getOptData()} should return.
 * @param <NOTE_TO_SELF> The type of object that the {@link #getOptNoteToSelf()} should return.
 * @see "https://github.com/interledger/rfcs/issues/77"
 */
public interface ForwardedLedgerTransfer<DATA, NOTE_TO_SELF> extends LedgerTransfer<DATA, NOTE_TO_SELF> {

    /**
     * Get the identifier of the {@link Ledger} that this transfer will be completed in.
     *
     * @return
     */
    LedgerId getLedgerId();

}
