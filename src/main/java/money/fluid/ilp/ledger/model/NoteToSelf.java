package money.fluid.ilp.ledger.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.LedgerTransfer;

/**
 * A class that contains any information necessary for a host to be able to react to transfer events like condition
 * fulfillment at a later point in time.
 * <p>
 * This data could be encoded with a {@link LedgerTransfer}, but it would need to be encrypted.  Therefore, this class
 * allows the DefaultLedgerClient to merely store this information in-memory or in a datastore so that it does not have to
 * leave Connector-space.
 */
@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class NoteToSelf {

    @NonNull
    private final LedgerId originatingLedgerId;

}
