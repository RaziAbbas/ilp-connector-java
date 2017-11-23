package org.interledgerx.ilp.core.events;

import com.google.common.base.MoreObjects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;

// Lombok only for quick prototyping...will remove if this makes it into ILP core.
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class LedgerTransferEvent extends LedgerEvent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final InterledgerPacketHeader ilpPacketHeader;
    private final IlpAddress localSourceAddress;
    private final IlpAddress localDestinationAddress;
    public InterledgerPacketHeader getIlpPacketHeader() {
		return ilpPacketHeader;
	}

	public IlpAddress getLocalSourceAddress() {
		return localSourceAddress;
	}

	public IlpAddress getLocalDestinationAddress() {
		return localDestinationAddress;
	}

	public MonetaryAmount getAmount() {
		return amount;
	}

	private final MonetaryAmount amount;

    /**
     * @param source
     * @param ilpPacketHeader
     * @param localSourceAddress
     * @param localDestinationAddress
     * @param amount
     */
    public LedgerTransferEvent(
            final LedgerInfo source, final InterledgerPacketHeader ilpPacketHeader,
            final IlpAddress localSourceAddress, final IlpAddress localDestinationAddress, final MonetaryAmount amount
    ) {
        super(source);

        this.ilpPacketHeader = ilpPacketHeader;
        this.localSourceAddress = localSourceAddress;
        this.localDestinationAddress = localDestinationAddress;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ilpPacketHeader", ilpPacketHeader)
                .toString();
    }
}
