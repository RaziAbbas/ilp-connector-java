package org.interledgerx.ilp.core.events;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;


public class LedgerDirectTransferEvent extends LedgerTransferEvent {

    private static final long serialVersionUID = 6955247421334987499L;

    public LedgerDirectTransferEvent(
            final LedgerInfo source, final InterledgerPacketHeader ilpPacketHeader,
            final LedgerAccountId localFromAccount,
            final LedgerAccountId localToAccount, final MonetaryAmount localTransferAmount
    ) {

        super(source, ilpPacketHeader, localFromAccount, localToAccount, localTransferAmount);

        //For a direct transfer the header should be for an optimistic mode ILP payment
        if (!ilpPacketHeader.isOptimisticModeHeader()) {
            throw new IllegalArgumentException(
                    "A direct transfer should not have a condition or timeout in the ILP header.");
        }

    }

}
