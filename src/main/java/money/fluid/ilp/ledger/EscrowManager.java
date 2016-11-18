package money.fluid.ilp.ledger;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.inmemory.exceptions.EscrowException;
import money.fluid.ilp.ledger.inmemory.model.Escrow;
import money.fluid.ilp.ledger.inmemory.model.EscrowInputs;

/**
 * A service that manages escrow for a Ledger.  A ledger will hold some assets in escrow that, when executed, will be
 * credited to the target account for an escrow.  Conversely, if an escrow is reversed, then the assets will be credited
 * back to the source account for that escrow.
 */
public interface EscrowManager {
    /**
     * Create an initiateEscrow transaction by debiting an {@code amount} of the associated ledger's asset from  {@code
     * {@link EscrowInputs#getSourceAddress()} and crediting the same amount into the initiateEscrow account for the
     * associated ledger.
     *
     * @param escrowInputs An instance of {@link EscrowInputs} with all information required to initiate an
     *                     initiateEscrow transaction.
     * @return
     */
    Escrow initiateEscrow(final EscrowInputs escrowInputs);

    /**
     * For a given pending escrow transaction identified by {@code ilpTransactionId}, execute the escrow by crediting
     * {@code amount} to the account identified by {@link Escrow#getLocalDestinationAddress()} and debiting an identical
     * amount from this ledger's escrow holding account.
     *
     * @param ilpTransactionId An instance of {@link IlpTransactionId} that identifies the pending escrow transaction.
     * @return
     * @throws EscrowException if the escrow execution failed for any reason.
     */
    Escrow executeEscrow(final IlpTransactionId ilpTransactionId);

    /**
     * For a given pending escrow transaction identified by {@code ilpTransactionId}, reverse the escrow by crediting
     * {@code amount} to the account identified by {@link Escrow#getLocalSourceAddress()} and debiting an identical
     * amount from this ledger's escrow holding account.
     *
     * @param ilpTransactionId An instance of {@link IlpTransactionId} that identifies the pending escrow transaction.
     * @return
     * @throws EscrowException if the escrow execution failed for any reason.
     */
    Escrow reverseEscrow(final IlpTransactionId ilpTransactionId);
}
