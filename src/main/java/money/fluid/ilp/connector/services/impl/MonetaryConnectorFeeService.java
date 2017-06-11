package money.fluid.ilp.connector.services.impl;

import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.connector.services.ConnectorFeeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An implementation of {@link ConnectorFeeService} that computes a monetary commission based upon supplied ILP transfer
 * request inputs.
 */
@Service
public class MonetaryConnectorFeeService { // implements ConnectorFeeService {

//    //private final Map<AssetId, BigDecimal> COMMISSION_RATES;
//
//    private static final BigDecimal DEFAULT_COMMISSION_RATE = BigDecimal.ZERO;
//
//    // Sand is very in-expensive, but it's the reference currency.
//    public static final BigDecimal EUR_COMMISSION = new BigDecimal(".025");
//
//    // Sand is very expensive...
//    public static final BigDecimal USD_COMMISSION = new BigDecimal(".01");
//
//    // Sand is very expensive...
//    //public static final BigDecimal JPY_COMMISSION = new BigDecimal(".015");
//
//    /**
//     * No-args Constructor.
//     */
//    public MonetaryConnectorFeeService() {
//
//        // TODO: FIXME with javax.money!
//
////        COMMISSION_RATES = ImmutableMap.of(
////                DefaultSupportedAssetService.SAND_GRAIN_CURRENCY, DEFAULT_COMMISSION_RATE,
////                DefaultSupportedAssetService.EUR_CURRENCY, EUR_COMMISSION,
////                DefaultSupportedAssetService.USD_CURRENCY, USD_COMMISSION
////                //DefaultSupportedAssetService.JPY_CURRENCY, JPY_COMMISSION
////        );
//    }
//
//    @Override
//    public ConnectorFeeInfo calculateConnectorFee(final AssetId assetId, final BigDecimal originalAmount) {
//        Objects.requireNonNull(assetId);
//        Objects.requireNonNull(originalAmount);
//
//        // TODO: When processing a reversal, this fee must not be positive.  Consider how this will work.
//
//        // TODO: FIXME!
//        final BigDecimal rate = null; //COMMISSION_RATES.get(assetId);
//
//        // Do this to avoid the 0e-10 of BigDecimal.ZERO
//        final BigDecimal fee;
//        if (BigDecimal.ZERO.equals(rate)) {
//            fee = BigDecimal.ZERO;
//        } else {
//            fee = originalAmount.multiply(rate).abs();
//        }
//        final BigDecimal amountAfterFee = originalAmount.subtract(fee);
//
//        return ConnectorFeeInfo.builder()
//                .originalAmount(originalAmount)
//                .fee(fee)
//                .feeAssetId(assetId)
//                .amountAfterFee(amountAfterFee)
//                .build();
//    }
}
