package money.fluid.ilp.connector.services.impl;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.connector.services.ConnectedLedgerService;
import money.fluid.ilp.connector.services.WhoAmIService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

/**
 * An implementation of {@link ConnectedLedgerService} that simulates 3 connected ledgers: RED, BLUE, and Sappenin's
 * SAND ledger.
 *
 * @deprecated When we have the concept of a Ledger, remotely connect to each ledger's config endpoint to determine the
 * actual assetId for a given ledger.  Also, allow the escrow and connector fee accounts to be set via configuration.
 */
@Service
@Deprecated
public class SimulatedConnectedLedgerService implements ConnectedLedgerService {

    private final WhoAmIService whoAmIService;

    /**
     * Required-args Constructor.
     *
     * @param whoAmIService An instance of {@link WhoAmIService}.
     */
    @Inject
    public SimulatedConnectedLedgerService(final WhoAmIService whoAmIService) {
        this.whoAmIService = Objects.requireNonNull(whoAmIService);
    }

    // TODO: Create a new implementation that backs this information by a proper datastore, or other mechanism for up-to-date data.
    @Override
    public Optional<LedgerAccountId> getEscrowAccountIdForLedger(final LedgerId ledgerId) {
        return Optional.of(new LedgerAccountId("sappenin-escrow"));
    }

    @Override
    public Optional<LedgerAccountId> getConnectorFeeAccountIdForLedger(LedgerId ledgerId) {
        return Optional.of(new LedgerAccountId("connector-fees"));
    }

    @Override
    public long getDefaultExpiration(LedgerId ledgerId) {
        // TODO: Connect to the ledger by its id, and cache the default expiration ms.
        return 5000;
    }
}
