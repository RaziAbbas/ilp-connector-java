package money.fluid.ilp.ledger.inmemory.utils;

import org.javamoney.moneta.Money;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryContext;
import javax.money.MonetaryContextBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.MathContext.UNLIMITED;

public class MoneyUtils {
    public static MonetaryAmount toMonetaryAmount(String amount, String currencyCode) {
//        final MonetaryContext mc = MonetaryContextBuilder.of()
//                .setMaxScale(2)
//                .setFixedScale(true)
//                .setPrecision(0)
//                .set(RoundingMode.HALF_UP)
//                .build();
        // TODO: Use a JavaMoney RoundingProvider instead...
        //final BigDecimal bdAmount = new BigDecimal(amount, UNLIMITED).setScale(2, BigDecimal.ROUND_HALF_UP);
        final BigDecimal bdAmount = new BigDecimal(amount, UNLIMITED);
        final MonetaryAmount monetaryAmount = Money.of(bdAmount, currencyCode);
        return monetaryAmount.with(Monetary.getRounding(monetaryAmount.getCurrency()));
    }

    public static MonetaryAmount zero(final String currencyCode) {
        return toMonetaryAmount("0.00", currencyCode);
    }
}
