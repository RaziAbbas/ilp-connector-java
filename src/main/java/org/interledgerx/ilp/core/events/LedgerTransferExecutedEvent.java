package org.interledgerx.ilp.core.events;

import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;

public class LedgerTransferExecutedEvent extends LedgerTransferEvent {

    private static final long serialVersionUID = 2742406317777118624L;

    public LedgerTransferExecutedEvent(
            LedgerInfo source,
            InterledgerPacketHeader ilpPacketHeader, IlpAddress localFromAccount,
            IlpAddress localToAccount, MonetaryAmount localTransferAmount
    ) {

        super(source, ilpPacketHeader, localFromAccount, localToAccount,
              localTransferAmount
        );

    }

}
