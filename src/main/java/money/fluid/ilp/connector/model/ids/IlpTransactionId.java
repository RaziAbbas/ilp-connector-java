package money.fluid.ilp.connector.model.ids;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An implementation of {@link StringId} for representing ILP transactions.
 * <p>
 * TODO: See comments in JS code.  This might need to be consistently generatable from some inputs + secret, but not
 * easily guessable?
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
// TODO: Consider renaming this to IlpPaymentId, since an ILP payment consists of a collection of ILP transfers.
public class IlpTransactionId extends StringId<IlpTransactionId> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public IlpTransactionId(final String id) {
        super(id);
    }

    /**
     * Helper method to create an instance of {@link IlpTransactionId}.
     *
     * @param id
     * @return
     */
    public static IlpTransactionId of(final String id) {
        return new IlpTransactionId(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
