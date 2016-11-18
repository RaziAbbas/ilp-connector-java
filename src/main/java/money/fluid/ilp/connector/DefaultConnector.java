package money.fluid.ilp.connector;

import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.services.routing.Route;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.inmemory.events.AbstractEventBusLedgerEventHandler;
import money.fluid.ilp.ledger.inmemory.model.DeliveredLedgerTransferImpl;
import money.fluid.ilp.ledger.inmemory.model.ForwardedLedgerTransferImpl;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.DeliveredLedgerTransfer;
import org.interledgerx.ilp.core.ForwardedLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;
import org.interledgerx.ilp.core.events.LedgerConnectedEvent;
import org.interledgerx.ilp.core.events.LedgerDirectTransferEvent;
import org.interledgerx.ilp.core.events.LedgerDisonnectedEvent;
import org.interledgerx.ilp.core.events.LedgerEventHandler;
import org.interledgerx.ilp.core.events.LedgerTransferExecutedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferPreparedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferRejectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * A default implementation of an Interledger {@link Connector}.
 *
 * @see "http://www.interledger.org"
 */
@Getter
@ToString
public class DefaultConnector implements Connector {
    private final ConnectorInfo connectorInfo;

    private final RoutingService routingService;

    private final LedgerManager ledgerManager;

    /**
     * Required-args Constructor.  Allows for full DI support of all dependencies.
     *
     * @param connectorInfo
     * @param routingService
     * @param ledgerManager
     */
    public DefaultConnector(
            final ConnectorInfo connectorInfo,
            final RoutingService routingService,
            final LedgerManager ledgerManager
    ) {
        this.connectorInfo = Objects.requireNonNull(connectorInfo);
        this.routingService = Objects.requireNonNull(routingService);
        this.ledgerManager = Objects.requireNonNull(ledgerManager);

        this.initialize();
    }

    /**
     * Initialize this Connector by trying to connect to the ledgers in question.
     */
    private void initialize() {
        // The Connector has a set of Ledgers that are pre-configured.  This initializer registers a LedgerEventHandler
        // with each one.  Alternative implementations might register N event handlers per Connector (e.g., a handler to
        // handle each event instead of a single handler that handles all events).
        this.getLedgerManager().getLedgerClients().stream().forEach(
                ledgerClient -> {
                    // Establish a connection first...
                    ledgerClient.connect();
                    // Register an event handler second, after the connection, so that the ledger is aware
                    ledgerClient.registerEventHandler(new SimpleLedgerEventHandler(this, ledgerClient.getLedgerInfo()));
                });
    }

    @Override
    public void shutdown() {
        // Shutdown any connections...
        this.getLedgerManager().getLedgerClients().stream().forEach((LedgerClient::disconnect));
    }

    /**
     * Determines if a given {@link IlpAddress} has a routing table entry that names a local ledger.  In other words,
     * _this_ connector has direct access to the destination address's ledger.
     *
     * @param destinationAddress An instance of {@link IlpAddress} representing the ultimate destination of an ILP
     *                           transfer.
     * @return {@code true} if {@link Route#getDestinationAddress()} exists in a ledger that this Connector has an
     * actively connected LedgerClient for; {@code false} otherwise.
     * @see "https://github.com/interledger/rfcs/issues/77"
     */
    public boolean isTransferLocallyDeliverable(final IlpAddress destinationAddress) {
        Objects.requireNonNull(destinationAddress);
        return this.getLedgerManager().findLedgerClient(destinationAddress.getLedgerId()).isPresent();
    }

    /**
     * Determines if a given {@link IlpAddress} has a routing table entry that names another connector.  In other words,
     * _this_ connector has no direct access to the destination address's ledger.
     *
     * @param destinationAddress An instance of {@link IlpAddress} representing the ultimate destination of an ILP
     *                           transfer.
     * @return {@code true} if {@code destinationAddress} exists in a route that this Connector has an actively
     * connected LedgerClient for; {@code false} otherwise.
     * @see "https://github.com/interledger/rfcs/issues/77"
     */
    public boolean isTransferRemotelyForwardable(final IlpAddress destinationAddress) {
        Objects.requireNonNull(destinationAddress);
        return this.getRoutingService().bestHopForDestinationAmount(destinationAddress).isPresent();
    }

