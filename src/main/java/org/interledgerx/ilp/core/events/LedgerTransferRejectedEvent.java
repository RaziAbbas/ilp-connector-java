package org.interledgerx.ilp.core.events;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;

import javax.money.MonetaryAmount;

public class LedgerTransferRejectedEvent extends LedgerTransferEvent {

    private static final long serialVersionUID = 5106316912312360715L;

    private LedgerTransferRejectedReason reason;

    public LedgerTransferRejectedEvent(
            //final ConnectorInfo connectorInfo,
            LedgerInfo source,
            InterledgerPacketHeader ilpPacketHeader, LedgerAccountId localFromAccount,
            LedgerAccountId localToAccount, MonetaryAmount localTransferAmount, LedgerTransferRejectedReason reason
    ) {

        super(source, ilpPacketHeader, localFromAccount, localToAccount,
              localTransferAmount
        );

        this.reason = reason;
    }

    public LedgerTransferRejectedReason getReason() {
        return reason;
    }

}
