package money.fluid.ilp.connector.managers.ledgers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;
import org.interledgerx.ilp.core.LedgerTransfer;

/**
 * A class that holds all neccesary information about a pending ILP transfer.
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
// TODO: Transform this into an interface.
public class PendingTransfer {

    @NonNull
    private final LedgerTransfer ledgerTransfer;

    // Add a LedgerId here so that when this is timed out, the manager can grab the client and reject
    @NonNull
    private final LedgerId ledgerId;

    public static PendingTransfer of(final LedgerTransfer ledgerTransfer, final LedgerId ledgerIdToNotify) {
        return new PendingTransfer(ledgerTransfer, ledgerIdToNotify);
    }

    // TODO: Consider NoteToSelf.  From the JS impl it appears that each transfer has its own identifier in addition
    // to the ILP transaction id, so this may need to be refactored.
}
