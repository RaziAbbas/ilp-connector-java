package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledger.model.NoteToSelf;
import org.interledgerx.ilp.core.DeliveredLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;

import javax.money.MonetaryAmount;
import java.util.Optional;


// TODO - Razi - Possibly duplicate with ForwardedLedgerTransferImpl. Look to make common
@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DeliveredLedgerTransferImpl implements DeliveredLedgerTransfer<String, NoteToSelf> {

    @NonNull
    private  InterledgerPacketHeader interledgerPacketHeader = null;

    @NonNull
    private  IlpAddress localSourceAddress = null;

    @NonNull
    private  IlpAddress localDestinationAddress = null;

    @NonNull
    private  MonetaryAmount amount = null;

    @NonNull
    private  Optional<String> optData = null;

    @NonNull
    private  Optional<NoteToSelf> optNoteToSelf = null;

    public DeliveredLedgerTransferImpl(InterledgerPacketHeader ilpPacketHeader, IlpAddress ledgerLocalSourceAddress,
			IlpAddress ledgerLocalDestinationAddress, MonetaryAmount amount2, Optional<Object> empty,
			Optional<Object> empty2) {
    	this.interledgerPacketHeader = ilpPacketHeader;
    	this.localSourceAddress = ledgerLocalSourceAddress;
		this.localDestinationAddress = ledgerLocalDestinationAddress;
		this.amount = amount2;
		this.optData = null;
		this.optNoteToSelf = null;
	}

	/**
     * For a delivered transfer, the ledgerId is the ledgerId of the destination account address.
     */
    public LedgerId getLedgerId() {
        return this.getLocalDestinationAddress().getLedgerId();
    }

	@Override
	public InterledgerPacketHeader getInterledgerPacketHeader() {
		return this.interledgerPacketHeader;
	}

	@Override
	public IlpAddress getLocalSourceAddress() {
		return this.localSourceAddress;
	}

	@Override
	public MonetaryAmount getAmount() {
		return this.amount;
	}

	@Override
	public Optional<String> getOptData() {
		return this.optData;
	}

	@Override
	public Optional<NoteToSelf> getOptNoteToSelf() {
		return this.optNoteToSelf;
	}

	@Override
	public IlpAddress getLocalDestinationAddress() {
		return this.localDestinationAddress;
	}
}
