package money.fluid.ilp.connector.services;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.quotes.QuoteRequest;

import javax.money.MonetaryAmount;

/**
 * A service for calculating a commission (in a particular currency) for a given ILP transfer.  This service is
 * necessary because certain ILP transactions may be transferring non-monetary assets, but the commission should be
 * quoted in a particular currency.
 */
public interface ConnectorFeeService {

    /**
     * Calculate a commission amount for the specified source {@link QuoteRequest}.
     *
     * @param originalAmount A {@link MonetaryAmount} representing the original amount of a transfer to calculate a fee
     *                       for.
     * @return
     */
    ConnectorFeeInfo calculateConnectorFee(MonetaryAmount originalAmount);

    @Getter
    @RequiredArgsConstructor
    @Builder
    @EqualsAndHashCode
    @ToString
    class ConnectorFeeInfo {
        // The original amount, before fees.
        private final MonetaryAmount originalAmount = null;

        // The amount, after fees.
        private final MonetaryAmount amountAfterFee = null;
    }
}
