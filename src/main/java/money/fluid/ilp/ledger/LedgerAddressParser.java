package money.fluid.ilp.ledger;

import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.exceptions.InterledgerException;

public interface LedgerAddressParser {

    /**
     * Parse a {@link String} to construct an instance of {@link IlpAddress}.
     *
     * @param address
     * @return
     * @throws LedgerAddressParserException
     */
    IlpAddress parse(String address) throws LedgerAddressParserException;

    class LedgerAddressParserException extends InterledgerException {
        /**
         * Constructs an instance of <code>LedgerAddressParserException</code> with
         * the specified detail message.
         *
         * @param msg the detail message.
         */
        public LedgerAddressParserException(String msg) {
            super(msg);
        }

        /**
         * Constructs an instance of <code>LedgerAddressParserException</code> with
         * the specified detail message and <code>Throwable</code> cause.
         *
         * @param msg   the detail message.
         * @param cause the <code>Throwable</code> cause.
         */
        public LedgerAddressParserException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
