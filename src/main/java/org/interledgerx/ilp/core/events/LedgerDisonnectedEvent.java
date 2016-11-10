package org.interledgerx.ilp.core.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.interledgerx.ilp.core.LedgerInfo;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LedgerDisonnectedEvent extends LedgerEvent {

    private static final long serialVersionUID = -2688034526014826323L;

    public LedgerDisonnectedEvent(final LedgerInfo source) {
        super(source);
    }

}
