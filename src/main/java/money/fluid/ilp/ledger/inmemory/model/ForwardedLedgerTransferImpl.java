package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledger.model.NoteToSelf;
import org.interledgerx.ilp.core.ForwardedLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;

import javax.money.MonetaryAmount;
import java.util.Optional;

@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class ForwardedLedgerTransferImpl implements ForwardedLedgerTransfer<String, NoteToSelf> {

    @NonNull
    private  InterledgerPacketHeader interledgerPacketHeader = null;

    @NonNull
    private  LedgerId ledgerId = null;

    @NonNull
    private  IlpAddress localSourceAddress = null;

    @NonNull
    private  MonetaryAmount amount = null;

    @NonNull
    private  Optional<String> optData = null;

    @NonNull
    private  Optional<NoteToSelf> optNoteToSelf = null;

	public ForwardedLedgerTransferImpl(InterledgerPacketHeader ilpPacketHeader, LedgerId ledgerId2,
			IlpAddress ledgerLocalSourceAddress, MonetaryAmount zero, Optional<Object> empty, Optional<Object> empty2) {
		this.interledgerPacketHeader = ilpPacketHeader;
		this.ledgerId = ledgerId2;
		this.localSourceAddress = ledgerLocalSourceAddress;
		this.amount = zero;
		this.optData = null;
		this.optNoteToSelf = null;
		
		
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
	public LedgerId getLedgerId() {
		return this.ledgerId;
	}

//    /**
//     * Required-args Consstructor for creation of an instance with no condition nor expiry, using the source ILP address
//     * as the local source address.
//     */
//    public ForwardedLedgerTransferImpl(
//            final InterledgerPacketHeader interledgerPacketHeader, final LedgerId ledgerId,
//            final IlpAddress localSourceAddress
//    ) {
//        this(
//                interledgerPacketHeader,
//                ledgerId,
//                localSourceAddress,
//                Optional.empty(),
//                Optional.empty()
//        );
//    }

}
