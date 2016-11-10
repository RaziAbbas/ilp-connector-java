package org.interledgerx.ilp.core.events;

import com.google.common.base.MoreObjects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;

// Lombok only for quick prototyping...will remove if this makes it into ILP core.
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class LedgerTransferEvent extends LedgerEvent {
    private final InterledgerPacketHeader ilpPacketHeader;
    private final LedgerAccountId localSourceAccount;
    private final LedgerAccountId localDestinationAccount;
    private final MonetaryAmount amount;

    /**
     * @param source
     * @param ilpPacketHeader
     * @param sourceAccount
     * @param destinationAccount
     * @param amount
     */
    public LedgerTransferEvent(
            final LedgerInfo source, final InterledgerPacketHeader ilpPacketHeader,
            final LedgerAccountId sourceAccount, final LedgerAccountId destinationAccount, final MonetaryAmount amount
    ) {
        super(source);

        this.ilpPacketHeader = ilpPacketHeader;
        this.localSourceAccount = sourceAccount;
        this.localDestinationAccount = destinationAccount;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ilpPacketHeader", ilpPacketHeader)
                .toString();
    }
}
