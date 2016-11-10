package money.fluid.ilp.ledger.inmemory.exceptions;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.exceptions.InterledgerException;

import java.util.Optional;

/**
 * An exception thrown when a ledger operation is attempted on an account that cannot exist on a given ledger.  This is
 * distinct from operations trying to perform on accounts that merely don't exist, for which methods may return an
 * {@link AccountNotFoundException} or an {@link Optional#empty()}.
 */
public class InvalidAccountException extends InterledgerException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of {@code AccountNotFoundException} without
     * detail message.
     */
    public InvalidAccountException() {
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param msg the detail message.
     * @deprecated This will go away once we create an ILPIdentifier type.
     */
    @Deprecated
    public InvalidAccountException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidAccountException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs an instance of {@code AccountNotFoundException} with the
     * specified detail message.
     *
     * @param ledgerAccountId the detail message.
     */
    public InvalidAccountException(final LedgerAccountId ledgerAccountId) {
        super("LedgerAccountId: " + ledgerAccountId.getId());
    }
}
