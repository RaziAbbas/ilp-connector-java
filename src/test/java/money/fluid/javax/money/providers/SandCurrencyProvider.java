package money.fluid.javax.money.providers;

import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link CurrencyProviderSpi} for registering a fictional currency called 'Sand' that is
 * denominated in granules of sand per single note.  The currency-code for this currency is "SND".
 */
public class SandCurrencyProvider implements CurrencyProviderSpi {

    public static final String SND = "SND";
    private Set<CurrencyUnit> sandSet = new HashSet<>();

    public SandCurrencyProvider() {
        sandSet.add(CurrencyUnitBuilder.of(SND, "SandCurrencyBuilder").build());
        sandSet = Collections.unmodifiableSet(sandSet);
    }

    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link CurrencyQuery}.
     *
     * @param query the {@link CurrencyQuery} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}s matching, never null.
     */
    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query) {
        // only ensure SND is the code, or it is a default query.
        if (query.isEmpty()
                || query.getCurrencyCodes().contains(SND)
                || query.getCurrencyCodes().isEmpty()) {
            return sandSet;
        }
        return Collections.emptySet();
    }
}