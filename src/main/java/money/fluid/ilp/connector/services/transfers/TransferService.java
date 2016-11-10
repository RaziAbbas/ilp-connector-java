package money.fluid.ilp.connector.services.transfers;

/**
 * This interface defines a contract for initiating and completing ledger transfers, both for transfers that can be
 * serviced by this connector, as well as transfers that must be serviced by multiple connectors (i.e., a multi-hop
 * payment).
 * <p>
 * In ILP, a connector will facilitate an interledger payment upon receiving a notification for a transfer in which it
 * is credited.  That "source" transfer must have a ilp_header in its credit's memo that specifies the payment's
 * destination and amount.  As soon as the source transfer is prepared, the connector will authorize the debits from its
 * account(s) on the destination ledger.
 */
public interface TransferService {


    /**
     * Begin the process of initiating a transfer.  This includes the following steps:
     * <p>
     * <pre>
     *  <ol>
     *      <li>Create a local ledger transfer, including the cryptographic condition, and authorize this transfer on
     * the local ledger.</li>
     *      <li>Wait for the local ledger to put the sender's funds on hold and notify this connector that this has been
     * completed.</li>
     *      <li>Receive the notification from the Ledger, and extract the ILP packet to determine if the payment should
     * be forwarded.</li>
     *
     *  </ol>
     * </pre>
     */
    void initiateTransfer();


    void forwardPayment();

}
