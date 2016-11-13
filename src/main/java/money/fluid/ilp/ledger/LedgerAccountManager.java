package money.fluid.ilp.ledger;


import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.ledger.inmemory.exceptions.InvalidAccountException;
import money.fluid.ilp.ledger.inmemory.model.SimpleLedgerAccount;
import money.fluid.ilp.ledger.model.LedgerAccount;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerInfo;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * An interface that defines how an ILP {@link Ledger} can operate on discrete accounts in the actual ledger.
 */
public interface LedgerAccountManager {

    /**
     * @param account
     * @return
     * @deprecated This method is a candidate for removal from the LedgerAccountManager interface because it's plausible
     * that we want any accounts on a ledger created out of band, and not via any ILP process.
     */
    @Deprecated
    SimpleLedgerAccount createAccount(final IlpAddress account);

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

    LedgerAccount creditAccount(final IlpAddress ilpAddress, final MonetaryAmount amount);

    LedgerAccount debitAccount(final IlpAddress ilpAddress, final MonetaryAmount amount);

    /**
     * Get the information about the {@link Ledger} this manager operates on.
     *
     * @return
     */
    LedgerInfo getLedgerInfo();

    /**
     * If the suppllied {@link IlpAddress#getLedgerId()} portion of the specified {@code ilpAddress} matches that of the
     * {@link Ledger} controlling this manager, then the supplied ILP address is considered 'locally services', meaning,
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

}
