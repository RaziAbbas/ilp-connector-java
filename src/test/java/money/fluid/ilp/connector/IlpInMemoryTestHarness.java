package money.fluid.ilp.connector;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.FakeTicker;
import money.fluid.ilp.connector.managers.ledgers.DefaultLedgerManager;
import money.fluid.ilp.connector.managers.ledgers.InMemoryPendingTransferManager;
import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.connector.services.routing.DefaultRoute;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.QuotingService;
import money.fluid.ilp.ledger.inmemory.InMemoryLedger;
import money.fluid.ilp.ledger.inmemory.model.DefaultLedgerInfo;
import money.fluid.ilp.ledger.inmemory.model.InitialLedgerTransferImpl;
import money.fluid.ilp.ledger.model.ConnectionInfo;
import money.fluid.ilp.ledger.model.ConnectorInfo;
import money.fluid.ilp.ledger.model.LedgerAccount;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.InMemoryLedgerClient;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransfer;
import org.interledgerx.ilp.core.LedgerTransferRejectedReason;
import org.javamoney.moneta.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * This class is meant to be run using JUnit to simulate an ILP Connector and some of its interactions with an in-memory
 * ledger.
 * <p>
 * The primary examples this harness explores are as follows:
 * <p>
 * <pre>
 *     <ul>
 *         <li>Scenario 1: An ILP payment from a sender/recipient pair on the same ledger.</li>
 *         <li>Scenario 2: An ILP payment from a sender/recipient pair on different ledgers serviced by a single
 * connector where the ledgers are denominated in the same asset type (e.g., two different ledgers that both deal in
 * Sand (i.e., SND).  This scenario is similar to scenario 3, but does not involve exchange-rate fluctuations or
 * risk.</li>
 *         <li>Scenario 3: An ILP payment from a sender/recipient pair on different ledgers serviced by a single
 * connector where the sender specifies the source amount.</li>
 *          <li>Scenario 4: An ILP payment from a sender/recipient pair on different ledgers serviced by a single
 * connector where the sender specifies the destination amount.</li>
 *         <li>Scenario 5: An ILP payment from a sender/recipient pair on different ledgers serviced by different
 * connectors where the sender specifies the source amount.</li>
 *          <li>Scenario 6: An ILP payment from a sender/recipient pair on different ledgers serviced by different
 * connectors where the sender specifies the destination amount.</li>
 *     </ul>
 * </pre>
 * <p>
 * NOTE: This class is only useful for development purposes, and is not meant to be normative or proscriptive.  Also,
 * see comments below for more test examples that will be necessary.
 */
public class IlpInMemoryTestHarness {

    private static final long DEFAULT_LEDGER_TIMEOUT = 5L;

    // The currency code for the "Sand" ledger.  Denominated in granules of sand.
    private static final String SND = "SND";
    private static final String SAND_CURRENCY_SYMBOL = "(S)";

    // The currency code for the "Dirt" ledger.  Denominated in granules of sand.
    private static final String DRT = "DRT";
    private static final String DIRT_CURRENCY_SYMBOL = "(D)";

    // This is the unique account identifier for the connector.  For simplicity, it is the same on each in-memory ledger
    // for purposes of this test.  In reality, this account identifier would be a configuration option of the connector
    // on a per-ledger basis.
    private static final LedgerAccountId CONNECTOR1 = LedgerAccountId.of("fluid-connector-1");
    private static final LedgerAccountId CONNECTOR2 = LedgerAccountId.of("fluid-connector-2");

    private static final LedgerAccountId ALICE = LedgerAccountId.of("alice");
    private static final LedgerAccountId BOB = LedgerAccountId.of("bob");
    private static final LedgerAccountId CHLOE = LedgerAccountId.of("chloe");
    private static final LedgerAccountId ESCROW = LedgerAccountId.of("__escrow__");

    private static final LedgerId SAND_LEDGER1 = LedgerId.of("sand-ledger1.example.com");
    private static final LedgerId SAND_LEDGER2 = LedgerId.of("sand-ledger2.example.com");
    private static final LedgerId SAND_LEDGER3 = LedgerId.of("sand-ledger3.example.com");
    private static final LedgerId DIRT_LEDGER1 = LedgerId.of("dirt-ledger1.example.com");
    private static final LedgerId DIRT_LEDGER2 = LedgerId.of("dirt-ledger2.example.com");
    private static final String INITIAL_AMOUNT = "500";
    private static final String ZERO_AMOUNT = "0";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Mock
    private RoutingService connector1RoutingServiceMock;
    @Mock
    private RoutingService connector2RoutingServiceMock;

    @Mock
    private QuotingService sandLedger1QuotingServiceMock;

    @Mock
    private QuotingService sandLedger2QuotingServiceMock;

    @Mock
    private QuotingService sandLedger3QuotingServiceMock;

    @Mock
    private QuotingService dirtLedger1QuotingServiceMock;

    //////////
    // Ledgers
    //////////

    // Ledgers for tracking the SND asset.
    private InMemoryLedger sandLedger1;
    private InMemoryLedger sandLedger2;
    private InMemoryLedger sandLedger3;
    // Ledgers for tracking the DRT asset.
    private InMemoryLedger dirtLedger1;

    /////////////////
    // Ledger Clients
    /////////////////

    // This is the ILP code that allows this connector to interface with the Sand Ledger.
    private LedgerClient sandLedger1Client;
    private LedgerClient sandLedger2Client;
    private LedgerClient sandLedger3Client;

    // This is the ILP code that allows this connector to inteface with the Dirt Ledger.
    private LedgerClient dirtLedger1Client;

    /////////////
    // Connectors
    /////////////

    // The Fluid Money connector that has accounts on both the Sand and Dirt ledgers.
    private InMemoryPendingTransferManager pendingTransferManager1;
    private Connector fluidConnector1;

    private InMemoryPendingTransferManager pendingTransferManager2;
    private Connector fluidConnector2;

    private FakeTicker fakeTicker;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.fakeTicker = new FakeTicker();

        // For this simulation, everyone uses the same precision and scale.
        final int precision = 2;
        final int scale = 10;

        //######################
        //######################
        // Mock the RoutingService...this will only live inside of the FluidConnector that has accounts on 2 ledgers,
        // so we always know the other side of the Route, for mocking purposes, and we hard-code it.
        // TODO: eventually this will be replaced with a real routing service that integrates with quoting.
        this.initializeMockRoutingService();

        //######################
        // Initialize Ledgers
        //######################

        // Initialize Sand Ledger1
        final LedgerInfo sandLedger1Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER1
        );
        this.sandLedger1 = new InMemoryLedger(
                "Sand Ledger 1", sandLedger1Info, sandLedger1QuotingServiceMock, DEFAULT_LEDGER_TIMEOUT, fakeTicker);
        //sandLedger1.getEscrowManager().setEscrowExpirationHandler();
        this.initializeLedgerAccounts(sandLedger1.getLedgerAccountManager());

        // Initialize Sand Ledger2
        final LedgerInfo sandLedger2Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER2
        );
        this.sandLedger2 = new InMemoryLedger("Sand Ledger 2", sandLedger2Info, sandLedger2QuotingServiceMock,
                                              DEFAULT_LEDGER_TIMEOUT, fakeTicker
        );
        this.initializeLedgerAccounts(sandLedger2.getLedgerAccountManager());

        // Initialize Sand Ledger3
        final LedgerInfo sandLedger3Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER3
        );
        this.sandLedger3 = new InMemoryLedger("Sand Ledger 3", sandLedger3Info, sandLedger3QuotingServiceMock,
                                              DEFAULT_LEDGER_TIMEOUT, fakeTicker
        );
        this.initializeLedgerAccounts(sandLedger3.getLedgerAccountManager());

        // Initialize Dirt Ledger1
        final LedgerInfo dirtLedger1Info = new DefaultLedgerInfo(
                precision, scale, DRT, DIRT_CURRENCY_SYMBOL, DIRT_LEDGER1
        );
        this.dirtLedger1 = new InMemoryLedger("Dirt Ledger 1", dirtLedger1Info, dirtLedger1QuotingServiceMock,
                                              DEFAULT_LEDGER_TIMEOUT, fakeTicker
        );
        this.initializeLedgerAccounts(dirtLedger1.getLedgerAccountManager());

        //######################
        // Initialize Connector1
        //######################
        {
            // Just random connector ids...don't read too much into it.  Hard-coded for now so the ledgerAccountId and the
            // connector1Id are the same for simulation purposes.
            final ConnectorId connector1Id = ConnectorId.of(CONNECTOR1.getId());

            // Each LedgerClient creates a single connection to a connector, and then handles all events via that
            // single connection.
            this.sandLedger1Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR1.getId())
                            .clientVersion("0.0.1")
                            .connectorId(connector1Id).connectorId(connector1Id)
                            // TODO: Improve the builder to only take an account id...
                            .ledgerAccountId(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build(),
                    sandLedger1
            );
            this.sandLedger2Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR1.getId())
                            .clientVersion("0.0.1")
                            .connectorId(connector1Id)
                            // TODO: Improve the builder to only take an account id...
                            .ledgerAccountId(IlpAddress.of(CONNECTOR1, SAND_LEDGER2))
                            .build(),
                    sandLedger2
            );
            this.dirtLedger1Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR1.getId())
                            .clientVersion("0.0.1")
                            .connectorId(connector1Id)
                            // TODO: Improve the builder to only take an account id...
                            .ledgerAccountId(IlpAddress.of(CONNECTOR1, DIRT_LEDGER1))
                            .build(),
                    dirtLedger1
            );

            final ConnectorInfo connectorInfo = ConnectorInfo.builder()
                    .connectorId(connector1Id)
                    .optLedgerAccountId(Optional.empty())
                    .build();

            // For simulation purposes, this connector has the same account identifier on all in-memory ledgers.
            final ImmutableSet<LedgerClient> ledgerClients = ImmutableSet.of(
                    sandLedger1Client, sandLedger2Client, dirtLedger1Client
            );


            // Used to mock time for the guava cache and to access the Cache via Get.
            final LedgerManager ledgerManager = new DefaultLedgerManager(
                    connectorInfo.getConnectorId(), ledgerClients, new InMemoryPendingTransferManager());
            this.fluidConnector1 = new DefaultConnector(
                    connectorInfo, this.connector1RoutingServiceMock, ledgerManager);
        }

        //######################
        // Initialize Connector2
        //######################
        {
            // Just random connector id...don't read too much into it.  Hard-coded for now so the ledgerAccountId and the
            // connector1Id are the same for simulation purposes.
            final ConnectorId connectorId = ConnectorId.of(CONNECTOR2.getId());

            // Each LedgerClient creates a single connection to a connector, and then handles all events via that
            // single connection.
            this.sandLedger2Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR2.getId())
                            .clientVersion("0.0.1")
                            .connectorId(connectorId)
                            // TODO: Improve the builder to only take an account id...
                            .ledgerAccountId(IlpAddress.of(CONNECTOR2, SAND_LEDGER2))
                            .build(),
                    sandLedger2
            );
            this.sandLedger3Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR2.getId())
                            .clientVersion("0.0.1")
                            .connectorId(connectorId)
                            .connectorId(connectorId)
                            // TODO: Improve the builder to only take an account id...
                            .ledgerAccountId(IlpAddress.of(CONNECTOR2, SAND_LEDGER3))
                            .build(),
                    sandLedger3
            );

            final ConnectorInfo connectorInfo = ConnectorInfo.builder()
                    .connectorId(connectorId)
                    .optLedgerAccountId(Optional.empty())
                    .build();


            // For simulation purposes, this connector has the same account identifier on all in-memory ledgers.
            final ImmutableSet<LedgerClient> ledgerClients = ImmutableSet.of(sandLedger2Client, sandLedger3Client);

            // Used to mock time for the guava cache and to access the Cache via Get.
            final LedgerManager ledgerManager = new DefaultLedgerManager(
                    connectorInfo.getConnectorId(), ledgerClients, new InMemoryPendingTransferManager());
            this.fluidConnector2 = new DefaultConnector(
                    connectorInfo, this.connector2RoutingServiceMock, ledgerManager);
        }

        this.initializeMockQuotingServices();
    }

    @After
    public void tearDown() {
        // Disconnect each LedgerClient...
        fluidConnector1.getLedgerManager().getLedgerClients().stream().forEach(
                simpleLedger -> simpleLedger.disconnect());
        fluidConnector2.getLedgerManager().getLedgerClients().stream().forEach(
                simpleLedger -> simpleLedger.disconnect());
    }

    /**
     * This test simulates a payment from one account to another on the same ledger (in this case the "Sand" ledger)
     * where the recipient accepts the payment payment.
     * <p>
     * The "Sand" ledger is a simulated in-memory ledger implemented by {@link InMemoryLedger} that tracks granules of
     * sand using the currency code "SND".  In this test, Alice will send SND 25 to Bob.  Since the transfer occurs on
     * the same ledger, there is no need for a crypto-condition, or any other typical ILP facilities.  This is because
     * the ledger knows both sides of the transaction and can provide all trust, liquidity, and transfer services.
     * <p>
     * However, since the transfer came into the Ledger with ILP instructions and ILP recipients, I would make the case
     * that the Ledger _should_ still process this as an ILP transaction, as best it can (i.e., without needing to
     * involve a connector and likely without needing to involve crypto-conditions (?)).  In other words, the ledger
     * should still put money on hold and give the recipient a chance to reject the money since, in general, a ledger
     * doesn't want to allow its account holders to send money into accounts without the receiving account holder's
     * approval of the transfer.
     * <p>
     * Counter-point: there is a strong case that this kind of thing doesn't need to involve ILP at all, so just let
     * each ledger do its thing.
     */
    @Test
    public void testScenario1_RecipientAcceptsTransfer() {
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final InitialLedgerTransferImpl ledgerTransfer =
                new InitialLedgerTransferImpl(
                        ilpTransactionId,
                        IlpAddress.of(ALICE, SAND_LEDGER1),
                        IlpAddress.of(BOB, SAND_LEDGER1),
                        Money.of(25, "SND")
                );

        // Simulate a wallet or other entity initiating a transfer from Alice to Bob.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "475");
        //receiver, and connector should have the initial amounts because the transfer is in a pending state.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        // Connector should not have any extra funds because it was not involved.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // Escrow should be SND 25 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, "25");

        // Simulate Bob's acceptance of the Transfer by fulfilling by ILP Transaction Id...
        sandLedger1.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        // Sender and receiver should have new account amounts reflecting a successful transfer
        assertLedgerAccount(sandLedger1, ALICE, "475");
        assertLedgerAccount(sandLedger1, BOB, "525");
        // Connector should not have any extra funds because it was not involved.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // Escrow should be SND 0 to reflect executed escrow.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a payment from one account to another on the same ledger (in this case the "Sand" ledger)
     * where the recipient accepts the payment payment.
     */
    @Test
    public void testScenario1_RecipientRejectsTransfer() {

        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer =
                new InitialLedgerTransferImpl(
                        ilpTransactionId,
                        IlpAddress.of(ALICE, SAND_LEDGER1),
                        IlpAddress.of(BOB, SAND_LEDGER1),
                        Money.of(25, "SND")
                );

        // Simulate a wallet or other entity initiating a transfer from Alice to Bob.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "475");
        //receiver, and connector should have the initial amounts because the transfer is in a pending state.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        // Connector should not have any extra funds because it was not involved.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // Escrow should be SND 25 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, "25");

        // Simulate Bob's rejection of the Transfer.
        sandLedger1.rejectTransfer(
                ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId(),
                LedgerTransferRejectedReason.REJECTED_BY_RECEIVER
        );

        // Neither sender nor receiver should have a different amount of funds because the transaction was reversed.
        assertLedgerAccount(sandLedger1, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        // Connector should not have any extra funds because it was not involved.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // Escrow should be SND 0.00 because the escrow tx was reversed.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a single connector, where the recipient accepts the transfer.  In this case, Alice
     * will send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger2} via the Fluid Connector.
     * <p>
     * This test operates as follows:
     * <p>
     * <pre>
     *  <ol>
     *      <li>The sending application (this test) chooses an amount (SND 100) and calls on its local interledger
     * module (in this case an instance of {@link Ledger}) to send that amount from Alice to an escrow account operated
     * by the ledger, with an ultimate destination of Bob's account on SandLedger2. (To accomplish this, the
     * test-harness passes the destination address and other parameters as arguments of the call).</li>
     *      <li>The ledger determines that it cannot service the transfer locally, and polls any connectors that are
     * connected to it to determine if they can process the payment (likely this will involve a combo of quoting and
     * routing service consultations as well)</li>
     * <li>The Connector responds that it can fulfill the transfer because it has accounts on both ledgers.</li>
     * <li>The connector initiates an ILP transaction on the DIRT ledger with the appropriate amount.</li>
     * <li>The DIRT ledger escrows the amount for the recipient (BOB).</li>
     * <li>The test calls an accept method on the DIRT ledger on behalf of Bob</li>
     * <li>The transfer succeeds, and the Connector is notified.</li>
     * <li>The connector receives the notification and fulfills its transfer on Ledger1.</li>
     * <li>Ledger1 transfers the money to the Connector's account and the ILP transacation is complete.</li>
     *  </ol>
     * </pre>
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario2_OneConnector_LedgersWithSameAssetType_SenderAcceptsPayment() {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER2),
                Money.of(100, "SND")
        );

        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);

        // SandLedger 2 is configured to automatically accept any payment and fulfill the condition, so there's nothing
        // more to do here except wait for the synchronous #send call to finish.  In a real system, the processing would
        // be async, so for a true integration test/simulation we would need to wait some time before checking assertions.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        // Alice's account on Ledger2 should not be affected.
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        //receiver, and connector should have the initial amounts because the transfer is in a pending state.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // The connector on SandLedger2 funded the escrow on SL2.
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        // Escrow should be SND 100 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");

        // Simulate BOB accepting funds on Ledger 2.
        sandLedger2.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        // Sender and receiver should have new account amounts reflecting a successful transfer, but uninvoled accounts
        // belonging to the same person on other ledgers should be unchanged.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, BOB, "600");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);

        // Connector should have SND 100 extra funds because was paid on ledger1 and debited on ledger2.
        assertLedgerAccount(sandLedger1, CONNECTOR1, "600");
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");

        // Escrow should be SND 0 to reflect executed escrows in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a single connector, but where the recipient rejects the transfer.  In this case,
     * Alice will attempt to send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger2} via the Fluid
     * Connector, but Bob will utlimately reject the payment.
     * <p>
     * This test operates as follows:
     * <p>
     * <pre>
     *  <ol>
     *      <li>The sending application (this test) chooses an amount (SND 100) and calls on its local interledger
     * module (in this case an instance of {@link Ledger}) to send that amount from Alice to an escrow account operated
     * by the ledger, with an ultimate destination of Bob's account on SandLedger2. (To accomplish this, the
     * test-harness passes the destination address and other parameters as arguments of the call).</li>
     *      <li>The ledger determines that it cannot service the transfer locally, and polls any connectors that are
     * connected to it to determine if they can process the payment (likely this will involve a combo of quoting and
     * routing service consultations as well)</li>
     * <li>The Connector responds that it can fulfill the transfer because it has accounts on both ledgers.</li>
     * <li>The connector initiates an 2nd ILP transaction on the DIRT ledger with the appropriate amount.</li>
     * <li>The DIRT ledger escrows the amount for the recipient (BOB).</li>
     * <li>The test calls a "reject" method on the DIRT ledger on behalf of Bob</li>
     * <li>The transfer is rejected, and the Connector is notified.</li>
     * <li>The connector receives the notification and rejects its transfer on Ledger1.</li>
     * <li>Ledger1 transfers the money back to Alice's account and the ILP transacation is reversed.</li>
     *  </ol>
     * </pre>
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario2_OneConnector_LedgersWithSameAssetType_SenderRejectsPayment() {
        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER2),
                Money.of(100, "SND")
        );

        // SandLedger 2 is configured to automatically accept any payment and fulfill the condition, so there's nothing
        // more to do here except wait for the synchronous #send call to finish.  In a real system, the processing would
        // be async, so for a true integration test/simulation we would need to wait some time before checking assertions.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        // Alice's account on Ledger2 should not be affected.
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        //receiver, and connector should have the initial amounts because the transfer is in a pending state.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // The connector on SandLedger2 funded the escrow on SL2.
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        // Escrow should be SND 100 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");

        // Simulate BOB rejecting funds on Ledger 2.
        sandLedger2.rejectTransfer(
                ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId(),
                LedgerTransferRejectedReason.REJECTED_BY_RECEIVER
        );

        // Sender and receiver should have their original amounts because the transfer was rejected and reversed.
        assertLedgerAccount(sandLedger1, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);

        // Connector should have their original amounts because the transfer was rejected and reversed.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);

        // Escrow should be SND 0 to reflect reversed escrows in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a two connectors, where the final recipient accepts the transfer.  In this case,
     * Alice will send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger2} via two Fluid
     * Connectors, FluidConnector1 and FluidConnector2.
     * <p>
     * FluidConnector1 has an accounts on the SandLedger1 and SandLedger2.  FluidConnector2 has accounts on the
     * SandLedger2 and SandLedger3.  In order to complete the transaction, ILP activity will need to occur on all three
     * ledgers.
     * <p>
     * This test operates as follows:
     * <p>
     * <pre>
     *  <ol>
     *      <li>The sending application (this test) chooses an amount (SND 100) and calls on its local interledger
     * module (in this case an instance of {@link Ledger}) on SandLedger1 to send that amount from Alice to an escrow
     * account operated
     * by the ledger, with an ultimate destination of Bob's account on SandLedger3.</li>
     *      <li>SandLedger1 determines that it cannot service the transfer locally, and polls any connectors that are
     * connected to it to determine if they can process the payment.</li>
     * <li>Connector1 responds that it can fulfill the transfer.</li>
     * <li>The connector initiates an ILP transaction on SandLedger2 with the appropriate amount.</li>
     * <li>SandLedger2 escrows the amount for the recipient and notifies Connector2.</li>
     * <li>Connector2 has an account on SandLedger3, and sends money to BOB.</li>
     * <li>Bob accepts the transfer, and the Connector2 is notified.</li>
     * <li>Connector2 receives the notification and fulfills its transfer on Ledger2, which notifies Connector1.</li>
     * <li>Connector1 receives the notification and fulfills its transfer on Ledger1.</li>
     * <li>Ledger1 executes the final escrow, releasing money to Connector1's account and the ILP transacation is
     * complete.</li>
     *  </ol>
     * </pre>
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario2_TwoConnectors_LedgersWithSameAssetType_SenderAcceptsPayment() {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER3),
                Money.of(100, "SND")
        );

        // SandLedger 3 is configured to automatically accept any payment and fulfill the condition, so there's nothing
        // more to do here except wait for the synchronous #send call to finish.  In a real system, the processing would
        // be async, so for a true integration test/simulation we would need to wait some time before checking assertions.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are funded...
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");
        assertLedgerAccount(sandLedger3, ESCROW, "100");

        // Simulate BOB accepting funds on Ledger 3.
        sandLedger3.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, "600");

        assertLedgerAccount(sandLedger1, CONNECTOR1, "600");
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, "600");
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are executed...
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a two connectors, where the final recipient rejects the transfer.
     * <p>
     * In this case, Alice will send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger3} via two
     * Connectors, FluidConnector1 and FluidConnector2.
     * <p>
     * FluidConnector1 has an accounts on the SandLedger1 and SandLedger2.  FluidConnector2 has accounts on the
     * SandLedger2 and SandLedger3.  In order to complete the transaction, ILP activity will need to occur on all three
     * ledgers.
     */
    @Test
    public void testScenario2_TwoConnectors_LedgersWithSameAssetType_SenderRejectsPayment() {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER3),
                Money.of(100, "SND")
        );

        // SandLedger 3 is configured to automatically accept any payment and fulfill the condition, so there's nothing
        // more to do here except wait for the synchronous #send call to finish.  In a real system, the processing would
        // be async, so for a true integration test/simulation we would need to wait some time before checking assertions.
        sandLedger1.send(ledgerTransfer);

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are funded...
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");
        assertLedgerAccount(sandLedger3, ESCROW, "100");

        // Simulate BOB accepting funds on Ledger 3.
        sandLedger3.rejectTransfer(
                ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId(),
                LedgerTransferRejectedReason.REJECTED_BY_RECEIVER
        );

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, INITIAL_AMOUNT);

        // Escrows on all ledgers are executed...
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a two connectors, where the final recipient accepts the transfer, but the second
     * connector (Connector2) does not pass a fulfillment back to Connector1 before the transaction expires, because it
     * (Connector2) goes offline.
     * <p>
     * In this case, Alice will attempt to send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger3}
     * via two Fluid Connectors, FluidConnector1 and FluidConnector2.
     * <p>
     * FluidConnector1 has an accounts on the SandLedger1 and SandLedger2.  FluidConnector2 has accounts on the
     * SandLedger2 and SandLedger3.  In order to complete the transaction, ILP activity will need to occur on all three
     * ledgers.
     */
    @Test
    public void testScenario2_TwoConnectors_LedgersWithSameAssetType_Connector2TimeOut() throws InterruptedException {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER3),
                Money.of(100, "SND")
        );

        sandLedger1.send(ledgerTransfer);

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are funded...
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");
        assertLedgerAccount(sandLedger3, ESCROW, "100");

        // Take Connector2 Offline.
        fluidConnector2.getLedgerManager().findLedgerClient(SAND_LEDGER3).ifPresent(
                (ledgerClient -> ledgerClient.disconnect()));

        // Simulate BOB accepting funds on Ledger 3.
        sandLedger3.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        // Because Connector2 is not connected, it never gets the LedgerEvent.  So, Ledger3 will be in a fulfilled state,
        // but the other ledgers and escrows will not.

        // Sender should have money returned to her on Ledger1
        assertLedgerAccount(sandLedger1, ALICE, "400");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Bob has accepted the funds on SL3 only.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, "600");

        // Connector1 should have all of its funds returned...
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        // Connector2 should have lost money on SL3, since it was paid to Bob.
        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are reversed...
        assertLedgerAccount(sandLedger1, ESCROW, "100");
        assertLedgerAccount(sandLedger2, ESCROW, "100");
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);

        // In this particular case, we simulate that some time goes by because the other ledgers (Ledgers 2 and 1) will
        // expire their escrows.


        fakeTicker.advance(10, TimeUnit.SECONDS);
        this.sandLedger2.getEscrowManager().getEscrows().cleanUp();
        this.sandLedger1.getEscrowManager().getEscrows().cleanUp();

        // Sender should have money returned to her on Ledger1
        assertLedgerAccount(sandLedger1, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Bob has accepted the funds on SL3 only.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, "600");

        // Connector1 should have all of its funds returned...
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        // Connector2 should have lost money on SL3, since it was paid to Bob.
        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400");

        // Escrows on all ledgers are reversed...
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);
    }

    // testScenario2_OneConnector_LedgersWithSameAssetType_MiddleConnectorFailsFulfilment


    // TODO: Create a test that exercises the routing table.  For example, if two connectors are connected to a ledger and
    // both can process/route an ILP payment, then the ledger should be smart enough to route the payment to the best
    // connector (this will be some combo of price, speed etc -- i.e., the liquidity curve).

    //////////////////
    // Private Helpers
    //////////////////

    /**
     * Mock the RoutingService...this will only live inside of the FluidConnector that has accounts on 2 ledgers,
     * so we always know the other side of the Route, for mocking purposes, and we hard-code it.
     * <p>
     * TODO: eventually this will be replaced with a real routing service that integrates with quoting.
     */
    private void initializeMockRoutingService() {

        ///////////////////////////////
        // Routing Table for Connector1
        ///////////////////////////////

        // These are merely simple routes to enable the test harness so we don't have to implement the entire routing
        // implementation until later.
        {
            final RoutingService routingServiceMock = connector1RoutingServiceMock;

            // While on Connector1, the only way to get funds to Bob on SL1 is to send to Connector1 on SL1.
//            final DefaultRoute defaultRouteToBobAtSand1 = DefaultRoute.builder()
//                    .sourceAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
//                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER1))
//                    .optExpiresAt(Optional.empty())
//                    .build();
//            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER1)), any()))
//                    .thenReturn(Optional.of(defaultRouteToBobAtSand1));

            // While on Connector1, the only way to get funds to Bob on SL2 is to send from Connector1 on SL2.
            final DefaultRoute defaultRouteToBobAtSand2 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER2))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER2))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER2))))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand2));
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER2)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand2));

            // While on Connector1, the only way to get funds to Bob on SL3 is to send from Connector2 on SL2.
            final DefaultRoute defaultRouteToBobAtSand3 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER2))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER3))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER3)))).thenReturn(
                    Optional.of(defaultRouteToBobAtSand3));
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER3)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
        }
        ///////////////////////////////
        // Routing Table for Connector2
        ///////////////////////////////

        {
            // While on Connector2, the only way to get funds to Bob on SL3 is to send from Connector2 on SL3.
            final RoutingService routingServiceMock = connector2RoutingServiceMock;
            final DefaultRoute defaultRouteToBobAtSand3 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER3))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER3))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER3))))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
            when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER3)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
        }

    }

    /**
     * Helper method to initialize accounts for Alice, Bob, Chloe, and Connector on the supplied {@link
     * LedgerAccountManager}.
     */
    private void initializeLedgerAccounts(final LedgerAccountManager ledgerAccountManager) {
        this.initializeLedgerAccountsHelper(
                ledgerAccountManager, IlpAddress.of(ALICE, ledgerAccountManager.getLedgerInfo().getLedgerId()));
        this.initializeLedgerAccountsHelper(
                ledgerAccountManager, IlpAddress.of(BOB, ledgerAccountManager.getLedgerInfo().getLedgerId()));
        this.initializeLedgerAccountsHelper(
                ledgerAccountManager, IlpAddress.of(CHLOE, ledgerAccountManager.getLedgerInfo().getLedgerId()));
        this.initializeLedgerAccountsHelper(
                ledgerAccountManager, IlpAddress.of(CONNECTOR1, ledgerAccountManager.getLedgerInfo().getLedgerId()));
        this.initializeLedgerAccountsHelper(
                ledgerAccountManager, IlpAddress.of(CONNECTOR2, ledgerAccountManager.getLedgerInfo().getLedgerId()));
    }

    private void initializeLedgerAccountsHelper(
            final LedgerAccountManager ledgerAccountManager, final IlpAddress ilpAddress
    ) {
        final LedgerAccount theAccount = ledgerAccountManager.getAccount(ilpAddress)
                .orElseGet(() -> ledgerAccountManager.createAccount(ilpAddress));

        // For testing purposes, give each account 500 of the ledger's tracked asset.
        // TODO: If #creditAccount gets removed, we can merely cast to the implementation for purposes of this test code.
        // Real applications would be creating accounts on a ledger out-of-band of the ILP process, so this method will likel
        // go away from the ILP interface, which focuses on crediting/debiting, and balance checking.
        ledgerAccountManager.creditAccount(
                theAccount.getIlpIdentifier(), Money.of(500, ledgerAccountManager.getLedgerInfo().getCurrencyCode())
        );
    }

    private void assertLedgerAccount(
            final Ledger ledger,
            final LedgerAccountId ledgerAccountId,
            final String amount
    ) {
        final IlpAddress ledgerAccountAddress = IlpAddress.of(ledgerAccountId, ledger.getLedgerInfo().getLedgerId());
        final LedgerAccount ledgerAccount = ledger.getLedgerAccountManager().getAccount(ledgerAccountAddress).get();
        assertThat(
                ledgerAccount.getBalance(),
                is(Money.of(new BigDecimal(amount), ledger.getLedgerInfo().getCurrencyCode()))
        );
    }

    private void assertLedgerAccount(
            final Ledger ledger,
            final LedgerAccountId ledgerAccountId,
            final MonetaryAmount expectedAmount
    ) {
        final IlpAddress ledgerAccountAddress = IlpAddress.of(ledgerAccountId, ledger.getLedgerInfo().getLedgerId());
        final LedgerAccount ledgerAccount = ledger.getLedgerAccountManager().getAccount(ledgerAccountAddress).get();
        assertThat(ledgerAccount.getBalance(), is(expectedAmount)
                   //is(Money.of(new BigDecimal("475.00"), SND))
        );
    }

    /**
     * Mock the QuotingService for now.
     * TODO: eventually this will be replaced with a real service that integrates with routing.
     */
    private void initializeMockQuotingServices() {
        when(sandLedger1QuotingServiceMock.findBestConnector(any())).thenReturn(
                Optional.of(this.fluidConnector1.getConnectorInfo()));

        when(sandLedger2QuotingServiceMock.findBestConnector(any())).thenReturn(
                Optional.of(this.fluidConnector2.getConnectorInfo()));
    }
}
