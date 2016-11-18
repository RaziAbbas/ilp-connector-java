package money.fluid.ilp.ledger.inmemory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import lombok.Getter;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.EscrowManager;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.inmemory.exceptions.EscrowException;
import money.fluid.ilp.ledger.inmemory.model.Escrow;
import money.fluid.ilp.ledger.inmemory.model.EscrowInputs;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * An in-memory implementation of {@link EscrowManager} that tracks Escrow without any sort of data persistence
 * (meaning, all escrows go away when the runtime process terminates).
 * <p>
 * This implementation allows for only a single-escrow account per ledger.  However, more complicated implementations
 * might allow for a more advanced mapping between escrow source-accounts, escrow accounts, and escrow destination
 * accounts.
 * <p>
 * WARNING: This implementation should not be used in a production environment since it does NOT utilize a
 * persistent datastore to store escrow information.
 */
@Getter
public class InMemoryEscrowManager implements EscrowManager, RemovalListener<IlpTransactionId, Escrow>, EscrowExpirationHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO A proper ledger will want to track the various states of the initiateEscrow for auditing.

    private final LedgerInfo ledgerInfo;

    // The main ledger to move funds around in...
    private final LedgerAccountManager ledgerAccountManager;

    // The ILP account to add and remove escrow from...
    private final IlpAddress escrowAccountAddress;

    // Indicates any amounts that are currently in initiateEscrow from a given source ledger account.
    //private final Map<IlpTransactionId, Escrow> escrows = new ConcurrentHashMap();
    private Cache<IlpTransactionId, Escrow> escrows;
    private volatile EscrowExpirationHandler escrowExpirationHandler;

    public InMemoryEscrowManager(
            final LedgerInfo ledgerInfo,
            final LedgerAccountId escrowAccountId,
            final LedgerAccountManager ledgerAccountManager,
            final CacheBuilder<Object, Object> escrowCacheBuilder
    ) {
        this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
        this.ledgerAccountManager = Objects.requireNonNull(ledgerAccountManager);

        // __escrow__ account!
        this.escrowAccountAddress = IlpAddress.of(escrowAccountId, ledgerInfo.getLedgerId());

        this.escrows = Objects.requireNonNull(escrowCacheBuilder)
                .removalListener(this)
                .build();
        this.escrowExpirationHandler = this;
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

        // WARNING: This operation is notatomic.  If either fails, the initiateEscrow would be corrupted!

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

        // WARNING: This operation is notatomic.  If either fails, the initiateEscrow would be corrupted!

        return Optional.ofNullable(this.escrows.getIfPresent(ilpTransactionId))
                .map(escrow -> {
                    // 1. Debit the sender's account
                    ledgerAccountManager.debitAccount(
                            this.escrowAccountAddress,
                            escrow.getAmount()
                    );
                    // 2. Credit the initiateEscrow account.
                    ledgerAccountManager.creditAccount(escrow.getLocalDestinationAddress(), escrow.getAmount());

                    this.escrows.invalidate(escrow.getIlpPacketHeader().getIlpTransactionId());
                    return escrow;
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

        // WARNING: This operation is notatomic.  If either fails, the initiateEscrow would be corrupted!

        return Optional.ofNullable(this.escrows.getIfPresent(ilpTransactionId))
                .map(escrow -> {
                    // 1. Debit the sender's account
                    ledgerAccountManager.debitAccount(
                            this.escrowAccountAddress,
                            escrow.getAmount()
                    );
                    // 2. Credit the initiateEscrow account.
                    ledgerAccountManager.creditAccount(escrow.getLocalSourceAddress(), escrow.getAmount());

                    this.escrows.invalidate(escrow.getIlpPacketHeader().getIlpTransactionId());
                    return escrow;
                })
                .orElseThrow(() -> new EscrowException("No escrow existed for ILPTransaction: " + ilpTransactionId));
    }

    // Not part of the EscrowManager interface because this only connects the Guava Cache to the EscrowManager.
    public void setEscrowExpirationHandler(final EscrowExpirationHandler escrowExpirationHandler) {
        this.escrowExpirationHandler = Objects.requireNonNull(escrowExpirationHandler);
    }

    // A default implementation.  Constructors should initialize this to something useful, if desired.
    @Override
    public void onEscrowTimedOut(final Escrow timedOutEscrow) {
        logger.warn("No escrow timeout handler assigned to {}", this);
    }

    /**
     * This method will be called by the Guava Cache whenever an entry is evicted.  Since the Guava Cache is merely
     * an implementation detail of this {@link EscrowManager} implementation, this method merely connects
     * the cache to the {@link EscrowExpirationHandler}.
     *
     * @param notification
     */
    @Override
    public void onRemoval(final RemovalNotification<IlpTransactionId, Escrow> notification) {
        if (notification.getCause().equals(RemovalCause.EXPIRED)) {
            logger.info("Ledger {} escrow timed out : {}", this.getLedgerInfo().getLedgerId(), notification);
            this.escrowExpirationHandler.onEscrowTimedOut(notification.getValue());
        } else if (notification.getCause().equals(RemovalCause.EXPLICIT)) {
            logger.info("Ledger {} escrow explicitely removed : {}", this.getLedgerInfo().getLedgerId(), notification);
        } else if (notification.getCause().equals(RemovalCause.SIZE)) {
            logger.info("Ledger {} Escrow removed due to SIZE: {}", notification);
        } else {
            throw new RuntimeException("Unhandled cache eviction: " + notification);
        }
    }

    @Override
    public String toString() {
        return this.getLedgerInfo().getLedgerId().getId();
    }
}
