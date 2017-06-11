package money.fluid.javax.money.providers;

import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link CurrencyProviderSpi} for registering a fictional currency called 'Dirt' that is
 * denominated in granules of dirt per single note.  The currency-code for this currency is "DRT".
 */
public class DirtCurrencyProvider implements CurrencyProviderSpi {

    public static final String DRT = "DRT";
    private Set<CurrencyUnit> dirtSet = new HashSet<>();

    public DirtCurrencyProvider() {
        dirtSet.add(CurrencyUnitBuilder.of(DRT, "DirtCurrencyBuilder")
                            .setDefaultFractionDigits(2)
                            .build());
        dirtSet = Collections.unmodifiableSet(dirtSet);
    }

    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link javax.money.CurrencyQuery}.
     *
     * @param query the {@link javax.money.CurrencyQuery} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}s matching, never null.
     */
    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query) {
        // only ensure DRT is the code, or it is a default query.
        if (query.isEmpty()
                || query.getCurrencyCodes().contains(DRT)
                || query.getCurrencyCodes().isEmpty()) {
            return dirtSet;
        }
        return Collections.emptySet();
    }
}