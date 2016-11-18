package money.fluid.ilp.ledger.inmemory;

import money.fluid.ilp.ledger.inmemory.model.Escrow;

/**
 * An internal-use-only interface that connects Guava cache timeouts to this EscrowManager.  Only really valid for this
 * implementation.
 * <p>
 * NOTE: This interface is purposefully not part of EscrowManager because this interface only exists for the in-memory
 * Ledger impl.
 */
interface EscrowExpirationHandler {
    /**
     * Called when an instance of {@link Escrow} has timed-out and is no longer valid.
     *
     * @param expiredEscrow
     */
    void onEscrowTimedOut(final Escrow expiredEscrow);
}
