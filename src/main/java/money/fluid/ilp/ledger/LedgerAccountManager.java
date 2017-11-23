package money.fluid.ilp.ledger;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.inmemory.exceptions.InvalidAccountException;
import money.fluid.ilp.ledger.model.LedgerAccount;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerInfo;
import org.joda.time.DateTime;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * An interface that defines how an ILP {@link Ledger} can operate on discrete accounts in the actual ledger.
 */
public interface LedgerAccountManager {

    /**
     * Retrieves an account for the ILP {@link IlpAddress} speecified by {@code ledgerAddress}.  Note that the
     * supplied ledger address must be for an account on the ledger that this manager exists for.  Otherwise, and
     *
     * @param ilpAddress
     * @return
     * @throws InvalidAccountException if the {@link IlpAddress#getLedgerId()} does not match the ledger for which this
     *                                 {@link LedgerAccountManager} is operating.
     * @deprecated This method might change to merely get an account balance since it's uncertain that an ILP process
     * needs to get more details than that about an account.
     */
    @Deprecated
    Optional<LedgerAccount> getAccount(final IlpAddress ilpAddress)
            throws InvalidAccountException;

    /**
     * @param page
     * @param pageSize
     * @return
     * @deprecated Will an ILP process every need to page through all accounts on a Ledger?  This is unlikely...possibly
     * a wallet might, but that should be a different interface.
     */
    @Deprecated
    Collection<LedgerAccount> getAccounts(final int page, final int pageSize);

    /**
     * Transfers funds from the {@code localSourceAddress} to the  {@code localDestinationAddress}.
     *
     * @param localSourceAddress
     * @param localDestinationAddress
     * @param amount
     * @return
     */
    void transfer(
            final IlpAddress localSourceAddress, final IlpAddress localDestinationAddress, final MonetaryAmount amount
    );

    /**
     * Get the information about the {@link Ledger} this manager operates on.
     *
     * @return
     */
    LedgerInfo getLedgerInfo();

    /**
     * If the suppllied {@link IlpAddress#getLedgerId()} portion of the specified {@code ilpAddress} matches that of the
     * {@link Ledger} controlling this manager, then the supplied ILP address is considered 'locally serviced', meaning,
     * that ILP address is serviced by an account on the local ledger.  In that case, this method will return {@code
     * true}. Otherwise, the account is considered 'non-local', and this method will return {@code false}.
     *
     * @param ilpAddress An {@link IlpAddress} representing an account in ILP space.
     * @return
     */
    default boolean isLocallyServiced(final IlpAddress ilpAddress) {
        Objects.requireNonNull(ilpAddress);
        return this.getLedgerInfo().getLedgerId().equals(ilpAddress.getLedgerId());
    }

    /**
     * An actual record of transfers between accounts on a single ledger.  This is used to correlate internal ledger
     * account transfers to ILP LedgerTransfers.
     */
    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    class LedgerAccountTransfer implements Comparable<LedgerAccountTransfer> {
        // The id of the ILP payment that triggered this local transfer.
        @NonNull
        private IlpTransactionId ilpTransactionId;

        @NonNull
        private final DateTime transferDateTime = null;

        @NonNull
        private final IlpAddress localSourceAddress = null;

        @NonNull
        private final IlpAddress localDestinationAddress = null;

        @NonNull
        private final MonetaryAmount amount = null;

        @Override
        public int compareTo(LedgerAccountTransfer o) {
            return this.compareTo(o);
        }
    }
}
