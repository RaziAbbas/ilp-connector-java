package money.fluid.ilp.connector.services;

import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * A service for calculating a commission (in a particular currency) for a given ILP transfer.  This service is
 * necessary because certain ILP transactions may be transferring non-monetary assets, but the commission should be
 * quoted in a particular currency.
 */
public interface ConnectorFeeService {

    /**
     * Calculate a commission amount for the specified source {@link QuoteRequest}.  This method computes the commission
     * as a {@link BigDecimal} as opposed to a monetery amount in order to support non-currency asset types.
     *
     * @param assetId       A {@link AssetId} representing the asset to compute a commission on.
     * @param orginalAmount A {@link BigDecimal} representing the original amount of a transfer.
     * @return
     */
    ConnectorFeeInfo calculateConnectorFee(AssetId assetId, BigDecimal orginalAmount);

    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    class ConnectorFeeInfo {
        // The fee charged by a connector.
        private final BigDecimal fee;

        private final AssetId feeAssetId;

        // The original amount, before fees.
        private final BigDecimal originalAmount;

        // The amount, after fees.
        private final BigDecimal amountAfterFee;
    }
}
