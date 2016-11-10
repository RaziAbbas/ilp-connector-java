package money.fluid.ilp.connector.services.ledgers.plugins;

import org.interledgerx.ilp.core.Ledger;

/**
 * A default implementation of {@link LedgerPlugin}.
 *
 * @deprecated Remove this class and merge it into {@link Ledger} and LedgerAccountManager.
 */
@Deprecated
public class DefaultLedgerPlugin {// implements LedgerPlugin {

//    @Getter
//    private final String prefix;
//    @Getter
//    private final String account;
//
//    private final Ledger ledger;
//
//    /**
//     * Required-args Constructor.
//     *
//     * @param prefix
//     * @param account
//     * @param ledger  An instance of {@link Ledger} that this plugin can operate upon.
//     */
//    public DefaultLedgerPlugin(final String prefix, final String account, final Ledger ledger) {
//        this.prefix = Objects.requireNonNull(prefix);
//        this.account = Objects.requireNonNull(account);
//        this.ledger = Objects.requireNonNull(ledger);
//    }
//
//    @Override
//    public void send(final LedgerTransfer transfer) {
//        Preconditions.checkNotNull(transfer);
//
//        // final LedgerAccountManager accountManager = LedgerAccountManagerFactory.getAccountManagerSingleton();
//        // final LedgerAccount from = accountManager.getAccountByName(transfer.getSourceAccountLocalIdentifier());
//        // final LedgerAccount to = accountManager.getAccountByName(transfer.getDestinationAccountLocalIdentifier());
//        if (to.equals(from)) {
//            throw new RuntimeException("Accounts are the same!");
//        }
//
//        final MonetaryAmount amount = MoneyUtils.toMonetaryAmount(transfer.getAmount(), info.getCurrencyCode());
//        if (from.getBalance().isGreaterThanOrEqualTo(amount)) {
//            from.debit(amount);
//            to.credit(amount);
//        } else {
//            throw new InsufficientAmountException(amount.toString());
//        }
//
//        // For Local Transfer, the only event is a LedgerDirectTransferEvent.
//        final LedgerDirectTransferEvent ledgerTransferExecutedEvent = new LedgerDirectTransferEvent(
//                this, transfer.getHeader(), transfer.getSourceAccountLocalIdentifier(),
//                transfer.getDestinationAccountLocalIdentifier(), transfer.getAmount()
//        );
//        this.notifyEventHandlers(ledgerTransferExecutedEvent);
//    }
//
//    /**
//     * If the specified {@code destinationAddress} has a corresponding account on this ledger, then the account is
//     * considered 'local', and this method will return {@code true}.  Otherwise, the account is considered 'non-local',
//     * and this method will return {@code false}.
//     *
//     * @param destinationAddress A {@link String} representing an ILP address of the ultimate destination account that
//     *                           funds will be transferred to as part of a {@link LedgerTransfer}.
//     * @return
//     */
//    private boolean isLocalAccount(final String destinationAddress) {
//        // TODO: Refactor SimpleLedgerAddressParser for DI and then thread-safety.
//        final SimpleLedgerAddressParser parser = new SimpleLedgerAddressParser();
//        parser.parse(destinationAddress);
//        final String accountName = parser.getAccountName();
//        return account != null;
//    }
//
//    /**
//     * Completes the supplied {@link LedgerTransfer} locally without utilizing any conditions or connectors.
//     *
//     * @param transfer An instance of {@link LedgerTransfer} to complete locally.
//     */
//    private void sendLocally(final LedgerTransfer transfer) {
//
//    }
//
//    /**
//     * Attempts to complete the supplied {@link LedgerTransfer} using ILP.
//     *
//     * @param ledgerTransfer
//     */
//    private void sendRemote(final LedgerTransfer ledgerTransfer) {
//
//        // Since this is a remote ILP transfer, this ledger needs to determine a Connector account to send through.
//        // This module determines a destination account on the local ledger for this interledger address.
//        // In this case it is the account of a connector.
//        //
//        // It passes the chosen amount and the local destination account to the local ledger interface.
//
//
//    }

}