    /**
     * An implementation of {@link LedgerEventHandler} that connects the in-memory ledger to the Connector.
     */
    @Getter
    private static class SimpleLedgerEventHandler extends AbstractEventBusLedgerEventHandler {
        private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

        private final DefaultConnector listeningConnector;

        // For a given handler, there is only one ledger client because each concrete handler implementation only listens for events from a single ledger.
        private final LedgerInfo sourceLedgerInfo;

        /**
         * Required-args Constructor.
         *
         * @param listeningConnector
         * @param sourceLedgerInfo
         */
        private SimpleLedgerEventHandler(
                final DefaultConnector listeningConnector,
                final LedgerInfo sourceLedgerInfo
        ) {
            this.listeningConnector = Objects.requireNonNull(listeningConnector);
            this.sourceLedgerInfo = Objects.requireNonNull(sourceLedgerInfo);
        }

        @Override
        protected void handleEvent(final LedgerConnectedEvent ledgerConnectedEvent) {
// TODO: Do something here with the Connector?  We've actually gotten called-back from the Ledger,
            // so maybe at least audit this?
            //this.getListeningConnectorId()
        }

        @Override
        protected void handleEvent(final LedgerDisonnectedEvent ledgerDisonnectedEvent) {
            // TODO: Settle or abort any escrows?
        }

        /**
         * @param ledgerDirectTransferEvent An instance of {@link LedgerDirectTransferEvent}.
         * @deprecated TODO: It seems like this event is intended to be used when no ILP escrow is required.  However,
         * it seems like this should never be used, and instead we should always use escrow, even for optimistic
         * transactions, so that a recipient who rejects a payment doesn't have to actually send money back to a
         * sender.
         */
        @Override
        @Deprecated
        protected void handleEvent(final LedgerDirectTransferEvent ledgerDirectTransferEvent) {
//            this.getListeningConnector().getPendingTransfers().remove(
//                    ledgerDirectTransferEvent.getIlpPacketHeader().getIlpTransactionId()
//            );
        }

