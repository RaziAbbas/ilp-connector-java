package money.fluid.ilp.ledger.inmemory;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.exceptions.InvalidQuoteRequestException;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.EscrowManager;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.QuotingService;
import money.fluid.ilp.ledger.inmemory.exceptions.AccountNotFoundException;
import money.fluid.ilp.ledger.inmemory.exceptions.InvalidAccountException;
import money.fluid.ilp.ledger.inmemory.model.Escrow;
import money.fluid.ilp.ledger.inmemory.model.EscrowInputs;
import money.fluid.ilp.ledger.inmemory.model.SimpleLedgerAccount;
import money.fluid.ilp.ledger.inmemory.utils.MoneyUtils;
import money.fluid.ilp.ledger.model.ConnectionInfo;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import money.fluid.ilp.ledger.model.LedgerAccount;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerConnectionManager;
import org.interledgerx.ilp.core.LedgerEventListener;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransfer;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;
import org.interledgerx.ilp.core.events.LedgerConnectedEvent;
import org.interledgerx.ilp.core.events.LedgerEvent;
import org.interledgerx.ilp.core.events.LedgerEventHandler;
import org.interledgerx.ilp.core.events.LedgerTransferExecutedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferPreparedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferRejectedEvent;
import org.interledgerx.ilp.core.exceptions.InsufficientAmountException;

