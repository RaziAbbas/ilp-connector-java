package money.fluid.ilp.connector;

import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.services.routing.Route;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.inmemory.events.AbstractEventBusLedgerEventHandler;
import money.fluid.ilp.ledger.inmemory.model.DefaultLedgerTransfer;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledger.model.NoteToSelf;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransfer;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A default implementation of an ILP {@link Connector}.
 */
@Getter
@ToString
public class DefaultConnector implements Connector {
    private final ConnectorInfo connectorInfo;

    // Specified at Connector initialization time, these are all ledgers that this Connector can connect to...required in
    // order for a Connector to move money along a given Route.
    private final Set<LedgerClient> ledgerClients;

    private final RoutingService routingService;

    //The Connector needs to track the pending transfers, not the event handlers.  This is because event handler1 will
    // receive a transfer for 1 ledger and make another transfer on another ledger.  When the transfer executes, the 2nd
    // handler won't have the originating ledger's info, so it gets it from here.
    // TODO: Consider holding the entire transfer here, or loading-cache that is backed by a Database.
    // TODO: This should be backed by a datastore since these transfers should not be lost until they are expired or fulfilled.
    private final Map<IlpTransactionId, NoteToSelf> pendingTransfers;

    /**
     * Required-args Constructor.  Allows for full DI support of all dependencies.
     *
     * @param connectorInfo
     * @param ledgerClients
     * @param routingService
     */
    public DefaultConnector(
            final ConnectorInfo connectorInfo,
            final Set<LedgerClient> ledgerClients,
            final RoutingService routingService
    ) {
        this.connectorInfo = Objects.requireNonNull(connectorInfo);
        this.ledgerClients = Objects.requireNonNull(ledgerClients);
        this.routingService = Objects.requireNonNull(routingService);

        this.pendingTransfers = new HashMap<>();

        this.initialize();
    }

