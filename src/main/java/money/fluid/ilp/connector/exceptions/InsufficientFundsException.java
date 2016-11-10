package money.fluid.ilp.connector.exceptions;

import lombok.NoArgsConstructor;

/**
 * An extension of {@link RuntimeException} that is thrown when a connector, or something the connector is operating
 * upon, does not have sufficient funds to complete the operation.
 */
@NoArgsConstructor
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientFundsException(Throwable cause) {
        super(cause);
    }

    public InsufficientFundsException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
