package org.interledgerx.ilp.core.events;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.ToString;
import org.interledgerx.ilp.core.LedgerInfo;

import java.util.EventObject;

/**
 * Base for all events emitted by a ledger
 */
@Getter
public abstract class LedgerEvent extends EventObject {

    private static final long serialVersionUID = 3292998781708775780L;

    public LedgerEvent(final LedgerInfo source) {
        super(source);
    }

    public LedgerInfo getLedgerInfo() {
        return (LedgerInfo) getSource();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
