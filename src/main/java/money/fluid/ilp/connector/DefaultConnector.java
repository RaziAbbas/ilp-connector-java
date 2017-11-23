package money.fluid.ilp.connector;

import java.util.Objects;
import java.util.Optional;

import javax.money.Monetary;

import org.interledgerx.ilp.core.DeliveredLedgerTransfer;
import org.interledgerx.ilp.core.ForwardedLedgerTransfer;
import org.interledgerx.ilp.core.IlpAddress;
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

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.model.ConnectorInfo;
import money.fluid.ilp.connector.services.ExchangeRateService;
import money.fluid.ilp.connector.services.ExchangeRateService.ExchangeRateInfo;
import money.fluid.ilp.connector.services.routing.Route;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.inmemory.events.AbstractEventBusLedgerEventHandler;
import money.fluid.ilp.ledger.inmemory.model.DeliveredLedgerTransferImpl;
import money.fluid.ilp.ledger.inmemory.model.ForwardedLedgerTransferImpl;
import money.fluid.ilp.ledger.inmemory.utils.MoneyUtils;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledger.model.NoteToSelf;
import money.fluid.ilp.ledgerclient.LedgerClient;

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
    
    private final ExchangeRateService exchangeRateService;

    public LedgerManager getLedgerManager() {
		return ledgerManager;
	}

    public ExchangeRateService getExchangeRateService() {
		return exchangeRateService;
	}
    
    public RoutingService getRoutingService() {
		return routingService;
	}

	public ConnectorInfo getConnectorInfo() {
		return connectorInfo;
	}

	/**
     * Required-args Constructor.  Allows for full DI support of all dependencies.
     *
     * @param connectorInfo
     * @param routingService
     * @param ledgerManager
     * @param exchangeRateService
     * @param connectorFeeService
     */
    public DefaultConnector(
            final ConnectorInfo connectorInfo,
            final RoutingService routingService,
            final LedgerManager ledgerManager,
            final ExchangeRateService exchangeRateService
    ) {
        this.connectorInfo = Objects.requireNonNull(connectorInfo);
        this.routingService = Objects.requireNonNull(routingService);
        this.ledgerManager = Objects.requireNonNull(ledgerManager);
        this.exchangeRateService = Objects.requireNonNull(exchangeRateService);

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
                    ledgerClient.registerEventHandler(new SimpleLedgerEventHandler(this, ledgerClient));
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
        private final LedgerClient sourceLedgerClient;

        /**
         * Required-args Constructor.
         *
         * @param listeningConnector
         * @param sourceLedgerClient
         */
        private SimpleLedgerEventHandler(
                final DefaultConnector listeningConnector,
                final LedgerClient sourceLedgerClient
        ) {
            this.listeningConnector = Objects.requireNonNull(listeningConnector);
            this.sourceLedgerClient = Objects.requireNonNull(sourceLedgerClient);
        }

        @Override
        protected void handleEvent(final LedgerConnectedEvent ledgerConnectedEvent) {
            // TODO: Audit this?
            logger.info("Received LedgerConnectedEvent {}", ledgerConnectedEvent);
        }

        @Override
        protected void handleEvent(final LedgerDisonnectedEvent ledgerDisonnectedEvent) {
            // TODO: Audit this?
            logger.info("Received LedgerDisonnectedEvent {}", ledgerDisonnectedEvent);
        }

        @Override
        protected void handleEvent(final LedgerDirectTransferEvent ledgerDirectTransferEvent) {
            logger.info("Received LedgerDirectTransferEvent {}", ledgerDirectTransferEvent);


            Preconditions.checkArgument(
                    ledgerDirectTransferEvent.getIlpPacketHeader().isOptimisticModeHeader(),
                    "Only Optimistic Mode transfers should utilize LedgerDirectTransferEvents!"
            );

            // TODO: implement fraud and velocity checks here, if possible.  If a given sender or ledger is behaving
            // badly, we don't want to suffer for it.

            if (ledgerDirectTransferEvent.getLocalSourceAddress().equals(
                    this.getSourceLedgerClient().getConnectionInfo().getLedgerAccountIlpAddress())) {
                // Audit here...
                logger.info(
                        "Acknowledged ledgerTransferPreparedEvent in response to a send(transfer) from {}: {}",
                        this.getListeningConnector().getConnectorInfo().getConnectorId(), ledgerDirectTransferEvent
                );
            } else {
                final IlpAddress destinationAddress = ledgerDirectTransferEvent.getIlpPacketHeader().getDestinationAddress();
                if (this.getListeningConnector().isTransferLocallyDeliverable(destinationAddress)) {

                    final LedgerId sourceLedgerId = ledgerDirectTransferEvent.getLedgerInfo().getLedgerId();
                    final DeliveredLedgerTransfer deliveredLedgerTransfer = this.constructDeliveredLedgerTransfer(
                            ledgerDirectTransferEvent);

                    this.getListeningConnector().getLedgerManager().deliverPayment(
                            // This is the ledgerId to notify in the event of a rejection, timeout, or fulfillment.  It's not
                            // the same as the ledger in the transfer (of type DeliveredLedgerTransfer).
                            sourceLedgerId,
                            deliveredLedgerTransfer
                    );
                } else if (this.getListeningConnector().isTransferRemotelyForwardable(destinationAddress)) {

                    // First, what's the best route to the destination address?  We rely on the routing table to let us know.
                    final Optional<Route> optRoute = this.listeningConnector.getRoutingService().bestHopForDestinationAmount(
                            ledgerDirectTransferEvent.getIlpPacketHeader().getDestinationAddress(),
                            ledgerDirectTransferEvent.getAmount()
                    );

                    if (optRoute.isPresent()) {
                        // Send a payment to the next-hop connector on a new ledger per the routing table...
                        final Route route = optRoute.get();

                        // The newSourceAccount should be this Connector's ledger-account on the new ledger from the route.
                        final LedgerId sourceLedgerId = route.getSourceAddress().getLedgerId();
                        final ForwardedLedgerTransfer transfer = constructForwardedLedgerTransfer(
                                ledgerDirectTransferEvent, route
                        );

                        this.getListeningConnector().getLedgerManager().forwardPayment(
                                // This is the ledgerId to notify in the event of a rejection, timeout, or fulfillment.  It's not
                                // the same as the ledger in the transfer.
                                sourceLedgerId,
                                transfer
                        );
                    } else {
                        // Reject the transfer due to no Route
                        throw new RuntimeException(String.format(
                                "No Route to Destination Ledger for LedgerDirectTransferEvent: %s",
                                ledgerDirectTransferEvent
                        ));
                    }
                } else {
                    // Reject the transfer due to no Route
                    throw new RuntimeException(String.format(
                            "No Route to Destination Ledger for LedgerDirectTransferEvent: %s",
                            ledgerDirectTransferEvent
                    ));
                }
            }
        }

        private DeliveredLedgerTransfer constructDeliveredLedgerTransfer(
                final LedgerDirectTransferEvent ledgerDirectTransferEvent
        ) {
            Objects.requireNonNull(ledgerDirectTransferEvent);

            // The source account of the new transfer should be the connector's account on the next-hop ledger.  Since
            // this is a "deliverable" payment, the local source account should be this Connector's account on the
            // destination ledger, which can be gleaned from the ILP destination address.
            final LedgerId ledgerId = ledgerDirectTransferEvent.getIlpPacketHeader().getDestinationAddress().getLedgerId();
            final IlpAddress localSourceAddress = this.getListeningConnector().getLedgerManager()
                    .getConnectorAccountOnLedger(ledgerId);
            // Since this is a "deliverable" payment, the next-hop destination account should be the destination
            // account in the ILP header.
            final IlpAddress localDestinationAddress = ledgerDirectTransferEvent.getIlpPacketHeader().getDestinationAddress();


            // 1) FX Differences
            final ExchangeRateInfo exchangeRateInfo = this.getListeningConnector().getExchangeRateService().getExchangeRate(
                    ledgerDirectTransferEvent.getAmount(),
                    Monetary.getCurrency(
                            this.getListeningConnector().getLedgerManager().findLedgerClient(
                                    localDestinationAddress.getLedgerId()).get().getLedgerInfo().getCurrencyCode()
                    )
            );

            // 2) Connector Fees (Part of the FX fee...)
//            final ConnectorFeeInfo connectorFeeInfo = this.getListeningConnector().getConnectorFeeService()
//                    .calculateConnectorFee(exchangeRateInfo.getDestinationAmount());

            // 3) Final Amount must be positive!
            Preconditions.checkArgument(
                    exchangeRateInfo.getDestinationAmount().isPositive(), "Amount after FX and fees must be Positive!");

            // Send a payment to the ILP destination address (the actual recipient) on the destination ledger...
            return new DeliveredLedgerTransferImpl(
                    ledgerDirectTransferEvent.getIlpPacketHeader(),
                    localSourceAddress,
                    localDestinationAddress,
                    exchangeRateInfo.getDestinationAmount(),
                    Optional.empty(),
                    Optional.empty()
            );
        }

        private ForwardedLedgerTransfer constructForwardedLedgerTransfer(
                final LedgerDirectTransferEvent ledgerDirectTransferEvent, final Route route
        ) {
            Objects.requireNonNull(ledgerDirectTransferEvent);
            Objects.requireNonNull(route);

            // Send a payment to the next-hop connector on a new ledger per the routing table...

            // The newSourceAccount should be this Connector's ledger-account on the new ledger from the route.
            final LedgerId ledgerId = route.getSourceAddress().getLedgerId();

            final IlpAddress ledgerLocalSourceAddress = this.getListeningConnector().getLedgerManager()
                    .getConnectorAccountOnLedger(ledgerId);


            // 1) FX Differences
            final ExchangeRateInfo exchangeRateInfo = this.getListeningConnector().getExchangeRateService().getExchangeRate(
                    ledgerDirectTransferEvent.getAmount(),
                    Monetary.getCurrency(
                            this.getListeningConnector().getLedgerManager().findLedgerClient(
                                    route.getSourceAddress().getLedgerId()).get().getLedgerInfo().getCurrencyCode()
                    )
            );

            // 2) Connector Fees (Part of the FX)

            // 3) Final Amount must be positive!
            Preconditions.checkArgument(
                    exchangeRateInfo.getDestinationAmount().isPositive(), "Amount after FX and fees must be Positive!");

            return new ForwardedLedgerTransferImpl(
                    ledgerDirectTransferEvent.getIlpPacketHeader(),
                    ledgerId,
                    ledgerLocalSourceAddress,
                    exchangeRateInfo.getDestinationAmount(),
                    Optional.empty(),
                    Optional.empty()
            );
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
         *
         * @param ledgerTransferPreparedEvent An instance of {@link LedgerTransferPreparedEvent}.
         * @return
         */
        @Override
        protected void handleEvent(final LedgerTransferPreparedEvent ledgerTransferPreparedEvent) {

            Preconditions.checkArgument(
                    ledgerTransferPreparedEvent.getIlpPacketHeader().isOptimisticModeHeader() == false,
                    "Optimistic mode transfers should not use conditional holds!"
            );
            // TODO: implement fraud and velocity checks here, if possible.  If a given sender or ledger is behaving
            // badly, we don't want to suffer for it.

            // If we receive a LedgerTransferPreparedEvent where this connector is the localSourceAddress, then this is
            // merely a notification that a payment this connector "sent" was successfully prepared.  For now, we do nothing
            // but possibly this could be audited.

            if (ledgerTransferPreparedEvent.getLocalSourceAddress().equals(
                    this.getSourceLedgerClient().getConnectionInfo().getLedgerAccountIlpAddress())) {
                // Audit here...
                logger.info(
                        "Acknowledged ledgerTransferPreparedEvent in response to a send(transfer) from {}: {}",
                        this.getListeningConnector().getConnectorInfo().getConnectorId(), ledgerTransferPreparedEvent
                );
            } else {
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
                    // Since this is a "deliverable" payment, the next-hop destination account should be the destination
                    // account in the ILP header.
                    final IlpAddress ledgerLocalDestinationAddress = ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress();

                    // Send a payment to the ILP destination address (the actual recipient) on the destination ledger...
                    final DeliveredLedgerTransfer transfer = new DeliveredLedgerTransferImpl(
                            ledgerTransferPreparedEvent.getIlpPacketHeader(),
                            ledgerLocalSourceAddress,
                            ledgerLocalDestinationAddress,
                            ledgerTransferPreparedEvent.getAmount(),
                            Optional.empty(),
                            Optional.empty()
                    );
                    this.getListeningConnector().getLedgerManager().deliverPayment(
                            // This is the ledgerId to notify in the event of a rejection, timeout, or fulfillment.  It's not
                            // the same as the ledger in the transfer (of type DeliveredLedgerTransfer).
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

                        final ForwardedLedgerTransfer<String, NoteToSelf> transfer = new ForwardedLedgerTransferImpl(
                                ledgerTransferPreparedEvent.getIlpPacketHeader(),
                                ledgerId,
                                ledgerLocalSourceAddress,
                                // TODO: Compute the amount!
                                MoneyUtils.zero("SND"),
                                Optional.empty(),
                                Optional.empty()
                                //.condition(ledgerTransferPreparedEvent.getOptCondition)
                                //.data(ledgerClient.getLedgerInfo())
                                // The source address on the next hop ledger that this Connector owns.
                                // TODO: The ledger will determine the destination account id to use for escrow, right?
                                // .destinationAddress(
                                // ledgerTransferPreparedEvent.getInterledgerPacketHeader().getLocalDestinationAddress())
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

            Preconditions.checkArgument(
                    ledgerTransferExecutedEvent.getIlpPacketHeader().isOptimisticModeHeader() == false,
                    "Optimistic mode transfers should not use conditional holds!"
            );


            // TODO: From the LedgerTransferEvent doc: "Ledger plugins MUST ensure that the data in the noteToSelf either isn't shared with any untrusted party or encrypted before it is shared."
            // If this value is signed via the fulfilment, then does it matter if it's encrypted?  In other words, what's secret about this information?
            // Perhaps the ILP Transaction Id?  If that's the case, then perhaps it would be preferable for the Ledger to store this data internally

            if (ledgerTransferExecutedEvent.getLocalDestinationAddress().equals(
                    this.getSourceLedgerClient().getConnectionInfo().getLedgerAccountIlpAddress())) {
                // Audit here...
                logger.info(
                        "Acknowledged ledgerTransferExecutedEvent in response to a fulfil from {}: {}",
                        this.getListeningConnector().getConnectorInfo().getConnectorId(), ledgerTransferExecutedEvent
                );
            } else {

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
                        // TODO: Add conditions here!
                        throw new RuntimeException("Not yet implemented!");


//                        optLedgerClient.get().fulfillCondition(
//                                ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId());
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
        }

        @Override
        protected void handleEvent(final LedgerTransferRejectedEvent ledgerTransferRejectedEvent) {

            Preconditions.checkArgument(
                    ledgerTransferRejectedEvent.getIlpPacketHeader().isOptimisticModeHeader() == false,
                    "Optimistic mode transfers should not use conditional holds!"
            );

            // TODO: From the LedgerTransferEvent doc: "Ledger plugins MUST ensure that the data in the noteToSelf either
            // isn't shared with any untrusted party or encrypted before it is shared."

            // If this value is signed via the fulfilment, then does it matter if it's encrypted?  In other words, what's
            // secret about this information?  Perhaps the ILP Transaction Id?  If that's the case, then perhaps it would
            // be preferable for the Ledger to store this data internally.

            if (ledgerTransferRejectedEvent.getLocalDestinationAddress().equals(
                    this.getSourceLedgerClient().getConnectionInfo().getLedgerAccountIlpAddress())) {
                // Audit here...
                logger.info(
                        "Acknowledged ledgerTransferRejectedEvent in response to a fulfil from {}: {}",
                        this.getListeningConnector().getConnectorInfo().getConnectorId(), ledgerTransferRejectedEvent
                );
            } else {
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

		@Override
		public Connector getListeningConnector() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LedgerClient getSourceLedgerClient() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}