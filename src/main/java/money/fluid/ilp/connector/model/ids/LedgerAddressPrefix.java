package money.fluid.ilp.connector.model.ids;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;

/**
 * A {@link StringId} for representing a ledger prefix.  For example, a ledger "us.fed.bofa" might have a ledger prefix
 * of "us" or "us.fed".
 */
@EqualsAndHashCode(callSuper = true)
public class LedgerAddressPrefix extends StringId<LedgerAddressPrefix> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public LedgerAddressPrefix(final String id) {
        super(id);
    }

    /**
     * Helper method to create an instance of {@link StringId}.
     *
     * @param id
     * @return
     */
    public static LedgerAddressPrefix of(final String id) {
        return new LedgerAddressPrefix(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