    /**
     * Initialize this Connector by trying to connect to the ledgers in question.
     */
    private void initialize() {
        // The Connector has a set of Ledgers that are pre-configured.  This initializer registers a LedgerEventHandler
        // with each one.  Alternative implementations might register N event handlers per Connector (e.g., a handler to
        // handle each event instead of a single handler that handles all events).
        this.ledgerClients.stream().forEach(
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
        this.ledgerClients.stream().forEach((LedgerClient::disconnect));
    }

    /**
     * Helper method to determine which ledger client should be used for a given {@link IlpAddress}.
     *
     * @param ledgerId
     * @return
     */
    public Optional<LedgerClient> findLedgerClient(final LedgerId ledgerId) {
        Objects.requireNonNull(ledgerId);
        return this.getLedgerClients().stream()
                .filter(ledgerClient -> ledgerClient.getLedgerInfo().getLedgerId().equals(ledgerId))
                .findFirst();
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
         * it seems like this should // never be used, and instead we should always use escrow, even for optimistic
         * transactions, so that a recipient // who rejects a payment doesn't have to actually send money back to a
         * sender.
         */
        @Override
        @Deprecated
        protected void handleEvent(final LedgerDirectTransferEvent ledgerDirectTransferEvent) {
            this.getListeningConnector().getPendingTransfers().remove(
                    ledgerDirectTransferEvent.getIlpPacketHeader().getIlpTransactionId()
            );
        }

        /**
         * When a transfer has been prepared on a particular ledger, it means that some payor (a person or a connector,
         * e.g.) has put money on-hold in the ledger that this listener is in charge of monitoring on behalf of the
         * connector that this handler operates.  In other words, it means this Connector has funds that are in-escrow,
         * waiting to be captured by it.  In order to capture those funds, this connector must "forward" another
         * corresponding payment in the "next-hop" ledger, in hopes of fulfilling the ILP protocol so that this ledger's
         * escrow releases the funds on hold waiting for this connector.
         * <p>
         * The connector associated with this handler must also store, for later rollback or execution, a record of the
         * next-hop transfer for auditing purposes, as well as update its advertised routing tables to reflect the
         * reduction in liquidity that processing this event entails.
         *
         * @param ledgerTransferPreparedEvent An instance of {@link LedgerTransferPreparedEvent}.
         * @return
         */
        @Override
        protected void handleEvent(final LedgerTransferPreparedEvent ledgerTransferPreparedEvent) {

            // TODO: implement fraud and velocity checks here, if possible.  If a given sender or ledger is behaving
            // badly, we don't want to suffer for it.

            // First, what's the best route to the destination address?  We rely on the routing table to let us know.
            final Optional<Route> optRoute = this.listeningConnector.getRoutingService().bestHopForDestinationAmount(
                    ledgerTransferPreparedEvent.getIlpPacketHeader().getDestinationAddress(),
                    ledgerTransferPreparedEvent.getAmount()
            );

            if (optRoute.isPresent()) {
                final Route route = optRoute.get();

                // TODO: Consider putting getConnectorLedger(ledgerId) into the connector, and return an optional.
                final LedgerClient ledgerClient = this.listeningConnector.getLedgerClient(
                        route.getDestinationAddress().getLedgerId()
                ).orElseThrow(() -> new RuntimeException(
                        "Routing table had a route but Connector has no DefaultLedgerClient!")
                );

                // TODO: Is this correct?  The connector is saying to escrow money locally from itself to a local recipient,
                // but it's unclear who that recipient is.  Thus, for this transfer the connector leaves it to the Ledger
                // to determine the recipient based upon its own business rules.
                // Create a new LedgerTransfer for the next hop, and send it to that
                final LedgerTransfer transfer = new DefaultLedgerTransfer(
                        ledgerTransferPreparedEvent.getIlpPacketHeader(),

                        // TODO: Should this come from the routing table, or should the routing table return a ledgerId,
                        // and then _this_ connector can determine what it's ledgerId is on that ledger?
                        route.getSourceAddress(),

                        // The connector doesn't actually know who the local destination account is.  Thus, it's empty,
                        // but perhaps it shouldn't even exist?
                        //Optional.ofNullable(route.getDestinationAddress()),
                        Optional.empty(),
                        ledgerTransferPreparedEvent.getAmount(),
                        Optional.empty(),
                        Optional.empty()
                        //.condition(ledgerTransferPreparedEvent.getOptCondition)
                        //.data(ledgerClient.getLedgerInfo())
                        // The source address on the next hop ledger that this Connector owns.
                        // TODO: The ledger will determine the destination account id to use for escrow, right?
                        // .destinationAddress(
                        // ledgerTransferPreparedEvent.getIlpPacketHeader().getLocalDestinationAddress())
                );

                ///////////////////////
                // Instruct the Ledger to send the transfer.
                ///////////////////////

                // TODO: This will put some funds on hold in that ledger, so we need to update our routing tables potentially.
                // TODO: Handle events idempotently.  In other words, don't process the same transfer twice in an unsafe fashion.

                // TODO: Depending on how this is used, we might just store the LedgerId of the source ledger for this transfer.
                // Track the transfer for later...
                final NoteToSelf noteToSelf = NoteToSelf.builder().originatingLedgerId(
                        ledgerTransferPreparedEvent.getLedgerInfo().getLedgerId()).build();

                // TODO: Consider making pending transfers part of the ConnectorInterface -- not the actual map access,
                // but methods to add and remove...
                this.getListeningConnector().getPendingTransfers().put(
                        ledgerTransferPreparedEvent.getIlpPacketHeader().getIlpTransactionId(),
                        noteToSelf
                );

                ledgerClient.send(transfer);
            } else {
                throw new RuntimeException("Implement Me!");
            }

            // For now, if the prepared transfer can't be routed by this connected, then the connector does nothing.
            // The payment will be expired after its widow by the ledger.
            // TODO: Query the list or js code to determine what should happen here...
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
            final LedgerId originatingLedgerId = Optional.ofNullable(
                    this.getListeningConnector().getPendingTransfers().get(
                            ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId())
            )
                    .map(noteToSelf -> noteToSelf.getOriginatingLedgerId())
                    .orElseThrow(() -> new RuntimeException(
                            "No pending transfer existed to determine which ledger to pass executed fulfilment back to!"));


            // This Connector needs to send the fufillment condition back to the ledger that originally triggered the ILP
            // transaction in the first place.
            final Optional<LedgerClient> optLedgerClient = this.getListeningConnector().findLedgerClient(
                    originatingLedgerId);
            if (optLedgerClient.isPresent()) {
                optLedgerClient.get().fulfillCondition(
                        ledgerTransferExecutedEvent.getIlpPacketHeader().getIlpTransactionId());
            } else {
                logger.warn("No LedgerClient existed for LedgerId: {}", originatingLedgerId);
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
            final LedgerId originatingLedgerId =
                    Optional.ofNullable(this.getListeningConnector().getPendingTransfers().get(
                            ledgerTransferRejectedEvent.getIlpPacketHeader().getIlpTransactionId())
                    )
                            .map(noteToSelf -> noteToSelf.getOriginatingLedgerId())
                            .orElseThrow(() -> new RuntimeException(
                                    "No pending transfer existed to determine which ledger to pass rejected fulfilment back to!"));

            // This Connector needs to send the rejection back to the ledger that originally triggered the ILP
            // transaction in the first place.
            final Optional<LedgerClient> optLedgerClient = this.getListeningConnector().findLedgerClient(
                    originatingLedgerId);
            if (optLedgerClient.isPresent()) {
                optLedgerClient.get().rejectTransfer(
                        ledgerTransferRejectedEvent.getIlpPacketHeader().getIlpTransactionId(),
                        LedgerTransferRejectedReason.REJECTED_BY_RECEIVER
                );
            } else {
                logger.warn("No LedgerClient existed for LedgerId: {}", originatingLedgerId);
            }
        }
    }
}