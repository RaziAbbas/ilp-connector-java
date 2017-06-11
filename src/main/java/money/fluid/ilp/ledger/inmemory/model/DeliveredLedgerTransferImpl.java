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

@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DeliveredLedgerTransferImpl implements DeliveredLedgerTransfer<String, NoteToSelf> {

    @NonNull
    private final InterledgerPacketHeader interledgerPacketHeader;

    @NonNull
    private final IlpAddress localSourceAddress;

    @NonNull
    private final IlpAddress localDestinationAddress;

    @NonNull
    private final MonetaryAmount amount;

    @NonNull
    private final Optional<String> optData;

    @NonNull
    private final Optional<NoteToSelf> optNoteToSelf;

    /**
     * For a delivered transfer, the ledgerId is the ledgerId of the destination account address.
     */
    public LedgerId getLedgerId() {
        return this.getLocalDestinationAddress().getLedgerId();
    }
}
