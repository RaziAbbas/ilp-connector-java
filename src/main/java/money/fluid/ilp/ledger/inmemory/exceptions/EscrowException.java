package money.fluid.ilp.ledger.inmemory.exceptions;

import lombok.NoArgsConstructor;

/**
 * An extension of {@link RuntimeException} that is thrown when an escrow operation is unable to be completed.
 */
@NoArgsConstructor
public class EscrowException extends RuntimeException {

    public EscrowException(String message) {
        super(message);
    }

    public EscrowException(String message, Throwable cause) {
        super(message, cause);
    }

    public EscrowException(Throwable cause) {
        super(cause);
    }

    public EscrowException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
