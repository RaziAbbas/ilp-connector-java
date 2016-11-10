package org.interledgerx.ilp.core.events;

import org.interledgerx.ilp.core.LedgerInfo;

public class LedgerErrorEvent extends LedgerEvent {

    private static final long serialVersionUID = -6494295568908151670L;
    protected Exception error;

    public LedgerErrorEvent(LedgerInfo source, Exception error) {
        super(source);

        this.error = error;

    }

    public Exception getError() {
        return error;
    }

}