import javax.money.MonetaryAmount;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * An implementation of {@link Ledger} that simulates a real ledger supporting ILP functionality.  Ordinarily, a ledger
 * would operate in its own ledger-space, and would implement the {@link Ledger} interface to provide external
 * connectivity.  This implementation runs "in-memory", so its event emissions don't need to involve any RPCs.
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class InMemoryLedger implements Ledger {

    // TODO: In a real ledger, should be configurable.
    private static final LedgerAccountId ESCROW = LedgerAccountId.of("__escrow__");

    @NonNull
    private final String name;

    @NonNull
    @Getter
    private final LedgerInfo ledgerInfo;

    @NonNull
    private final QuotingService quotingService;

    /////////////
    /////////////

    @Getter
    @NonNull
    private final LedgerAccountManager ledgerAccountManager;

    // Each Ledger has a ConnectionManager that processes callbacks.
    @Getter
    @NonNull
    private final InMemoryLedgerConnectionManager ledgerConnectionManager;

    @NonNull
    private final EscrowManager escrowManager;

    public InMemoryLedger(final String name, final LedgerInfo ledgerInfo, final QuotingService quotingService) {
        this.name = name;
        this.ledgerInfo = ledgerInfo;

        // TODO: Implement a proper QuotingService!
        this.quotingService = Objects.requireNonNull(quotingService);

        // TODO: Create constructors to allow these to be passed-in!
        this.ledgerConnectionManager = new InMemoryLedgerConnectionManager(this.getLedgerInfo());
        this.ledgerAccountManager = new InMemoryLedgerAccountManager(ledgerInfo);

        // Create an Escrow Account in this ledger...
        final IlpAddress escrowAccountAddress = IlpAddress.of(ESCROW, ledgerInfo.getLedgerId());
        this.getLedgerAccountManager().createAccount(escrowAccountAddress);
        this.escrowManager = new EscrowManager(
                ledgerInfo, escrowAccountAddress.getLedgerAccountId(), ledgerAccountManager);
    }

    /**
     * Initiate an ILP transfer.  This implementation assumes that all transfers involve a connector.  If a
     * particular transfer doesn't involve a connector, then this method should not be invoked.
     * <p>
     * TODO: What if a ledger wants to offer local ILP-type transaction services?  Should this be allowed?  For example,
     * couldn't BankX just transfer funds from one ILP address to another in some other process without involving
     * this Ledger implementation (in other words, without involving ILP at all?)
     */
    @Override
    public void send(final LedgerTransfer transfer) {
        Preconditions.checkNotNull(transfer);

        //////////////
        // Local Ledger Transfer
        //////////////
        if (this.getLedgerAccountManager().isLocallyServiced(
                transfer.getInterledgerPacketHeader().getDestinationAddress())) {
            // Here, the ledger itself can compute the local accounts for the sender and receiver.
            this.sendLocally(transfer);
        } else {
            //////////////
            // Send the payment to a connector that can best process the payment...
            //////////////
            this.sendRemotely(transfer);
        }
    }

    @Override
    public void rejectTransfer(
            IlpTransactionId ilpTransactionId, LedgerTransferRejectedReason ledgerTransferRejectedReason
    ) {
        // Whether remote or local, we always need to reverse the ILP transaction in optimistic mode transactions...
        final Escrow reversedEscrow = this.escrowManager.reverseEscrow(ilpTransactionId);

        // The ultimate source of the ILP transaction is local to this ledger, so there's no need to involve a connector.
        if (this.getLedgerAccountManager().isLocallyServiced(reversedEscrow.getIlpPacketHeader().getSourceAddress())) {
            // TODO: Notify the Wallet!
        }
        // The ultimate source of this ILP transaction is non-local, so the ledger needs to notify the appropriate connector
        // so that it can pass its fulfillments back up the ILP chain.
        else {
            final LedgerTransferRejectedEvent event = new LedgerTransferRejectedEvent(
                    this.getLedgerInfo(),
                    reversedEscrow.getIlpPacketHeader(),
                    reversedEscrow.getLocalSourceAddress().getLedgerAccountId(),
                    reversedEscrow.getLocalDestinationAddress().getLedgerAccountId(),
                    reversedEscrow.getAmount(),
                    ledgerTransferRejectedReason
            );

            // Given a source address (for the Connector) ask the ledger for the connectorId.
            final ConnectorId connectorId = this.getSourceConnector(reversedEscrow);
            this.getLedgerConnectionManager().notifyEventListeners(connectorId, event);
        }
    }

    @Override
    public void fulfillCondition(Fulfillment fulfillment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void fulfillCondition(final IlpTransactionId ilpTransactionId) {
        // Whether remote or local, we always need to execute the ILP transaction in optimistic mode transactions...
        final Escrow executedEscrow = this.escrowManager.executeEscrow(ilpTransactionId);

        // If the source account was local to _this_ ledger, then we don't need to do anything except notify the
        // connector that they receieved an escrow distribution (?).
        if (this.getLedgerAccountManager().isLocallyServiced(executedEscrow.getIlpPacketHeader().getSourceAddress())) {
            // TODO: Notify the Wallet!
        }
        // The account is non-local, so notify the appropriate connector.
        else {
            final LedgerTransferExecutedEvent event = new LedgerTransferExecutedEvent(
                    this.getLedgerInfo(),
                    executedEscrow.getIlpPacketHeader(),
                    // TODO: Should the events use an ILPAddress instead?
                    executedEscrow.getLocalSourceAddress().getLedgerAccountId(),
                    executedEscrow.getLocalDestinationAddress().getLedgerAccountId(),
                    executedEscrow.getAmount()
            );

            // Given a source address (for the Connector) ask the ledger for the connectorId.
            final ConnectorId connectorId = this.getSourceConnector(executedEscrow);
            this.getLedgerConnectionManager().notifyEventListeners(connectorId, event);
        }
    }

    //////////////////
    // Private Helpers
    //////////////////

    /**
     * Helper method to encapsulate all logic surrounding the determination of which Connector should be processing
     * events related to an {@link IlpTransactionId}.
     * <p>
     * TODO: Fix this per https://github.com/fluid-money/ilp-connector-java/issues/1.
     * <p>
     * Rather than making a real-time judgement about which connector can "currently" service the callback
     * based upon the source address, the ledger likely needs to be tracking the reverse-path of the connector
     * so that it can properly send events back to the right connector.  For example, imagine an ILP transfer
     * that came in via ConnectorA, but by the time the transfer is approved by hte ledger, ConnectorA is no longer
     * connected, but ConnectorB provides a "route" back to the ultimate ILP source address.  In this case, ConnectorB
     * won't actually be able to fulfil the payment/rejection, because it wasn't the original connector.  Thus,
     * the ledger has to inteligently track "pending transfers" just like the Connector does.
     */
    private ConnectorId getSourceConnector(final Escrow escrow) {  //final IlpTransactionId ilpTransactionId) {
        return this.getLedgerConnectionManager().ledgerEventListeners
                .values().stream()
                .filter(ledgerEventListener -> ledgerEventListener.getConnectorInfo().getOptLedgerAccountId().isPresent())
                .filter(ledgerEventListener -> ledgerEventListener.getConnectorInfo().getOptLedgerAccountId().get().equals(
                        escrow.getLocalSourceAddress().getLedgerAccountId()))
                .map(ledgerEventListener -> ledgerEventListener.getConnectorInfo().getConnectorId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Unable to find ConnectorId for escrow source account: %s.",
                        escrow.getLocalSourceAddress()
                )));
    }

    /**
     * A lookup utility to find the local account for a Connector.
     * <p>
     * TODO: For simulation purposes, this implementation merely uses a local account identifier that matches the
     * connectorid.  In a real implementation, this value should be created during an account registration phase that is
     * outside the scope of ILP.
     *
     * @param connectorId
     * @return
     */
    private Optional<IlpAddress> getLocalLedgerIlpAddressForConnector(final ConnectorId connectorId) {
        Objects.requireNonNull(connectorId);
        return Optional.of(IlpAddress.of(LedgerAccountId.of(connectorId.getId()), this.getLedgerInfo().getLedgerId()));
    }

    /**
     * Completes the supplied {@link LedgerTransfer} locally by adding funds to escrow.  This will allow a recipient to
     * accept or reject the incoming transfer via Ledger-specific mechanisms.
     *
     * @param transfer An instance of {@link LedgerTransfer} to complete locally.
     */
    private void sendLocally(final LedgerTransfer transfer) {
        // Process the transfer locally...
        final MonetaryAmount amount = transfer.getAmount();

        final LedgerAccount localSourceAccount = this.getLedgerAccountManager().getAccount(
                transfer.getLocalSourceAddress()).get();
        final LedgerAccount localDestinationAccount = this.getLedgerAccountManager().getAccount(
                transfer.getInterledgerPacketHeader().getDestinationAddress()).get();

        Preconditions.checkArgument(
                !localSourceAccount.equals(localDestinationAccount), "Can't transfer to/from the same account!");

        if (localSourceAccount.getBalance().isGreaterThanOrEqualTo(amount)) {
            // PUT Money on-hold...
            final EscrowInputs escrowInputs = EscrowInputs.builder()
                    .interledgerPacketHeader(transfer.getInterledgerPacketHeader())
                    .sourceAddress(localSourceAccount.getIlpIdentifier())
                    .destinationAddress(localDestinationAccount.getIlpIdentifier())
                    .amount(transfer.getAmount())
                    .build();
            this.escrowManager.initiateEscrow(escrowInputs);

            // This implmentation doesn't involve a connector, and therefore doesn't need to send any notifications
            // because the it assumes that the ledger will handle this in some form or fashion.
            // TODO: Is this a valid assumption, or does ILP handle this?
        } else {
            throw new InsufficientAmountException(amount.toString());
        }
    }

    /**
     * Completes the supplied {@link LedgerTransfer} using another connector.
     *
     * @param transfer An instance of {@link LedgerTransfer} to complete remotely.
     */
    private void sendRemotely(final LedgerTransfer transfer) {

        // In order to initiate a remote transfer, the LedgerTransferInputs needs to be transformed into
        // a LedgerTransfer that contains local account information.


        // Find appropriate routable Connector
        final Optional<ConnectorInfo> optConnectorInfo = quotingService.findBestConnector(transfer);

        if (optConnectorInfo.isPresent()) {
            final ConnectorInfo connectorInfo = optConnectorInfo.get();

            // PUT Money on-hold from the source, for a Connector...
            this.getLocalLedgerIlpAddressForConnector(connectorInfo.getConnectorId())
                    .map(connectorIlpAddress -> {
                             // Compute the local source account for the transfer given an ILP source address.  This is
                             // only stored locally in escrow.
                             final IlpAddress localSourceAccountId = transfer.getInterledgerPacketHeader().getSourceAddress();

                             // The local destination account for the transfer is the designated Connector's account.  This is
                             // only stored in Escrow.
                             final IlpAddress localDestinationAccountId = connectorIlpAddress;

                             final EscrowInputs escrowInputs = EscrowInputs.builder()
                                     .interledgerPacketHeader(transfer.getInterledgerPacketHeader())
                                     .sourceAddress(localSourceAccountId)
                                     .destinationAddress(localDestinationAccountId)
                                     .amount(transfer.getAmount())
                                     .build();

                             // Execute Escrow!
                             final Escrow escrow = this.escrowManager.initiateEscrow(escrowInputs);

                             // Notify any Listening Connectors that they have assets waiting!
                             this.getLedgerConnectionManager().notifyEventListeners(
                                     connectorInfo.getConnectorId(),
                                     new LedgerTransferPreparedEvent(
                                             this.getLedgerInfo(),
                                             transfer.getInterledgerPacketHeader(),
                                             // TODO: Should the events use an ILPAddress instead?
                                             escrow.getLocalSourceAddress().getLedgerAccountId(),
                                             escrow.getLocalDestinationAddress().getLedgerAccountId(),
                                             escrow.getAmount()
                                     )
                             );
                             return escrow;
                         }
                    )
                    .orElseThrow(() -> new AccountNotFoundException(
                            "ConnectorId: " + connectorInfo.getConnectorId().getId()));
        } else {
            // TODO: Throw something better here
            throw new InvalidQuoteRequestException("No Connector available");
        }

        // TODO: Handle multiple debits?
        // TODO: store the transfer status somewhere?

//            for (Debit debit : newTransfer.getDebits()) {
//                executeLocalTransfer(debit.account, HOLDS_URI, debit.amount);
//            }
//            newTransfer.setTransferStatus(TransferStatus.PROPOSED);
    }


