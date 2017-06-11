package org.interledgerx.ilp.core;

import javax.money.MonetaryAmount;
import java.util.Optional;

/**
 * @param <DATA>         The type of object that {@link #getOptData()} should return.
 * @param <NOTE_TO_SELF> The type of object that the {@link #getOptNoteToSelf()} should return.
 */
public interface LedgerTransfer<DATA, NOTE_TO_SELF> {

    /**
     * Get the packet header for this transfer, based upon all information contained in the Transfer.
     *
     * @return the Interledger Packet Header
     */
    InterledgerPacketHeader getInterledgerPacketHeader();

    /**
     * Get the local account that funds are being debited from, as an ILP {@link IlpAddress}.  This field exists because
     * the local source account may differ from {@link InterledgerPacketHeader#getSourceAddress()}, for example, an ILP
     * transfer that involves multiple connectors and ledgers.
     *
     * @return An {@link IlpAddress} for the local source account.
     */
    IlpAddress getLocalSourceAddress();

    /**
     * The amount of this transfer (this can differ from the amount in the ILP packet due to FX or connector fee
     * reductions).
     *
     * @return
     */
    MonetaryAmount getAmount();

    /**
     * Get the data to be sent.
     * <p>
     * Ledger plugins SHOULD treat this data as opaque, however it will usually
     * start with an ILP header followed by a transport layer header, a quote
     * request or a custom user-provided data packet.
     * <p>
     * If the data is too large, the ledger plugin MUST throw a
     * MaximumDataSizeExceededError. If the data is too large only because the
     * amount is insufficient, the ledger plugin MUST throw an
     * InsufficientAmountError.
     *
     * @return a buffer containing the data
     */
    Optional<DATA> getOptData();

    /**
     * Get the host's internal memo.  This can be encoded on the wire in any format chosen by an implementation, while
     * being treated as a typed object in the JVM.
     * <p>
     * For example, this could be an optional bytestring containing details the host needs to persist with the transfer
     * in order to be able to react to transfer events like condition fulfillment later.
     * <p>
     * Ledger plugins MAY attach the noteToSelf to the transfer and let the ledger store it. Otherwise it MAY use the
     * store in order to persist this field. Regardless of the implementation, the ledger plugin MUST ensure that all
     * instances of the transfer carry the same noteToSelf, even across different machines.
     * <p>
     * Ledger plugins MUST ensure that the data in the noteToSelf either isn't shared with any untrusted party or
     * encrypted before it is shared.
     *
     * @return a buffer containing the data
     */
    Optional<NOTE_TO_SELF> getOptNoteToSelf();


}
