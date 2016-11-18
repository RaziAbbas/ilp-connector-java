package money.fluid.ilp.ledgerclient;

import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.ledger.model.ConnectionInfo;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransfer;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;
import org.interledgerx.ilp.core.events.LedgerEventHandler;

/**
 * An interface that defines a ledger client, running in connector-space, that can operates on a single ledger on behalf
 * of the connector it is operating inside of.  This interface is conceptually similar to the Ledger interface found in
 * the "money.fluid.ilp.ledger" package, but defines its own behavior because a {@link LedgerClient} only operates on a
 * single Ledger->Connector connection, whereas a Ledger will operate with potentially many Connectors.
 * <p>
 * Additionally, this client will register with its connected {@link Ledger} as a notification listener, and pass those
 * notifications back to the Connector.
 * <p>
 * Note: A {@link LedgerClient} does not have the capability to create accounts on the ledger, debit/credit from the
 * ledger, or otherwise do things with a ledger that the ledger might expose to external clients like funding accounts,
 * etc.  Instead, this interface only facilitates ILP functionality.
 */
public interface LedgerClient {

    /**
     * Connector-supplied connection information used to connect to a Ledger.
     *
     * @return
     */
    ConnectionInfo getConnectionInfo();

    /**
     * Ledger-supplied meta-data about the ledger that this client is connected to.
     *
     * @return <code>LedgerInfo</code>
     */
    LedgerInfo getLedgerInfo();

    /**
     * Connect to the ledger that this client specifies.
     */
    void connect();

    /**
     * Disconnect from the ledger that this client specifies.
     */
    void disconnect();

    /**
     * Indicates if this {@link LedgerClient} is currently connected to a ledger.
     *
     * @return
     */
    boolean isConnected();

    /**
     * Initiates a ledger-local transfer to start an ILP transaction.
     *
     * @param transfer <code>LedgerTransfer</code>
     */
    void send(LedgerTransfer transfer);

    /**
     * Reject a transfer
     * <p>
     * This should only be allowed if the entity rejecting the transfer is the
     * receiver
     *
     * @param ilpTransactionId
     * @param reason
     */
    // TODO: It's unclear if we'll need an additional #rejectTransfer method that possibly accepts a LedgerEvent.
    void rejectTransfer(IlpTransactionId ilpTransactionId, LedgerTransferRejectedReason reason);

    /**
     * Submit a fulfillment to a ledger.
     * <p>
     * The ledger will execute all transfers that are fulfilled by this
     * fulfillment.
     *
     * @param fulfillment the fulfillment for this transfer
     */
    void fulfillCondition(Fulfillment fulfillment);

    /**
     * Submit an optimistic-mode fulfillment to a ledger.
     * <p>
     * The ledger will execute all transfers for the specified {@link IlpTransactionId} since the ILP transaction is an
     * optimistic-mode transfer, and no fulfillment is required.
     */
    void fulfillCondition(final IlpTransactionId ilpTransactionId);

    /**
     * Register an event handler to subscribe to events from this client, which come from a single Ledger.  This method
     * will either succeed, or throw a {@link RuntimeException} if registration fails for some unforseen reason.
     *
     * @param handler An instance of {@link LedgerEventHandler} that will handle events emitted from a {@link Ledger}.
     */
    void registerEventHandler(LedgerEventHandler<?> handler);
}
