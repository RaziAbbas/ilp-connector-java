package money.fluid.ilp.connector.services.quoting.meta;

import com.sappenin.utils.StringId;
import org.interledgerx.ilp.core.Ledger;

/**
 * A meta service for determining information about "this" connector.
 */
public class MeService {

    public StringId<Ledger> getLedgerId() {
        // TODO: Connect this to a property or env variable.
        return new StringId<>("cool ledger");
    }

    /**
     * Get the number of milliseconds to elapse before expiring a transfer.
     *
     * @return
     */
    public long getDefaultExpiration() {
        return 5000;
    }


}
