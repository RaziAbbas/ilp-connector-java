package money.fluid.ilp.ledger.model;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;

import javax.money.MonetaryAmount;

/**
 * An account on a particular {@link Ledger}.
 */
public interface LedgerAccount {

    // The ledger-local account identifier (e.g., "123456789"
    LedgerAccountId getId();

    // The ILP address of this account.
    IlpAddress getIlpIdentifier();

    MonetaryAmount getBalance();
}
