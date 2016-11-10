package money.fluid.ilp.ledger.model;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;

/**
 * An implementation of {@link StringId} for representing Ledgers.
 */
@EqualsAndHashCode(callSuper = true)
public class LedgerId extends StringId<LedgerId> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public LedgerId(final String id) {
        super(id);
    }

    /**
     * Helper method to create an instance of {@link StringId}.
     *
     * @param id
     * @return
     */
    public static LedgerId of(final String id) {
        return new LedgerId(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
