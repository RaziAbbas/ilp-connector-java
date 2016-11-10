package org.interledgerx.ilp.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

/**
 * An ILP address that uniquely identifies an account in a ledger for purposes of ILP transactions.
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class IlpAddress {

    @NonNull
    private final LedgerAccountId ledgerAccountId;
    @NonNull
    private final LedgerId ledgerId;

    /**
     * Helper method to create an instance of {@link IlpAddress}.
     */
    public static IlpAddress of(final LedgerAccountId accountId, final LedgerId ledgerId) {
        return new IlpAddress(accountId, ledgerId);
    }
}
