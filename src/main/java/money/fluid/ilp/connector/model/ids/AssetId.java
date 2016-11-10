package money.fluid.ilp.connector.model.ids;

import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;

/**
 * An implementation of {@link StringId} for representing Assets.
 */
@EqualsAndHashCode(callSuper = true)
public class AssetId extends StringId<AssetId> {

    /**
     * Required-args Constructor.
     *
     * @param id
     */
    public AssetId(final String id) {
        super(id);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
