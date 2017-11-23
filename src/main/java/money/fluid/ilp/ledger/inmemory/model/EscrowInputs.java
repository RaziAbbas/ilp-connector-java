package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.joda.time.DateTime;

import javax.money.MonetaryAmount;
import java.util.Date;
import java.util.Optional;

/**
 * A class that outlines the data necessary to create an escrow arrangement between two parties for which a
 * given ledger is the escrow agent.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class EscrowInputs {

    // Holds the ilp transaction id, the ultimate source and destination of funds from an ILP perspective (might be
    // different from the local accounts in this escrow)
    @NonNull
    private InterledgerPacketHeader interledgerPacketHeader = null;

    // The ILP address of the local account that funded this escrow. This is often different
    // from ILP Source Address.
    @NonNull
    private IlpAddress localSourceAddress = null;

    // The ILP address of the local account that will recieve funds if this escrow is executed.  This is often different
    // from ILP Destination Address.
    @NonNull
    private IlpAddress localDestinationAddress = null;

    // This is the expiry of the escrow/transfer...
    @NonNull
    private Optional<DateTime> optExpiry = null;
    
	@NonNull
    private MonetaryAmount amount = null;

	public EscrowInputs(InterledgerPacketHeader vinterledgerPacketHeader, IlpAddress vlocalSourceAddress, IlpAddress vlocalDestinationAddress, MonetaryAmount vdestinationAmount, Optional<DateTime> voptExpiry) {
		this.interledgerPacketHeader = vinterledgerPacketHeader;
		this.localSourceAddress = vlocalSourceAddress;
		this.localDestinationAddress = vlocalDestinationAddress;
		this.amount = vdestinationAmount;
		this.optExpiry = voptExpiry;		
	}
	
    public InterledgerPacketHeader getInterledgerPacketHeader() {
		return interledgerPacketHeader;
	}

	public IlpAddress getLocalSourceAddress() {
		return localSourceAddress;
	}

	public IlpAddress getLocalDestinationAddress() {
		return localDestinationAddress;
	}

	public Optional<DateTime> getOptExpiry() {
		return optExpiry;
	}

	public MonetaryAmount getAmount() {
		return amount;
	}
}
