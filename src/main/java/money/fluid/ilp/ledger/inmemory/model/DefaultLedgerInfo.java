package money.fluid.ilp.ledger.inmemory.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledger.inmemory.utils.CurrencyUtils;
import org.interledgerx.ilp.core.LedgerInfo;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class DefaultLedgerInfo implements LedgerInfo {
    private final int precision;
    private final int scale;
    private final String currencyCode;
    private final String currencySymbol;
    private final LedgerId ledgerId;

    public static LedgerInfo create(int precission, int scale, String currencyCode, final LedgerId ledgerId) {
        return create(precission, scale, currencyCode, CurrencyUtils.getSymbol(currencyCode), ledgerId);
    }

    public static LedgerInfo create(
            int precission, int scale, String currencyCode, String currencySymbol, final LedgerId ledgerId
    ) {
        return new DefaultLedgerInfo(precission, scale, currencyCode, currencySymbol, ledgerId);
    }

//    public static LedgerInfo from(final CurrencyUnit currency) {
//        return create(
//                CurrencyUtils.getPrecision(currency),
//                currency.getDefaultFractionDigits(),
//                currency.getCurrencyCode()
//        );
//    }
}