package money.fluid.ilp.ledger.inmemory.utils;

import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

public class MoneyUtils {
    public static MonetaryAmount toMonetaryAmount(String amount, String currencyCode) {
        return Money.of(new BigDecimal(amount), currencyCode);
    }

    public static MonetaryAmount zero(final String currencyCode) {
        return toMonetaryAmount("0.00", currencyCode);
    }
}
