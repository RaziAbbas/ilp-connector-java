package money.fluid.ilp.connector.services.ledgers.plugins;

import org.interledgerx.ilp.core.LedgerTransfer;

/**
 * A ledger abstraction interface for Interledger clients and connectors to communicate and route payments across
 * different ledger protocols.
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0004-ledger-plugin-interface/0004-ledger-plugin-interface.md"
 */
public interface LedgerPlugin {

    /**
     * Initiates a ledger-local transfer.
     *
     * @param transfer <code>LedgerTransfer</code>
     */
    void send(LedgerTransfer transfer);

    /**
     * Get the ledger plugin's ILP address prefix. This is used to determine whether a given ILP address is local to
     * this ledger plugin and thus can be reached using this plugin's send method.
     * <p>
     * Example Return Value: "us.fed.some-bank"
     *
     * @return
     */
    String getPrefix();

    /**
     * Get the ledger plugin's ILP address. This is given to senders to receive transfers to this account.
     * <p>
     * The mapping from the ILP address to the local ledger address is dependent on the ledger / ledger plugin. An ILP
     * address could be the <ledger prefix>.<account name or number>, or a token could be used in place of the actual
     * account name or number.
     * <p>
     * Example Return Value: "us.fed.some-bank.my-account"
     *
     * @return
     */
    String getAccount();

    // TODO: Implement the stuff below!


//    /**
//     * Returns true if and only if this ledger plugin can connect to the ledger described by the authentication data
//     * provided.
//     * <p>
//     * Ledger plugins are queried in precedence order and the first plugin that returns true for this method will be
//     * used to talk to the given ledger.
//     *
//     * @return
//     */
//    Boolean canConnect(String auth);
//
//    /**
//     * Initiate ledger event subscriptions.
//     * <p>
//     * Once connect is called the ledger plugin MUST attempt to subscribe to and report ledger events. Once the
//     * connection is established, the ledger plugin should emit the connect event. If the connection is lost, the ledger
//     * plugin SHOULD emit the disconnect event.
//     */
//    void connect();
//
//    void disconnect();
//
//    Boolean isConnected();
//
//    LedgerInfo getLedgerInfo();
//
//    MonetaryAmount getBalance();
//
//    Collection<String> getConnectors();
//
//    void send();
//
//    /**
//     * Submit a fulfillment to a ledger. The ledger plugin or the ledger MUST automatically detect whether the
//     * fulfillment is an execution or cancellation condition fulfillment.
//     */
//    void fulfillCondition(TransferId transferId, String fulfillment);
//
//    /**
//     * @param transferId
//     * @param replyMessage
//     */
//    void replyToTransfer(TransferId transferId, String replyMessage);

//    /**
//     * Plugin options are passed in to the LedgerPlugin constructor when a plugin is being instantiated.
//     */
//    interface PluginOptions {
//        /**
//         * A JSON object that encapsulates authentication information and ledger properties. The format of this object
//         * is specific to each ledger plugin.
//         * <p>
//         * For example:
//         * <pre>
//         * {
//         *  "account": "https://red.ilpdemo.org/ledger/accounts/alice",
//         *  "password": "alice"
//         * }
//         * </pre>
//         */
//        String getAuth();
//
//        /**
//         * Provides callback hooks to the host's persistence layer.
//         * <p>
//         * Persistence MAY be required for internal use by some ledger plugins. For this purpose hosts MAY be configured
//         * with a persistence layer.
//         * <p>
//         * Method names are based on the popular LevelUP/LevelDOWN packages.
//         */
//        Optional<Store> getStore();
//    }
//
//
//    /**
//     * An interface for handling ILP events.
//     */
//    interface EventHandler {
//
//        /**
//         * Handle the event {@code EventType#CONNECT}.
//         *
//         * @param event       An instance of {@link Event} that contains an instance of type {@link EventType}.
//         * @param <EventType>
//         */
//        <EventType> void onConnect(Event<EventType> event);
//
//        /**
//         * Handle the event {@code EventType#DISCONNECT}.
//         *
//         * @param event       An instance of {@link Event} that contains an instance of type {@link EventType}.
//         * @param <EventType>
//         */
//        <EventType> void onDisconnect(Event<EventType> event);
//
//        /**
//         * Helper method to set the handler for "connect" events.
//         *
//         * @param c An instance of {@link Consumer}.
//         */
//        void setConnectHandler(Consumer<Void> c);
//
//        /**
//         * Helper method to set the handler for "connect" events.
//         *
//         * @param c An instance of {@link Consumer}.
//         */
//        void setDisconnectHandler(Consumer<Void> c);
//    }
//
//    enum EventType {
//
//        CONNECT("event.connect"),
//
//        DISCONNECT("event.disconnect"),
//
//        ERROR("event.error");
//
//        @Getter
//        private final String value;
//
//        EventType(final String value) {
//            this.value = value;
//        }
//
//        @Override
//        public String toString() {
//            return this.value;
//        }
//    }
}

