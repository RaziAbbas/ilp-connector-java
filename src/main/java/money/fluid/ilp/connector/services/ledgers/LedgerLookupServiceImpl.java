package money.fluid.ilp.connector.services.ledgers;

import com.sappenin.utils.StringId;
import money.fluid.ilp.connector.model.Account;
import org.interledgerx.ilp.core.Ledger;

import java.util.Optional;

/**
 * A default implementation of {@link LedgerLookupService}.
 */
public class LedgerLookupServiceImpl implements LedgerLookupService {

    // TODO: Create a new implementation that backs this information by a proper datastore, or other mechanism for up-to-date data.
    @Override
    public Optional<Account> getAccountForLedger(final StringId<Ledger> ledgerId) {

        // TODO:FIXME!
        return null;
        //return Optional.of(new Account.Builder().withAccountId(new StringId<>("sappenin")).build());
    }
}
