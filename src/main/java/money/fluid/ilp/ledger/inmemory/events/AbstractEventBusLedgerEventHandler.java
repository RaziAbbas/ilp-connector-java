package money.fluid.ilp.ledger.inmemory.events;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.interledgerx.ilp.core.events.LedgerConnectedEvent;
import org.interledgerx.ilp.core.events.LedgerDirectTransferEvent;
import org.interledgerx.ilp.core.events.LedgerDisonnectedEvent;
import org.interledgerx.ilp.core.events.LedgerEvent;
import org.interledgerx.ilp.core.events.LedgerEventHandler;
import org.interledgerx.ilp.core.events.LedgerTransferExecutedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferPreparedEvent;
import org.interledgerx.ilp.core.events.LedgerTransferRejectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;


/**
 * An extension of {@link AbstractLedgerEventHandler} that implements {@link LedgerEventHandler} and uses Guava's {@link
 * EventBus} to route all ILP ledger events.  Note that per {@link LedgerEventHandler}, this implementation listens to a
 * single ledger and listens on behalf of a single connector.
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract class AbstractEventBusLedgerEventHandler extends AbstractLedgerEventHandler implements LedgerEventHandler<LedgerEvent> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final EventBus eventBus;

    /**
     * No-args Constructor.  Provides a default implementation of all dependencies.
     */
    public AbstractEventBusLedgerEventHandler() {
        this(new EventBus());
    }

    /**
     * Required-args Constructor.
     *
     * @param eventBus An instance of {@link EventBus} that can be custom-configured by the creator of this class.
     */
    public AbstractEventBusLedgerEventHandler(
            final EventBus eventBus
    ) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.eventBus.register(this);
    }

    /**
     * Handles a {@link LedgerEvent} in a type-safe fashion and accounts for unhandled events.
     *
     * @param ledgerEvent An instance of {@link LedgerEvent}.
     */
    @Override
    protected final void handleInternal(final LedgerEvent ledgerEvent) {
        Preconditions.checkNotNull(ledgerEvent);
        eventBus.post(ledgerEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerConnectedEvent ledgerConnectedEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerConnectedEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerDisonnectedEvent ledgerDisonnectedEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerDisonnectedEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerTransferPreparedEvent ledgerTransferPreparedEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerTransferPreparedEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerTransferExecutedEvent ledgerTransferExecutedEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerTransferExecutedEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerDirectTransferEvent ledgerDirectTransferEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerDirectTransferEvent);
    }

    @Subscribe
    private final void handleEventInternal(final LedgerTransferRejectedEvent ledgerTransferRejectedEvent) {
        // Forward to handleEventInternalHelper for logging purposes...
        this.handleEventInternalHelper((event) -> this.handleEvent(event), ledgerTransferRejectedEvent);
    }

    /**
     * Helper method that logs around the supplied {@link Consumer}.
     *
     * @param consumer
     * @param ledgerEvent
     * @param <T>
     */
    private final <T extends LedgerEvent> void handleEventInternalHelper(
            final Consumer<T> consumer, final T ledgerEvent
    ) {
        logger.info(
                "LedgerEventHandler[{}]: About to handle LedgerEvent '{}' for Connector[{}]",
                this.getSourceLedgerInfo().getLedgerId(),
                ledgerEvent,
                this.getListeningConnector().getConnectorInfo().getConnectorId()
        );
        consumer.accept(ledgerEvent);
        logger.info(
                "LedgerEventHandler[{}]: Handled LedgerEvent '{}' for Connector[{}]",
                this.getSourceLedgerInfo().getLedgerId(),
                ledgerEvent,
                this.getListeningConnector().getConnectorInfo().getConnectorId()
        );
    }

    @Subscribe
    protected void deadEvent(final DeadEvent deadEvent) {
        throw new RuntimeException("Unhandled Event: " + deadEvent);
    }
}
