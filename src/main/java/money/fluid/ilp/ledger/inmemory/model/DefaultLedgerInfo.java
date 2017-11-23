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
    private int precision = 0;
    private int scale = 0;
    private String currencyCode = null;
    private String currencySymbol = "USD";
    private LedgerId ledgerId = null;

    public static LedgerInfo create(int precission, int scale, String currencyCode, final LedgerId ledgerId) {
        return create(precission, scale, currencyCode, CurrencyUtils.getSymbol(currencyCode), ledgerId);
    }

    public DefaultLedgerInfo (int precission, int scale, String currencyCode, String currencySymbol, final LedgerId ledgerId) {
        this.precision = precission;
        this.scale = scale;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.ledgerId = ledgerId;
    }
    
    
    public int getPrecision() {
		return precision;
	}



	public int getScale() {
		return scale;
	}



	public String getCurrencyCode() {
		return currencyCode;
	}



	public String getCurrencySymbol() {
		return currencySymbol;
	}



	public LedgerId getLedgerId() {
		return ledgerId;
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