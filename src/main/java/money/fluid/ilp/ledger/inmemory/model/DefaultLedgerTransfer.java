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
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DefaultLedgerTransfer implements LedgerTransfer<String, NoteToSelf> {

    @NonNull
    private final InterledgerPacketHeader interledgerPacketHeader;

    @NonNull
    private final IlpAddress localSourceAddress;

    @NonNull
    private final Optional<IlpAddress> optLocalDestinationAddress;

    @NonNull
    private final MonetaryAmount amount;

    @NonNull
    private final Optional<String> optData;

    @NonNull
    private final Optional<NoteToSelf> optNoteToSelf;

    /**
     * Required-args Consstructor for creation of an instance with no condition nor expiry, using the source ILP address
     * as the local source address.
     */
    public DefaultLedgerTransfer(
            final IlpAddress sourceAddress, final IlpAddress destinationAddress, final MonetaryAmount amount
    ) {
        this(
                new InterledgerPacketHeader(
                        IlpTransactionId.of(UUID.randomUUID().toString()), sourceAddress, destinationAddress, amount
                ),
                sourceAddress, Optional.empty(), amount, Optional.empty(),
                Optional.empty()
        );
    }
}
