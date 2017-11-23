package money.fluid.ilp.connector.services;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.convert.ExchangeRateProvider;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A service for calculating current exchange rates between various asset types that are supported by this Connector.
 */
public interface ExchangeRateService {

    /**
     * Computes the current exchange rate between the specified instances of {@link CurrencyUnit}.
     *
     * @param sourceCurrencyUnit
     * @param destinationCurrencyUnit
     * @return
     */
    // TODO: Consider removing this, or else implement it
    //ExchangeRateInfo getExchangeRate(CurrencyUnit sourceCurrencyUnit, CurrencyUnit destinationCurrencyUnit);

    /**
     * Computes the current exchange rate between the specified instances of {@link CurrencyUnit}.
     *
     * @param sourceAmount            The {@link MonetaryAmount} to convert.
     * @param destinationCurrencyUnit The {@link CurrencyUnit} to convert the {@code sourceAmount} to.
     * @return
     */
    ExchangeRateInfo getExchangeRate(MonetaryAmount sourceAmount, CurrencyUnit destinationCurrencyUnit);


    /**
     * A default implementation of {@link ExchangeRateService} that returns statically defined exchange rate information
     * as compared to a hypothetical currency called "sand" which is transacted in grains of actual sand.
     *
     * @deprecated This class exists only for testing purposes, and will go away once a real exchange rate service is
     * created.
     */
    @Service
    @Deprecated
    class SandStaticExchangeRateService implements ExchangeRateService {

        private final ExchangeRateProvider exchangeRateProvider;


        // Sand is very in-expensive, but it's the reference currency.
        public static final BigDecimal SDC_TO_EUR = new BigDecimal("0.90821572");

        // Sand is very expensive...
        public static final BigDecimal SDC_TO_USD = new BigDecimal("1.000");

        // Sand is very expensive...
        public static final BigDecimal SDC_TO_JPY = new BigDecimal("101.68");


        //private final Map<AssetId, BigDecimal> SAND_EXCHANGE_RATES;

        /**
         * No-args Constructor.
         *
         * @param exchangeRateProvider
         */
        public SandStaticExchangeRateService(final ExchangeRateProvider exchangeRateProvider) {
            this.exchangeRateProvider = Objects.requireNonNull(exchangeRateProvider);

//            this.SAND_EXCHANGE_RATES = ImmutableMap.of(
//                    DefaultSupportedAssetService.SAND_GRAIN_CURRENCY, BigDecimal.ONE,
//                    DefaultSupportedAssetService.EUR_CURRENCY, SDC_TO_EUR,
//                    DefaultSupportedAssetService.USD_CURRENCY, SDC_TO_USD
//                    //DefaultSupportedAssetService.JPY_CURRENCY, SDC_TO_JPY
//            );
        }

        /**
         * Computes an exchange rate by first converting to the reference asset (SDC) and then to the destination
         * asset.
         * <p>
         * Sample Rates
         * <p>
         * SAND/RED ==> 1 : 1.0950 (1 Sand dollar costs 1.0950 RED for a rate of 0.913)
         * <p>
         * RED/SAND ==> (1 RED costs 0.913 SAND for a rate of 1.0950)
         * <p>
         * SAND/BLUE ==> 1 : 2 (1 Sand dollar costs 2 BLUE for a rate of 0.5000)
         * <p>
         * BLUE/SAND ==> 2 : 1 (1 BLUE costs 0.5 SAND for a rate of 2.0000)
         * <p>
         * To convert from RED to BLUE using our reference currency (SAND), first convert the RED currency to SAND,
         * which will yield 0.913 SAND for each RED.  Next, we convert our 0.913 SAND to blue, at a rate of 1:2, or
         * 1.8260 BLUE.
         **/
        // @Override
        // public ExchangeRateInfo getExchangeRate(final AssetId sourceAssetId, final AssetId destinationAssetId) {

        // TODO: FIXME or replace with javax.money variant!

//            //final AssetId ass
//            // 1.) Convert the source asset into SDC, unless it's already SDC, in which case we can return an exchange rate directly.
//            if (sourceAssetId.equals(DefaultSupportedAssetService.SAND_GRAIN_CURRENCY)) {
//                return fromSandTo(destinationAssetId);
//            } else if (destinationAssetId.equals(DefaultSupportedAssetService.SAND_GRAIN_CURRENCY)) {
//                return toSandFrom(sourceAssetId);
//            } else {
//                // To convert to the destination, we first convert from source to sand.
//                // Then, we convert from sand to dest.
//                // For example, RED to Sand is .5.  Then, Sand to Blue is 2.  The FX rate is 2 * .5, or 1.0.
//                BigDecimal sandPerSource = toSandFrom(sourceAssetId).getExchangeRate();
//                BigDecimal destPerSand = fromSandTo(destinationAssetId).getExchangeRate();
//
//                return ExchangeRateInfo.builder()
//                        .destinationAmount(sandPerSource.multiply(destPerSand).setScale(10, BigDecimal.ROUND_HALF_UP))
//                        .sourceAssetId(sourceAssetId)
//                        .destinationAssetId(destinationAssetId)
//                        .build();
//            }

        //  return null;
        // }
        @Override
        public ExchangeRateInfo getExchangeRate(MonetaryAmount sourceAmount, CurrencyUnit destinationCurrencyUnit) {
            Objects.requireNonNull(sourceAmount);
            Objects.requireNonNull(destinationCurrencyUnit);

            return null;
        }

//        @VisibleForTesting
//        protected final ExchangeRateInfo fromSandTo(AssetId otherAssetId) {
//            return ExchangeRateInfo.builder()
//                    .destinationAmount(SAND_EXCHANGE_RATES.get(otherAssetId))
//                    .destinationAssetId(otherAssetId)
//                    .sourceAssetId(DefaultSupportedAssetService.SAND_GRAIN_CURRENCY)
//                    .build();
//        }
//
//        @VisibleForTesting
//        protected final ExchangeRateInfo toSandFrom(AssetId otherAssetId) {
//            return ExchangeRateInfo.builder()
//                    .destinationAmount(
//                            BigDecimal.ONE.divide(SAND_EXCHANGE_RATES.get(otherAssetId), 10, RoundingMode.HALF_UP))
//                    .destinationAssetId(DefaultSupportedAssetService.SAND_GRAIN_CURRENCY)
//                    .sourceAssetId(otherAssetId)
//                    .build();
//        }
    }

    /**
     * An object that can represent an exchange rate, which is a pair of assets, and two amount values that represent
     * the value this connector would exchange for each asset type.
     */
    @Getter
    @RequiredArgsConstructor
    @Builder
    @ToString
    @EqualsAndHashCode
    class ExchangeRateInfo {
        @NonNull
        private final MonetaryAmount sourceAmount = null;
        @NonNull
        private final MonetaryAmount destinationAmount = null;
		public MonetaryAmount getSourceAmount() {
			return sourceAmount;
		}
		public MonetaryAmount getDestinationAmount() {
			return destinationAmount;
		}

        /**
         * Return the exchange rate, as a big decimal.  For example, if one of the source asset can be purchased for 10
         * of the destination, then the exchange rate is 1/10, or 0.1.  Conversely, if the source asset can be purchased
         * for .1 of the destination asset, then the exchange rate is 10.0.
         *
         * @return
         */
        // TODO: If you end up needing this, then consider implementing in the service instead.
//        public BigDecimal getExchangeRate() {
//            return sourceAmount.divide(destinationAmount, 10, BigDecimal.ROUND_HALF_UP);
//        }

    }
}
