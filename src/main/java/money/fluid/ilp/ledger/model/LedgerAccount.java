package money.fluid.ilp.ledger.model;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.IlpAddress;

import javax.money.MonetaryAmount;

public interface LedgerAccount {
    // The ledger-local account identifier (e.g., "123456789"
    LedgerAccountId getLedgerAccountId();

    // The ILP address of this account.
    IlpAddress getIlpIdentifier();

    MonetaryAmount getBalance();
}
