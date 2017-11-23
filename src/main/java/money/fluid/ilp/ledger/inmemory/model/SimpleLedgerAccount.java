package money.fluid.ilp.ledger.inmemory.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerAccount;
import org.interledgerx.ilp.core.IlpAddress;

import javax.money.MonetaryAmount;

@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class SimpleLedgerAccount implements LedgerAccount {
	
    public SimpleLedgerAccount(LedgerAccountId of, IlpAddress ilpAddress, MonetaryAmount initialAmount) {
		this.ledgerAccountId = of;
		this.ilpIdentifier = ilpAddress;
		this.balance = initialAmount;
	}

	@NonNull
    private final LedgerAccountId ledgerAccountId;

    @NonNull
    private final IlpAddress ilpIdentifier;

    @NonNull
    private final MonetaryAmount balance;

	public LedgerAccountId getLedgerAccountId() {
		return ledgerAccountId;
	}

	public IlpAddress getIlpIdentifier() {
		return ilpIdentifier;
	}

	public MonetaryAmount getBalance() {
		return balance;
	}

	@Override
	public LedgerAccountId getId() {
		return this.ledgerAccountId;
	}
}
