package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.model.NoteToSelf;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.interledgerx.ilp.core.LedgerTransfer;

import javax.money.MonetaryAmount;
import java.util.Date;
import java.util.Optional;

/**
 * An implementation of {@link LedgerTransfer} that is generally created by the first ledger in an ILP transaction, and
 * includes only ILP-related info.
 */
@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class InitialLedgerTransferImpl implements LedgerTransfer<String, NoteToSelf> {

    @NonNull
    private final InterledgerPacketHeader interledgerPacketHeader;

    @NonNull
    private final MonetaryAmount amount;

    // This is the expiry of the transfer..
    @NonNull
    private final Optional<Date> optExpiry;

    @NonNull
    private final Optional<String> optData;

    @NonNull
    private final Optional<NoteToSelf> optNoteToSelf;

    public InitialLedgerTransferImpl(InterledgerPacketHeader vinterledgerPacketHeader, MonetaryAmount vamount, Optional<Date> voptExpiry, Optional<String> voptData, Optional<NoteToSelf> voptNoteToSelf) {
    	this.interledgerPacketHeader = vinterledgerPacketHeader;
    	this.amount = vamount;
    	this.optExpiry = voptExpiry;
    	this.optData = voptData;
    	this.optNoteToSelf = voptNoteToSelf;
    }

    public InterledgerPacketHeader getInterledgerPacketHeader() {
		return interledgerPacketHeader;
	}

	public MonetaryAmount getAmount() {
		return amount;
	}

	public Optional<Date> getOptExpiry() {
		return optExpiry;
	}

	public Optional<String> getOptData() {
		return optData;
	}

	public Optional<NoteToSelf> getOptNoteToSelf() {
		return optNoteToSelf;
	}

	/**
     * Required-args Constructor for creation of an instance with no condition nor expiry, using the source ILP address
     * as the local source address.
     */
    public InitialLedgerTransferImpl(
            final IlpTransactionId ilpTransactionId, final IlpAddress sourceAddress,
            final IlpAddress destinationAddress, final MonetaryAmount amount
    ) {
        this(
                new InterledgerPacketHeader(ilpTransactionId, sourceAddress, destinationAddress, amount),
                amount,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    @Override
    public IlpAddress getLocalSourceAddress() {
        return this.getInterledgerPacketHeader().getSourceAddress();
    }
}
