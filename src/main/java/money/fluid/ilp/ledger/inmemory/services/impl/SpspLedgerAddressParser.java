package money.fluid.ilp.ledger.inmemory.services.impl;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.LedgerAddressParser;
import money.fluid.ilp.ledger.model.LedgerId;
import org.apache.commons.lang3.StringUtils;
import org.interledgerx.ilp.core.IlpAddress;

import java.util.Objects;

public class SpspLedgerAddressParser implements LedgerAddressParser {
    private final static String DEFAULT_LEDGER_HOST_ACCOUNT_SEPARATOR = "@";

    private String separator;
    private String accountName;
    private String ledgerName;

    public SpspLedgerAddressParser() {
        this(DEFAULT_LEDGER_HOST_ACCOUNT_SEPARATOR);
    }

    public SpspLedgerAddressParser(String separator) {
        if (separator == null) {
            separator = DEFAULT_LEDGER_HOST_ACCOUNT_SEPARATOR;
        }
        this.separator = separator;
    }

    @Override
    public org.interledgerx.ilp.core.IlpAddress parse(
            final String ledgerAddressString
    ) throws LedgerAddressParserException {

        Objects.requireNonNull(ledgerAddressString);

        if (StringUtils.isEmpty(ledgerAddressString)) {
            throw new LedgerAddressParserException("empty ledgerAccountId");
        }
        if (!ledgerAddressString.contains(separator)) {
            throw new LedgerAddressParserException(ledgerAddressString);
        }
        try {
            String parts[] = ledgerAddressString.trim().split(separator);
            accountName = parts[0];
            if (StringUtils.isEmpty(accountName)) {
                throw new IllegalArgumentException("empty account name");
            }
            ledgerName = parts[1];
            if (StringUtils.isEmpty(ledgerName)) {
                throw new IllegalArgumentException("empty ledger name");
            }
            return IlpAddress.of(LedgerAccountId.of(accountName), LedgerId.of(ledgerName));
        } catch (Exception ex) {
            throw new LedgerAddressParserException(ledgerAddressString, ex);
        }
    }
}