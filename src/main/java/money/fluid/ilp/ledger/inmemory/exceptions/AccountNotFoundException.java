package money.fluid.ilp.ledger.inmemory.exceptions;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.exceptions.InterledgerException;

/**
 * @author mrmx
 */
public class AccountNotFoundException extends InterledgerException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of {@code AccountNotFoundException} without
     * detail message.
     */
    public AccountNotFoundException() {
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param msg the detail message.
     * @deprecated This will go away once we create an ILPIdentifier type.
     */
    @Deprecated
    public AccountNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AccountNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param ledgerAccountId the detail message.
     */
    public AccountNotFoundException(final LedgerAccountId ledgerAccountId) {
        super("LedgerAccountId: " + ledgerAccountId.getId());
    }
}
