package money.fluid.ilp.connector.services;


import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

import java.util.Optional;

/**
 * A service for looking up information about a ledger.
 */
public interface ConnectedLedgerService {

    /**
     * Given a ledger identifier that can be orchestrated by this connector, determine the account that should be used
     * for holding escrowable assets for ultimate transfer. This account is isolated from the connector fees account for
     * proper accounting, and to avoid the connector from tapping into escrow assets to fund the operation of the
     * connector.
     *
     * @param ledgerId
     * @return
     */
    Optional<LedgerAccountId> getEscrowAccountIdForLedger(LedgerId ledgerId);

    /**
     * Given a ledger identifier that can be orchestrated by this connector, determine the account that should be used
     * for collecting connector fees.  This account is isolated from the escrow account for proper accounting, and to
     * avoid the connector from tapping into escrow assets to fund the operation of the connector.
     *
     * @param ledgerId
     * @return
     */
    Optional<LedgerAccountId> getConnectorFeeAccountIdForLedger(LedgerId ledgerId);

    /**
     * Get the default expiration for the supplied {@link LedgerId}.
     *
     * @return
     */
    long getDefaultExpiration(LedgerId ledgerId);
}
