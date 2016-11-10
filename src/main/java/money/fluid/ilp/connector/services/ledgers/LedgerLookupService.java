package money.fluid.ilp.connector.services.ledgers;


import com.sappenin.utils.StringId;
import money.fluid.ilp.connector.model.Account;
import org.interledgerx.ilp.core.Ledger;

import java.util.Optional;

/**
 * A service for looking up information about a ledger.
 */
public interface LedgerLookupService {

    /**
     * Given a ledger identifier that is controlled by the owner of this connector, determine the account that should be
     * used.  This mechanism allows the connector to know about the other connectors that it can work with in order to
     * provide quoting and transfer services.
     *
     * @param ledgerId
     * @return
     */
    Optional<Account> getAccountForLedger(final StringId<Ledger> ledgerId);

}
