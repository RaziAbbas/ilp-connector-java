package org.interledgerx.ilp.core.events;

import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;

/**
 * An event that is sent by a ledger when an amount of funds has been escrowed and is ready to take part in an ILP
 * transaction.
 */
public class LedgerTransferPreparedEvent extends LedgerTransferEvent {

    private static final long serialVersionUID = 4965377395551079045L;

    public LedgerTransferPreparedEvent(
            //final ConnectorInfo connectorInfo,
            LedgerInfo source,
            InterledgerPacketHeader ilpPacketHeader, IlpAddress localSourceAccount,
            IlpAddress localDestinationAccount, MonetaryAmount localTransferAmount
    ) {

        super(source, ilpPacketHeader, localSourceAccount, localDestinationAccount,
              localTransferAmount
        );
    }

}
