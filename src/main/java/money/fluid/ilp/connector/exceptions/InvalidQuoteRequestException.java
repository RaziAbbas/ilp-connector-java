package money.fluid.ilp.connector.exceptions;

import lombok.NoArgsConstructor;

/**
 * An extension of {@link RuntimeException} that is thrown when a connector, or something the connector is operating
 * upon, does not have sufficient funds to complete the operation.
 */
@NoArgsConstructor
public class InvalidQuoteRequestException extends RuntimeException {

    public InvalidQuoteRequestException(String message) {
        super(message);
    }

    public InvalidQuoteRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidQuoteRequestException(Throwable cause) {
        super(cause);
    }

    public InvalidQuoteRequestException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
