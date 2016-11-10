package org.interledgerx.ilp.core.events;

import lombok.Getter;
import org.interledgerx.ilp.core.LedgerInfo;

@Getter
public class LedgerConnectedEvent extends LedgerEvent {

    private static final long serialVersionUID = 6501842605798174441L;

    public LedgerConnectedEvent(LedgerInfo source) {
        super(source);
    }
}
