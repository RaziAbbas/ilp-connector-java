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
    private LedgerTransfer ledgerTransfer = null;

    // Add a LedgerId here so that when this is timed out, the manager can grab the client and reject
    @NonNull
    private LedgerId ledgerId = null;

    public PendingTransfer(LedgerTransfer ledgerTransfer2, LedgerId ledgerIdToNotify) {
		this.ledgerTransfer = ledgerTransfer2;
		this.ledgerId = ledgerIdToNotify;
	}

	public LedgerTransfer getLedgerTransfer() {
		return ledgerTransfer;
	}

	public LedgerId getLedgerId() {
		return ledgerId;
	}

	public static PendingTransfer of(final LedgerTransfer ledgerTransfer, final LedgerId ledgerIdToNotify) {
        return new PendingTransfer(ledgerTransfer, ledgerIdToNotify);
    }

    // TODO: Consider NoteToSelf.  From the JS impl it appears that each transfer has its own identifier in addition
    // to the ILP transaction id, so this may need to be refactored.
}
