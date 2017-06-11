package money.fluid.javax.money.providers;

import com.google.common.collect.ImmutableSet;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryContext;
import javax.money.MonetaryContextBuilder;
import javax.money.MonetaryRounding;
import javax.money.RoundingContext;
import javax.money.RoundingContextBuilder;
import javax.money.RoundingQuery;
import javax.money.spi.RoundingProviderSpi;
import java.math.BigDecimal;
import java.util.Set;

public class SandRoundingProvider implements RoundingProviderSpi {

    private final Set<String> roundingNames = ImmutableSet.of("SND");
    private final MonetaryRounding sandRounding = new SandRounding();

    public SandRoundingProvider() {
    }

    public MonetaryRounding getRounding(RoundingQuery query) {
        final CurrencyUnit currency = query.getCurrency();
        if (currency != null && ("SND".equals(currency.getCurrencyCode()))) {
            return sandRounding;
        } else if ("SND".equals(query.getRoundingName())) {
            return sandRounding;
        }
        return null;
    }

    public Set<String> getRoundingNames() {
        return roundingNames;
    }


    public static final class SandRounding implements MonetaryRounding {
        @Override
        public RoundingContext getRoundingContext() {
            return RoundingContextBuilder.of("SandRoundingProvider", "SND").build();
        }

        @Override
        public MonetaryAmount apply(MonetaryAmount amount) {
            if (amount.getNumber().getNumberType().equals(BigDecimal.class)) {

                // Round the BigDecimal...
                final BigDecimal bd = amount.getNumber().numberValue(BigDecimal.class);

                final MonetaryContext mc = MonetaryContextBuilder.of()
                        .setMaxScale(2)
                        .setPrecision(0)
                        .setFixedScale(true)
                        .build();
                final MonetaryAmount newAmount = amount.getFactory()
                        .setCurrency(amount.getCurrency())
                        .setNumber(bd.setScale(2, BigDecimal.ROUND_HALF_UP))
                        .setContext(mc)
                        .create();
                return newAmount;
            } else {
                // TODO: Account for other Money types, like FastMoney...
                return amount;
            }
        }
    }
}