//    /**
//     * Helper method to transform an instance of {@link LedgerTransferInputs} into an instance of {@link LedgerTransfer}
//     * by looking up appropriate local account identifiers and populating them into the returned object.  For example,
//     * if a request comes in to transfer between ILP address 1 and ILP address 2, then the local accounts might be
//     * different in order to satisfy the connection.
//     *
//     * @param transferInputs
//     * @param connectorInfo  An instance of {@link ConnectorInfo} that contains information necessary to determine local
//     *                       account identifiers.
//     * @return
//     */
//    @VisibleForTesting
//    LedgerTransfer<String, NoteToSelf> toLedgerTransfer(
//            final LedgerTransferInputs transferInputs, final ConnectorInfo connectorInfo
//    ) {
//
//
//        final IlpAddress localSourceAddress = this.getLedgerAccountManager().getAccount()
//        final IlpAddress localDestinationAddress = this.getLedgerAccountManager().getAccount();
//
//        return DefaultLedgerTransfer.builder()
//                .interledgerPacketHeader(transferInputs.getInterledgerPacketHeader())
//                .localSourceAddress(localSourceAddress)
//                .localDestinationAddress(localDestinationAddress)
//                .amount(transferInputs.getAmount())
//                .optData(transferInputs.getOptData())
//                .optNoteToSelf(transferInputs.getOptNoteToSelf())
//                .build();
//    }

    ////////////////////////////
    // Internal Implementations
    ////////////////////////////

    @RequiredArgsConstructor
    @Getter
    private class InMemoryLedgerAccountManager implements LedgerAccountManager {

        @NonNull
        private final LedgerInfo ledgerInfo;

        // Local identifier to account mapping...Consider a Set here?
        @NonNull
        private final Map<IlpAddress, LedgerAccount> accounts;

        private InMemoryLedgerAccountManager(final LedgerInfo ledgerInfo) {
            this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
            this.accounts = new HashMap<>();
        }

        /**
         * When an account is created, it is initialized with a zero balance and an associated ILP address identifier.
         *
         * @param ilpAddress
         */
        @Override
        public SimpleLedgerAccount createAccount(final IlpAddress ilpAddress) {
            final SimpleLedgerAccount newAccount = new SimpleLedgerAccount(
                    LedgerAccountId.of(UUID.randomUUID().toString()), ilpAddress,
                    MoneyUtils.zero(this.ledgerInfo.getCurrencyCode())
            );
            this.accounts.put(ilpAddress, newAccount);
            return newAccount;
        }

        @Override
        public Optional<LedgerAccount> getAccount(final IlpAddress ilpAddress)
                throws InvalidAccountException {
            Objects.requireNonNull(ilpAddress);
            Preconditions.checkArgument(
                    ilpAddress.getLedgerId().equals(this.getLedgerInfo().getLedgerId()),
                    "Can't retrieve an ILP LedgerAccount for a foreign Ledger!"
            );

            return Optional.ofNullable(this.accounts.get(ilpAddress));
        }

        @Override
        public Collection<LedgerAccount> getAccounts(int page, int pageSize) {
            // No paging necessary since this is just an in-memory implementation.
            return this.accounts.values();
        }

        /**
         * This implementation requires that the account to be debited exist in order for this operation to succeed.
         *
         * @param ilpAddress
         * @param amount
         * @return
         */
        @Override
        public LedgerAccount creditAccount(final IlpAddress ilpAddress, final MonetaryAmount amount) {
            Objects.requireNonNull(ilpAddress);
            Objects.requireNonNull(amount);
            Preconditions.checkArgument(amount.isPositiveOrZero(), "Transfers must be $0 or greater!");
            Preconditions.checkArgument(
                    amount.getCurrency().getCurrencyCode().equals(this.getLedgerInfo().getCurrencyCode()),
                    "Transfers must specify the same currency code as this Ledger!"
            );

            final LedgerAccount creditedLedgerAccount = this.getAccount(ilpAddress)
                    .map(ledgerAccount -> new SimpleLedgerAccount(
                            ledgerAccount.getLedgerAccountId(), ledgerAccount.getIlpIdentifier(),
                            ledgerAccount.getBalance().add(amount)
                    )).orElseThrow(() -> new RuntimeException("No account exists for " + ilpAddress));

            return this.accounts.put(ilpAddress, creditedLedgerAccount);
        }


        /**
         * This implementation requires that the account to be debited exist in order for this operation to succeed.
         *
         * @param ilpAddress
         * @param amount
         * @return
         */
        @Override
        public LedgerAccount debitAccount(final IlpAddress ilpAddress, final MonetaryAmount amount) {
            Objects.requireNonNull(ilpAddress);
            Objects.requireNonNull(amount);
            Preconditions.checkArgument(
                    amount.getCurrency().getCurrencyCode().equals(this.getLedgerInfo().getCurrencyCode()),
                    "Transfers must specify the same currency code as this Ledger!"
            );

            final LedgerAccount debitedLedgerAccount = this.getAccount(ilpAddress)
                    .map(ledgerAccount -> new SimpleLedgerAccount(
                            ledgerAccount.getLedgerAccountId(), ledgerAccount.getIlpIdentifier(),
                            ledgerAccount.getBalance().subtract(amount)
                    )).orElseThrow(() -> new RuntimeException("No account exists for " + ilpAddress));

            // Disallow the account from going negative...
            Preconditions.checkArgument(debitedLedgerAccount.getBalance().isPositiveOrZero());

            return this.accounts.put(ilpAddress, debitedLedgerAccount);
        }
    }

    /**
     * An internal implementation of {@link LedgerConnectionManager} that handles all connections for listening
     * connectors.
     */
    @RequiredArgsConstructor
    public class InMemoryLedgerConnectionManager implements LedgerConnectionManager {

        // Information about the ledger that this connection manager operates on behalf of.
        @NonNull
        @Getter
        private final LedgerInfo ledgerInfo;

        // Each time a Connector connects to this Ledger, a LedgerEventListener will be added to this list with a unique connection id...
        // NOTE: For real applications, consider using something akin to Guava's SubscriberRegistry found in EventBus.java.
        @NonNull
        private final Map<ConnectorId, LedgerEventListener> ledgerEventListeners;

        /**
         * Helper constructor.
         *
         * @param ledgerInfo
         */
        public InMemoryLedgerConnectionManager(final LedgerInfo ledgerInfo) {
            this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
            this.ledgerEventListeners = new HashMap<>();
        }

        @Override
        public void connect(final ConnectionInfo connectionInfo) {
            // TODO: Validate authentication!
            // TODO: Validate inputs!

            // For a given LedgerEventListener, this implementation should have a mechanism to callback to the Connector
            // that registered the listener.  This could be something like a Webhook, or it could be a direct in-process
            // call.

            // For each ILP connection to this ledger, a LedgerEventListener is added to the list of listeners.  This
            // listener contains one or more handlers that should connect back to the Connector that just connected.  This
            // layer is necessary in order to restrict callbacks/notifications to only the connector that should receive
            // them.

            final ConnectorInfo connectorInfo = ConnectorInfo.builder()
                    .connectorId(connectionInfo.getConnectorId())
                    // TODO: Determine how the Ledger gets the account of the Connector so that when money is sent by a
                    // connector to a local destination, the ledger know who to call-back.
                    .optLedgerAccountId(Optional.of(LedgerAccountId.of("fluid-connector-27"))).build();
            final LedgerEventListener ledgerEventListener = new InMemoryLedgerEventListener(
                    this.ledgerInfo, connectorInfo);
            this.ledgerEventListeners.put(connectionInfo.getConnectorId(), ledgerEventListener);
        }

        @Override
        public void disconnect(final ConnectorId connectorId) {
            // TODO: Validate authentication!
            // TODO: Validate inputs!
            this.ledgerEventListeners.remove(connectorId);
        }

        @Override
        public void notifyEventListeners(final ConnectorId targetedConnectorId, LedgerEvent ledgerEvent) {
            Objects.requireNonNull(targetedConnectorId);
            Objects.requireNonNull(ledgerEvent);

            this.ledgerEventListeners.values().stream()
                    // Only listeners with the proper ConnectorId target
                    .filter(listener -> listener.getConnectorInfo().getConnectorId().equals(targetedConnectorId))
                    .forEach(listener -> listener.notifyEventHandlers(ledgerEvent));
        }

        @Override
        public void registerEventHandler(
                final ConnectorId connectorId, final LedgerEventHandler ledgerEventHandler
        ) {
            // If the ledger is present, just add the handler.  Otherwise, throw an exception.

            final Optional<LedgerEventListener> optLedgerEventListener = Optional.ofNullable(
                    this.ledgerEventListeners.get(connectorId));

            if (optLedgerEventListener.isPresent()) {
                optLedgerEventListener.get().registerEventHandler(ledgerEventHandler);
            } else {
                throw new RuntimeException(String.format(
                        "No LedgerId registered: %s.  Call the connect() method first before trying to register!",
                        connectorId
                ));
            }
        }

    }

    /**
     * An implementation of {@link LedgerEventListener} that allows for a collection of {@link LedgerEventHandler}
     * to be associated to a single Ledger/Connector pair.  This implementation will be used by an {@link
     * InMemoryLedger} to emit all types of {@link LedgerEvent} to a single Connector.  In other words, every
     * connector that connects to this ledger will have one instances of this class constructed for it, with N
     * number of handlers found inside.
     * <p>
     * NOTE: Per the docs in {@link LedgerEventListener}, there is a one-to-one relationship between an instance of
     * this listener, a connector and a ledger (in this case, the single ledger in the in-memory ledger).
     * Registering instances of {@link LedgerEventHandler} that apply to more than one ledger or connector could
     * cause security issues because this implementation notifies all handlers on the assumption that it contains
     * handlers for only a single connector/ledger pair.
     */
    @Getter
    @RequiredArgsConstructor
    public static class InMemoryLedgerEventListener implements LedgerEventListener {

        // The ledger that this listener is listening to.
        @NonNull
        private final LedgerInfo ledgerInfo;

        // The connector that this listener is listening on behalf of.  This is necessary for the consumers of this listener to properly route events
        @NonNull
        private final ConnectorInfo connectorInfo;

        // This listener might have a single handler (for all events) or it might have a single handler for each event.
        // However, this always only connects a single ledger/connector pair.
        @NonNull
        private final Set<LedgerEventHandler> ledgerEventHandlers;

        /**
         * Helper Constructor.  Initializes the {@link List} of {@link LedgerEventHandler}'s to an empty list.
         *
         * @param ledgerInfo
         * @param connectorInfo
         */
        public InMemoryLedgerEventListener(final LedgerInfo ledgerInfo, final ConnectorInfo connectorInfo) {
            this.ledgerInfo = Objects.requireNonNull(ledgerInfo);
            this.connectorInfo = Objects.requireNonNull(connectorInfo);
            this.ledgerEventHandlers = new HashSet<>();
        }


        // TODO: consider returning a boolean to align with java.util.Collection - indicates if the handler already existed?
        @Override
        public void registerEventHandler(LedgerEventHandler<?> handler) {
            Preconditions.checkNotNull(handler);

            // Add the handler to the list...
            this.ledgerEventHandlers.add(handler);

            // Any handlers in this class should be notified, since they're all limited to a single connector.
            this.notifyEventHandlers(new LedgerConnectedEvent(this.getLedgerInfo()));
        }

        @Override
        public void unRegisterEventHandlers() {
            // TODO: Is this necessary?  There's a contradiction between a Connector being disconnected from a ledger and
            // a ledger trying to send the disconnected Connector an event that says the disconnect worked.
            //this.notifyEventHandlers(new LedgerDisonnectedEvent(this.getLedgerInfo(), this.getConnectorInfo()));

            // Remove all envet handlers...
            this.ledgerEventHandlers.clear();
        }

        @Override
        public void unRegisterEventHandler(LedgerEventHandler<?> handler) {
            // Remove the handler from the handler list...
            this.ledgerEventHandlers.remove(handler);
        }

        @Override
        public void notifyEventHandlers(final LedgerEvent ledgerEvent) {
            // Notify all handlers.  If this notify handler is getting called, it means there's an event for this ledger/connector combination.
            for (final LedgerEventHandler handler : this.ledgerEventHandlers) {
                handler.onLedgerEvent(ledgerEvent);
            }
        }
    }
}