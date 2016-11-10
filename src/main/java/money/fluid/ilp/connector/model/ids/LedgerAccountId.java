package money.fluid.ilp.connector.model.ids;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;

/**
 * An implementation of {@link StringId} for representing Ledger Accounts.
 */
@EqualsAndHashCode(callSuper = true)
// TODO: Consider splitting this into a LocalLedgerAccountId and a RemoteLedgerAccountId?  Or just always use FQ ledger id?
public class LedgerAccountId extends StringId<LedgerAccountId> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public LedgerAccountId(final String id) {
        super(id);
    }

    /**
     * Helper method to create an instance of {@link StringId}.
     *
     * @param id
     * @return
     */
    public static LedgerAccountId of(final String id) {
        return new LedgerAccountId(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
