package money.fluid.ilp.ledger;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.inmemory.exceptions.EscrowException;
import money.fluid.ilp.ledger.inmemory.model.Escrow;
import money.fluid.ilp.ledger.inmemory.model.EscrowInputs;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that manages escrow for a Ledger.  A ledger will hold some assets in escrow that, when executed, will be
 * credited to the target account for an escrow.  Conversely, if an escrow is reversed, then the assets will be credited
 * back to the source account for that escrow.
 * <p>
 * This implementation allows for only a single-escrow account per ledger.  However, more complicated implementations
 * might allow for a more advanced mapping between escrow source-accounts, escrow accounts, and escrow destination
 * accounts.
 */
// TODO: Make this an interface!
public class EscrowManager {

    // TODO A proper ledger will want to track the various states of the initiateEscrow for auditing.

    private final LedgerInfo ledgerInfo;

    // The main ledger to move funds around in...
    private final LedgerAccountManager ledgerAccountManager;

    // The ILP account to add and remove escrow from...
    private final IlpAddress escrowAccountAddress;

    // TODO: Is this necessary?  A real ledger would always want to have an accounting of where the money in its initiateEscrow account has come from at any given time.  This could be modeled as another "ledger", or it could merely be an audit log?

    // Indicates any amounts that are currently in initiateEscrow from a given source ledger account.
    private final Map<IlpTransactionId, Escrow> escrows = new ConcurrentHashMap();

    public EscrowManager(
            final LedgerInfo ledgerInfo, final LedgerAccountId escrowAccountId,
            final LedgerAccountManager ledgerAccountManager
    ) {
        this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
        this.ledgerAccountManager = Objects.requireNonNull(ledgerAccountManager);

        // __escrow__ account!
        this.escrowAccountAddress = IlpAddress.of(escrowAccountId, ledgerInfo.getLedgerId());
    }

    /**
     * Create an initiateEscrow transaction by debiting an {@code amount} of the associated ledger's asset from  {@code
     * {@link EscrowInputs#getSourceAddress()} and crediting the same amount into the initiateEscrow account for the
     * associated ledger.
     *
     * @param escrowInputs An instance of {@link EscrowInputs} with all information required to initiate an
     *                     initiateEscrow transaction.
     * @return
     */
    public Escrow initiateEscrow(final EscrowInputs escrowInputs) {
        Objects.requireNonNull(escrowInputs);

        // TODO: Make this operation atomic.  If either fails, the initiateEscrow would be corrupted!

        // 1. Debit the sender's account
        ledgerAccountManager.debitAccount(escrowInputs.getSourceAddress(), escrowInputs.getAmount());
        // 2. Credit the initiateEscrow account for the sourceAccountId, and put money in there for holding...
        ledgerAccountManager.creditAccount(this.escrowAccountAddress, escrowInputs.getAmount());
        // 3. Add the initiateEscrow to the map for later storage.
        final Escrow escrow = new Escrow(escrowInputs, escrowAccountAddress);
        this.escrows.put(escrowInputs.getInterledgerPacketHeader().getIlpTransactionId(), escrow);
        return escrow;
    }

    /**
     * For a given pending escrow transaction identified by {@code ilpTransactionId}, execute the escrow by crediting
     * {@code amount} to the account identified by {@link Escrow#getLocalDestinationAddress()} and debiting an identical
     * amount from this ledger's escrow holding account.
     *
     * @param ilpTransactionId An instance of {@link IlpTransactionId} that identifies the pending escrow transaction.
     * @return
     * @throws EscrowException if the escrow execution failed for any reason.
     */
    public Escrow executeEscrow(final IlpTransactionId ilpTransactionId) {
        Objects.requireNonNull(ilpTransactionId);

        // TODO: Make this operation atomic.  If either fails, the initiateEscrow would be corrupted!

        return Optional.ofNullable(this.escrows.get(ilpTransactionId))
                .map(escrow -> {
                    // 1. Debit the sender's account
                    ledgerAccountManager.debitAccount(
                            this.escrowAccountAddress,
                            escrow.getAmount()
                    );
                    // 2. Credit the initiateEscrow account.
                    ledgerAccountManager.creditAccount(escrow.getLocalDestinationAddress(), escrow.getAmount());
                    return this.escrows.remove(escrow.getIlpPacketHeader().getIlpTransactionId());
                })
                .orElseThrow(() -> new EscrowException("No escrow existed for ILPTransaction: " + ilpTransactionId));
    }

    /**
     * For a given pending escrow transaction identified by {@code ilpTransactionId}, reverse the escrow by crediting
     * {@code amount} to the account identified by {@link Escrow#getLocalSourceAddress()} and debiting an identical
     * amount from this ledger's escrow holding account.
     *
     * @param ilpTransactionId An instance of {@link IlpTransactionId} that identifies the pending escrow transaction.
     * @return
     * @throws EscrowException if the escrow execution failed for any reason.
     */
    public Escrow reverseEscrow(final IlpTransactionId ilpTransactionId) {
        Objects.requireNonNull(ilpTransactionId);

        // TODO: Make this operation atomic.  If either fails, the initiateEscrow would be corrupted!

        return Optional.ofNullable(this.escrows.get(ilpTransactionId))
                .map(escrow -> {
                    // 1. Debit the sender's account
                    ledgerAccountManager.debitAccount(
                            this.escrowAccountAddress,
                            escrow.getAmount()
                    );
                    // 2. Credit the initiateEscrow account.
                    ledgerAccountManager.creditAccount(escrow.getLocalSourceAddress(), escrow.getAmount());
                    return this.escrows.remove(escrow.getIlpPacketHeader().getIlpTransactionId());
                })
                .orElseThrow(() -> new EscrowException("No escrow existed for ILPTransaction: " + ilpTransactionId));
    }
}
