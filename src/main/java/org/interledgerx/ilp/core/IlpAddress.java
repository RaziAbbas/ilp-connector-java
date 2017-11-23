package org.interledgerx.ilp.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

/**
 * An ILP address that uniquely identifies an account in a ledger for purposes of ILP transactions.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class IlpAddress {

	@NonNull
    private LedgerAccountId ledgerAccountId = null;
    @NonNull
    private LedgerId ledgerId = null;
	
    public LedgerAccountId getLedgerAccountId() {
		return ledgerAccountId;
	}

	public LedgerId getLedgerId() {
		return ledgerId;
	}
	
	private IlpAddress(LedgerAccountId vaccountId, LedgerId vledgerId) {
		this.ledgerId = vledgerId;
		this.ledgerAccountId = vaccountId;
	}

    /**
     * Helper method to create an instance of {@link IlpAddress}.
     */
    public static IlpAddress of(final LedgerAccountId accountId, final LedgerId ledgerId) {
        return new IlpAddress(accountId, ledgerId);
    }

    @Override
    public String toString() {
        return this.getLedgerAccountId() + "@" + this.getLedgerId();
    }
}
