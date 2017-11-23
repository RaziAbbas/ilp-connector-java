package money.fluid.ilp.ledger.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.MapMaker;
import lombok.Getter;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.EscrowManager;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.inmemory.exceptions.EscrowException;
import money.fluid.ilp.ledger.inmemory.model.Escrow;
import money.fluid.ilp.ledger.inmemory.model.Escrow.Status;
import money.fluid.ilp.ledger.inmemory.model.EscrowInputs;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerInfo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

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
    private final org.interledgerx.ilp.core.IlpAddress escrowAccountAddress;

    // Indicates any amounts that are currently in initiateEscrow from a given source ledger account.  Allows them to be
    // expired without actuall losing them in the actual backing store.  This setup is just an implementation detail.
    // In reality, all escrows should be backed by a persistent datastore since losing them would be catastrophic.
    private final ConcurrentMap<IlpTransactionId, Escrow> escrows = new MapMaker().initialCapacity(
            50).makeMap();
    private volatile EscrowExpirationHandler escrowExpirationHandler;

    /**
     * Required-args Constructor.
     *
     * @param ledgerInfo
     * @param escrowAccountId
     * @param ledgerAccountManager
     */
    public InMemoryEscrowManager(
            final LedgerInfo ledgerInfo,
            final LedgerAccountId escrowAccountId,
            final LedgerAccountManager ledgerAccountManager
    ) {
        this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
        this.ledgerAccountManager = Objects.requireNonNull(ledgerAccountManager);

        // __escrow__ account!
        this.escrowAccountAddress = IlpAddress.of(escrowAccountId, ledgerInfo.getLedgerId());
    }
    
    public LedgerInfo getLedgerInfo() {
		return ledgerInfo;
	}

	public LedgerAccountManager getLedgerAccountManager() {
		return ledgerAccountManager;
	}

	public org.interledgerx.ilp.core.IlpAddress getEscrowAccountAddress() {
		return escrowAccountAddress;
	}

	public ConcurrentMap<IlpTransactionId, Escrow> getEscrows() {
		return escrows;
	}

	public EscrowExpirationHandler getEscrowExpirationHandler() {
		return escrowExpirationHandler;
	}


    /**
     * Create an initiateEscrow transaction by debiting an {@code amount} of the associated ledger's asset from  {@code
     * {@link EscrowInputs#getLocalSourceAddress()} and crediting the same amount into the initiateEscrow account for
     * the associated ledger.
     *
     * @param escrowInputs An instance of {@link EscrowInputs} with all information required to initiate an
     *                     initiateEscrow transaction.
     * @return
     */
    public Escrow initiateEscrow(final EscrowInputs escrowInputs) {
        Objects.requireNonNull(escrowInputs);

        // WARNING: This operation is notatomic.  If either fails, the initiateEscrow would be corrupted!
        // Debit the sender's account and Credit the initiateEscrow account for the sourceAccountId, and put money in
        // there for holding...
        ledgerAccountManager.transfer(
                escrowInputs.getLocalSourceAddress(),
                this.escrowAccountAddress,
                escrowInputs.getAmount()
        );

        // Add the initiateEscrow to the map for later storage.
        final Escrow escrow = new Escrow(escrowInputs, escrowAccountAddress);
        this.escrows.put(escrowInputs.getInterledgerPacketHeader().getIlpTransactionId(), escrow);
        return escrow;
    }

    @Override
    public Optional<Escrow> getEscrow(IlpTransactionId ilpTransactionId) {
        Objects.requireNonNull(ilpTransactionId);
        return Optional.ofNullable(this.escrows.get(ilpTransactionId));
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

        return Optional.ofNullable(this.escrows.get(ilpTransactionId))
                .map(escrow -> {
                    // Transfer from the Escrow into the destination account.
                    ledgerAccountManager.transfer(
                            this.escrowAccountAddress,
                            escrow.getLocalDestinationAddress(),
                            escrow.getAmount()
                    );

                    // Update the Escrow Status...
                    final Escrow executedEscrow = new Escrow(escrow, Status.EXECUTED);
                    this.escrows.put(ilpTransactionId, new Escrow(escrow, Status.EXECUTED));
                    return executedEscrow;
                })
                .orElseThrow(
                        () -> new EscrowException("No escrow existed for ILPTransaction: " + ilpTransactionId));
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

        return Optional.ofNullable(this.escrows.get(ilpTransactionId))
                .map(escrow -> {

                    Preconditions.checkArgument(
                            escrow.getInterledgerPacketHeader().isOptimisticModeHeader(),
                            "Only OptimisticMode escrows can be reversed!"
                    );

                    // Transfer from the Escrow into the destination account.
                    ledgerAccountManager.transfer(
                            this.escrowAccountAddress,
                            escrow.getLocalSourceAddress(),
                            escrow.getAmount()
                    );

                    // Update the Escrow Status...
                    final Escrow executedEscrow = new Escrow(escrow, Status.EXECUTED);
                    this.escrows.put(ilpTransactionId, new Escrow(escrow, Status.EXECUTED));
                    return executedEscrow;
                })
                .orElseThrow(
                        () -> new EscrowException("No escrow existed for ILPTransaction: " + ilpTransactionId));
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
            logger.info("Ledger {} escrow explicitly removed : {}", this.getLedgerInfo().getLedgerId(), notification);
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

    /**
     * Implementation-only method to provide the test-harness a hook to reverse any expired escrows.  This method is not
     * part of the formatl {@link EscrowManager} interface because it's only useful in the test harness.  A real escrow
     * manager would have its own expiration functionality.
     */
    public void processExpiredEscrows() {
        this.escrows.values().stream()
                .filter(escrow -> escrow.getOptExpiry().isPresent())
                .filter(escrow -> {
                    final DateTime now = DateTime.now(DateTimeZone.UTC);
                    return escrow.getOptExpiry().get().isAfter(now);
                })
                .forEach(escrow -> this.reverseEscrow(escrow.getInterledgerPacketHeader().getIlpTransactionId()));
    }
}