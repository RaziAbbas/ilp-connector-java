package money.fluid.ilp.connector.model.ids;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An implementation of {@link StringId} for representing Ledger Accounts.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConnectorId extends StringId<ConnectorId> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public ConnectorId(final String id) {
        super(id);
    }

    /**
     * Helper method to create an instance of {@link ConnectorId}.
     *
     * @param id
     * @return
     */
    public static ConnectorId of(final String id) {
        return new ConnectorId(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