        /**
         * When a transfer has been prepared on a particular ledger, it means that some payor (a person or a connector,
         * e.g.) has put money on-hold in the ledger that this listener is in charge of monitoring on behalf of the
         * connector that this handler operates.  In other words, it means this Connector has funds that are in-escrow,
         * waiting to be captured by it.
         * <p>
         * In order to capture those funds, this connector must "deliver" another corresponding payment in the
         * "next-hop" ledger, in hopes of fulfilling the ILP protocol so that this ledger's escrow releases the funds on
         * hold waiting for this connector.
         * <p>
         * The Connector (associated with this {@link LedgerEventHandler}) must also store, for later rollback or
         * execution, a record of the next-hop transfer for auditing purposes, as well as to update its advertised
         * routing tables to reflect the reduction in liquidity that processing this ILP transfer entails.
         *
         * @param ledgerTransferPreparedEvent An instance of {@link LedgerTransferPreparedEvent}.
         * @return
         */
        @Override
        protected void handleEvent(final LedgerTransferPreparedEvent ledgerTransferPreparedEvent) {

            // TODO: implement fraud and velocity checks here, if possible.  If a given sender or ledger is behaving
            // badly, we don't want to suffer for it.

            final IlpAddress destinationAddress = ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress();
            if (this.getListeningConnector().isTransferLocallyDeliverable(destinationAddress)) {

                ///////////////////////////////
                // Deliver the Transfer Payment via a new Transfer
                ///////////////////////////////
                // The source account of the new transfer should be the connector's account on the next-hop ledger.  Since
                // this is a "deliverable" payment, the local source account should be this Connector's account on the
                // destination ledger, which can be gleaned from the ILP destination address.
                final LedgerId ledgerId = ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress().getLedgerId();
                final IlpAddress ledgerLocalSourceAddress = this.getListeningConnector().getLedgerManager()
                        .getConnectorAccountOnLedger(ledgerId);
                // Since this is a "deliverable" payment, the next-hopt destination account should be the destination
                // account in the ILP header.
                final IlpAddress ledgerLocalDestinationAddress = ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress();

                // Send a payment to the ILP destination address (the actual recipient) on the destination ledger...
                final DeliveredLedgerTransfer transfer = new DeliveredLedgerTransferImpl(
                        ledgerTransferPreparedEvent.getIlpPacketHeader(),
                        ledgerId,
                        ledgerLocalSourceAddress,
                        ledgerLocalDestinationAddress,
                        Optional.empty(),
                        Optional.empty()
                );
                this.getListeningConnector().getLedgerManager().deliverPayment(
                        // This is the ledgerId to notify in the event of a rejection, timeout, or fulfillment.  It's not
                        // the same as the ledger in the transfer.
                        ledgerTransferPreparedEvent.getLedgerInfo().getLedgerId(),
                        transfer
                );
            } else if (this.getListeningConnector().isTransferRemotelyForwardable(destinationAddress)) {
                // First, what's the best route to the destination address?  We rely on the routing table to let us know.
                final Optional<Route> optRoute = this.listeningConnector.getRoutingService().bestHopForDestinationAmount(
                        ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress(),
                        ledgerTransferPreparedEvent.getAmount()
                );

                if (optRoute.isPresent()) {
                    final Route route = optRoute.get();

                    // Send a payment to the next-hop connector on a new ledger per the routing table...

                    // The newSourceAccount should be this Connector's ledger-account on the new ledger from the route.
                    final LedgerId ledgerId = route.getSourceAddress().getLedgerId();

                    final IlpAddress ledgerLocalSourceAddress = this.getListeningConnector().getLedgerManager()
                            .getConnectorAccountOnLedger(ledgerId);

                    final ForwardedLedgerTransfer transfer = new ForwardedLedgerTransferImpl(
                            ledgerTransferPreparedEvent.getIlpPacketHeader(),
                            ledgerId,
                            ledgerLocalSourceAddress
                            //.condition(ledgerTransferPreparedEvent.getOptCondition)
                            //.data(ledgerClient.getLedgerInfo())
                            // The source address on the next hop ledger that this Connector owns.
                            // TODO: The ledger will determine the destination account id to use for escrow, right?
                            // .destinationAddress(
                            // ledgerTransferPreparedEvent.getIlpPacketHeader().getLocalDestinationAddress())
                    );

                    this.getListeningConnector().getLedgerManager().forwardPayment(
                            // This is the ledgerId to notify in the event of a rejection, timeout, or fulfillment.  It's not
                            // the same as the ledger in the transfer.
                            ledgerTransferPreparedEvent.getLedgerInfo().getLedgerId(),
                            transfer
                    );
                } else {
                    // Reject the transfer due to no Route (which is comparable to not having a ledger).
                    //final LedgerClient ledgerClient = this.getListeningConnector().findLedgerClientSafely(
                    //       ledgerTransferPreparedEvent.getLedgerInfo().getLedgerId());
                    this.getListeningConnector().getLedgerManager().rejectPayment(
                            ledgerTransferPreparedEvent.getIlpPacketHeader().getIlpTransactionId(),
                            LedgerTransferRejectedReason.NO_ROUTE_TO_LEDGER
                    );
                }
            } else {
                // Reject the transfer due to no Ledger Connection...
                this.getListeningConnector().getLedgerManager().rejectPayment(
                        ledgerTransferPreparedEvent.getIlpPacketHeader().getIlpTransactionId(),
                        LedgerTransferRejectedReason.NO_ROUTE_TO_LEDGER
                );
            }
        }

        /**
         * When a particular ledger (in this case, the destination ledger) indicates that a transfer has been executed,
         * it means that the recipient of funds has accepted the transfer and that the fulfilled condition can be
         * presented back down the chain to originating ledgers so that escrow can be executed.  Since ILP is a
         * recursive protocol, the only thing this Connector needs to do is pass the fulfilment back to the original
         * ledger that triggered the ILP transaction in the first place (i.e., the first-hop ledger).  To do this, the
         * Connector tracks all pending transfers, and this can be queried to determine which ledger to pass the
         * fulfilment to.
         *
         * @param ledgerTransferExecutedEvent An instance of {@link LedgerTransferExecutedEvent}.
         * @return
         */
        @Override
        protected void handleEvent(final LedgerTransferExecutedEvent ledgerTransferExecutedEvent) {

            // TODO: From the LedgerTransferEvent doc: "Ledger plugins MUST ensure that the data in the noteToSelf either isn't shared with any untrusted party or encrypted before it is shared."
            // If this value is signed via the fulfilment, then does it matter if it's encrypted?  In other words, what's secret about this information?
            // Perhaps the ILP Transaction Id?  If that's the case, then perhaps it would be preferable for the Ledger to store this data internally

            // The ledger that this executed transfer came from, so we can pass the fulfillment back.
            final Optional<LedgerId> optOriginatingLedgerId = this.getListeningConnector().getLedgerManager()
                    .getOriginatingLedgerId(
                            ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId()
                    );

            if (optOriginatingLedgerId.isPresent()) {
                final LedgerId originatingLedgerId = optOriginatingLedgerId.get();

                // This Connector needs to send the fufillment condition back to the ledger that originally triggered
                // the ILP transaction in the first place.
                final Optional<LedgerClient> optLedgerClient = this.getListeningConnector().getLedgerManager()
                        .findLedgerClient(originatingLedgerId);
                if (optLedgerClient.isPresent()) {
                    optLedgerClient.get().fulfillCondition(
                            ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId());
                } else {
                    logger.warn("No LedgerClient existed for LedgerId: {}", originatingLedgerId);
                }
            } else {
                logger.error(
                        "No originating LedgerId to execute for ILP Transaction {}!",
                        ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId()
                );
            }
        }

        @Override
        protected void handleEvent(final LedgerTransferRejectedEvent ledgerTransferRejectedEvent) {
            // TODO: From the LedgerTransferEvent doc: "Ledger plugins MUST ensure that the data in the noteToSelf either
            // isn't shared with any untrusted party or encrypted before it is shared."

            // If this value is signed via the fulfilment, then does it matter if it's encrypted?  In other words, what's
            // secret about this information?  Perhaps the ILP Transaction Id?  If that's the case, then perhaps it would
            // be preferable for the Ledger to store this data internally.

            // The ledger that this executed transfer came from, so we can pass the fulfillment back.
            final Optional<LedgerId> optOriginatingLedgerId = this.getListeningConnector().getLedgerManager()
                    .getOriginatingLedgerId(
                            ledgerTransferRejectedEvent.getIlpPacketHeader().getIlpTransactionId()
                    );

            if (optOriginatingLedgerId.isPresent()) {
                final LedgerId originatingLedgerId = optOriginatingLedgerId.get();

                // This Connector needs to send the rejection back to the ledger that originally triggered the ILP
                // transaction in the first place.
                final Optional<LedgerClient> optLedgerClient = this.getListeningConnector().getLedgerManager()
                        .findLedgerClient(originatingLedgerId);

                if (optLedgerClient.isPresent()) {
                    optLedgerClient.get().rejectTransfer(
                            ledgerTransferRejectedEvent.getIlpPacketHeader().getIlpTransactionId(),
                            LedgerTransferRejectedReason.REJECTED_BY_RECEIVER
                    );
                } else {
                    logger.warn("No LedgerClient existed for LedgerId: {}", originatingLedgerId);
                }
            } else {
                logger.error(
                        "No originating LedgerId to execute for ILP Transaction {}!",
                        ledgerTransferRejectedEvent.getIlpPacketHeader().getIlpTransactionId()
                );
            }
        }
    }
}