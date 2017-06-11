package money.fluid.ilp.connector;

import com.google.common.collect.ImmutableSet;
import money.fluid.ilp.connector.managers.ledgers.DefaultLedgerManager;
import money.fluid.ilp.connector.managers.ledgers.InMemoryPendingTransferManager;
import money.fluid.ilp.connector.managers.ledgers.LedgerManager;
import money.fluid.ilp.connector.model.ConnectorInfo;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.connector.services.ExchangeRateService;
import money.fluid.ilp.connector.services.ExchangeRateService.ExchangeRateInfo;
import money.fluid.ilp.connector.services.routing.DefaultRoute;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.QuotingService;
import money.fluid.ilp.ledger.QuotingService.LedgerQuote;
import money.fluid.ilp.ledger.inmemory.InMemoryLedger;
import money.fluid.ilp.ledger.inmemory.InMemoryLedger.InMemoryLedgerAccountManager;
import money.fluid.ilp.ledger.inmemory.model.DefaultLedgerInfo;
import money.fluid.ilp.ledger.inmemory.model.InitialLedgerTransferImpl;
import money.fluid.ilp.ledger.inmemory.utils.MoneyUtils;
import money.fluid.ilp.ledger.model.ConnectionInfo;
import money.fluid.ilp.ledger.model.LedgerAccount;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.ledgerclient.InMemoryLedgerClient;
import money.fluid.ilp.ledgerclient.LedgerClient;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;
import org.interledgerx.ilp.core.LedgerInfo;
import org.interledgerx.ilp.core.LedgerTransfer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * This class is meant to be run using JUnit to simulate optimistic-mode ILP payments via an in-memory implementation of
 * {@link Connector} (see {@link DefaultConnector}) and various instances of {@link InMemoryLedger}.
 * <p>
 * The test harness covers the following scenarios:
 * <p>
 * <pre>
 *     <h1>Scenario 1: Same Ledger (No Connector)</h1>
 *      Simulate an ILP payments from a sender/receiver pair in the same ledger (not serviced by a Connector).
 * </pre>
 * <p>
 * <pre>
 *     <h1>Scenario 2: Same Ledger Type (1 Connector)</h1>
 *     Simulate ILP payments from a sender/receiver pair on different ledgers of the same asset type (i.e.,
 * no exchange-rate fluctuations) serviced by a single connector.
 * </pre>
 * * <pre>
 *     <h1>Scenario 3: Same Ledger Type (Multiple Connectors)</h1>
 *      Simulate ILP payments from a sender/receiver pair on different ledgers of the same asset type (i.e., no
 * exchange-rate fluctuations) serviced by a two connectors).
 * </pre>
 * <pre>
 *     <h1>Scenario 4: Different Ledger Types (1 Connector)</h1>
 *     These tests simulate ILP payments from a sender/receiver pair on different ledgers with different asset types
 * (i.e., the payment will involve exchange-rate fluctuations) serviced by a single connector.
 *      <ul>
 *         <li>An ILP payment where the sender specifies a source-amount.</li>
 *         <li>An ILP payment where the sender specifies a destination-amount.</li>
 *     </ul>
 * </pre>
 * <pre>
 *     <h1>Scenario 5: Different Ledger Types (Multiple Connectors)</h1>
 *      These tests simulate ILP payments from a sender/receiver pair on different ledgers with different asset types
 * (i.e., the payment will involve exchange-rate fluctuations) serviced by a two connectors.
 *      <ul>
 *         <li>An ILP payment where the sender specifies a source-amount.</li>
 *         <li>An ILP payment where the sender specifies a destination-amount.</li>
 *     </ul>
 * </pre>
 * <p>
 * <p>
 * TODO: Create a test that mock various scenarios of the routing table.  For example, if two connectors are connected
 * to a ledger and both can process/route an ILP payment, then the ledger should be smart enough to route the payment to
 * the best connector (this will be some combo of price, speed etc -- i.e., the liquidity curve).
 */
public class OptimisticModeInMemoryTestHarness {

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
    private Connector fluidConnector1;
    private Connector fluidConnector2;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // For this simulation, everyone uses the same precision and scale.
        final int precision = 2;
        final int scale = 10;

        //######################
        // Initialize Ledgers
        //######################

        // Initialize Sand Ledger1
        final LedgerInfo sandLedger1Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER1
        );
        this.sandLedger1 = new InMemoryLedger("Sand Ledger 1", sandLedger1Info, mock(QuotingService.class));
        this.initializeLedgerAccounts(sandLedger1.getLedgerAccountManager());

        // Initialize Sand Ledger2
        final LedgerInfo sandLedger2Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER2
        );
        this.sandLedger2 = new InMemoryLedger("Sand Ledger 2", sandLedger2Info, mock(QuotingService.class)
        );
        this.initializeLedgerAccounts(sandLedger2.getLedgerAccountManager());

        // Initialize Sand Ledger3
        final LedgerInfo sandLedger3Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER3
        );
        this.sandLedger3 = new InMemoryLedger("Sand Ledger 3", sandLedger3Info, mock(QuotingService.class));
        this.initializeLedgerAccounts(sandLedger3.getLedgerAccountManager());

        // Initialize Dirt Ledger1
        final LedgerInfo dirtLedger1Info = new DefaultLedgerInfo(
                precision, scale, DRT, DIRT_CURRENCY_SYMBOL, DIRT_LEDGER1
        );
        this.dirtLedger1 = new InMemoryLedger("Dirt Ledger 1", dirtLedger1Info, mock(QuotingService.class));
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
                            .ledgerAccountIlpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build(),
                    sandLedger1
            );
            this.sandLedger2Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR1.getId())
                            .clientVersion("0.0.1")
                            .ledgerAccountIlpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER2))
                            .build(),
                    sandLedger2
            );
            this.dirtLedger1Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR1.getId())
                            .clientVersion("0.0.1")
                            .ledgerAccountIlpAddress(IlpAddress.of(CONNECTOR1, DIRT_LEDGER1))
                            .build(),
                    dirtLedger1
            );

            final ImmutableSet<LedgerClient> ledgerClients = ImmutableSet.of(
                    sandLedger1Client, sandLedger2Client, dirtLedger1Client
            );
            final ConnectorInfo connectorInfo = ConnectorInfo.builder().connectorId(connector1Id).build();
            final LedgerManager ledgerManager = new DefaultLedgerManager(
                    connector1Id, ledgerClients, new InMemoryPendingTransferManager()
            );
            this.fluidConnector1 = new DefaultConnector(
                    connectorInfo,
                    mock(RoutingService.class),
                    ledgerManager,
                    mock(ExchangeRateService.class)
            );
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
                            .ledgerAccountIlpAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER2))
                            .build(),
                    sandLedger2
            );
            this.sandLedger3Client = new InMemoryLedgerClient(
                    ConnectionInfo.builder()
                            .clientId(CONNECTOR2.getId())
                            .clientVersion("0.0.1")
                            .ledgerAccountIlpAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER3))
                            .build(),
                    sandLedger3
            );

            final ConnectorInfo connectorInfo = ConnectorInfo.builder().connectorId(connectorId).build();
            final ImmutableSet<LedgerClient> ledgerClients = ImmutableSet.of(sandLedger2Client, sandLedger3Client);
            final LedgerManager ledgerManager = new DefaultLedgerManager(
                    connectorInfo.getConnectorId(), ledgerClients, new InMemoryPendingTransferManager()
            );
            this.fluidConnector2 = new DefaultConnector(
                    connectorInfo,
                    mock(RoutingService.class),
                    ledgerManager,
                    mock(ExchangeRateService.class)
            );
        }

        //######################
        //######################
        // Mock the RoutingService...this will only live inside of the FluidConnector that has accounts on 2 ledgers,
        // so we always know the other side of the Route, for mocking purposes, and we hard-code it.
        // TODO: eventually this will be replaced with a real routing service that integrates with quoting.
        this.initializeMockRoutingService();

        this.initializeMockQuotingServices();

        this.initializeExchangeRateService();
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
     * This test simulates an optimistic-mode payment from one account to another on the same ledger (in this case the
     * "Sand" ledger).
     * <p>
     * In this scenario, the "Sand" ledger is a simulated in-memory {@link Ledger} implemented by {@link InMemoryLedger}
     * that tracks granules of sand using the currency code "SND".  In this test, Alice will send SND 25 to Bob.  Since
     * the transfer occurs on the same ledger, there is no need for a crypto-condition, or any other typical ILP
     * facilities.  This is because the ledger knows both sides of the transaction and can provide all trust, liquidity,
     * and transfer services.</p>
     * <p>
     * Note: Optimistic-mode does not support rejection, so ledgers that wish to support such functionality should do
     * so outside of ILP.</p>
     * <p>
     * TODO: Move this into the unit tests for the Ledger, and not the Connector!
     */
    @Test
    public void testScenario1_SameLedger() {
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final InitialLedgerTransferImpl ledgerTransfer =
                new InitialLedgerTransferImpl(
                        ilpTransactionId,
                        IlpAddress.of(ALICE, SAND_LEDGER1),
                        IlpAddress.of(BOB, SAND_LEDGER1),
                        MoneyUtils.toMonetaryAmount("25", SND)
                );

        this.assertInitialAmounts();

        // Simulate a wallet or other entity initiating a transfer from Alice to Bob.
        sandLedger1.send(ledgerTransfer);

        // Sender should have a reduced amount reflecting a successful transfer
        assertLedgerAccount(sandLedger1, ALICE, "475");
        // Receiver has not yet accepted the payment, so he will have the initial amount.
        assertLedgerAccount(sandLedger1, BOB, "525");
        // Connector is not involved.
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        // Escrow has money on hold...
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates an optimistic-mode, fixed-source-amount payment from one account to another on different
     * ledgers that share the same asset type, connected by a single connector.
     * <p>
     * In this case, Alice will send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger2}.
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario2_OneConnector_LedgersWithSameAssetType() {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER2),
                MoneyUtils.toMonetaryAmount("100.00", SND)
        );

        this.assertInitialAmounts();

        sandLedger1.send(ledgerTransfer);

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "399.99"); // To send 100 SND, it costs 100.01 SND.
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        //Bob has not yet accepted funds, so his account should be unchanged.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, "599.01");
        // The Connector will have paid and received funds since this is an OM payment.
        assertLedgerAccount(sandLedger1, CONNECTOR1, "600.01");
        assertLedgerAccount(sandLedger2, CONNECTOR1, "400.99");
        // Escrow should be SND 100 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
    }


    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a two connectors.  In this case, Alice will send SND 100 from {@code sandLedger1}
     * to Bob's account on {@code sandLedger2} via two Connectors, FluidConnector1 and FluidConnector2.
     * <p>
     * NOTE: FluidConnector1 has an accounts on the SandLedger1 and SandLedger2.  FluidConnector2 has accounts on the
     * SandLedger2 and SandLedger3.  In order to complete the transaction, ILP activity will need to occur on all three
     * ledgers.
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario3_TwoConnectors_LedgersWithSameAssetType() {

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER3),
                MoneyUtils.toMonetaryAmount("100.00", SND)
        );

        this.assertInitialAmounts();

        sandLedger1.send(ledgerTransfer);

        assertLedgerAccount(sandLedger1, ALICE, "399.99");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, "599.01");
        assertLedgerAccount(sandLedger1, CONNECTOR1, "600.01");
        assertLedgerAccount(sandLedger2, CONNECTOR1, "399.99");
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, "600.01");
        assertLedgerAccount(sandLedger3, CONNECTOR2, "400.99");
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates an optimistic-mode fixed-source-amount payment from one account to another on different
     * ledgers that share the same asset type, connected by a two connectors, but where the first connector has no route
     * to the final ledger.
     * <p>
     * In this scenario, since all transfers are optimistic, the only account that will lose funds will be Connector2 on
     * SandLedger3.
     */
    @Test
    public void testScenario3b_TwoConnectors_LedgersWithSameAssetType_NoRouteToLedger3() throws InterruptedException {

        when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(
                eq(IlpAddress.of(BOB, SAND_LEDGER3)))).thenReturn(
                Optional.empty());
        when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(
                eq(IlpAddress.of(BOB, SAND_LEDGER3)), any())).thenReturn(Optional.empty());

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER3),
                MoneyUtils.toMonetaryAmount("100.00", SND)
        );

        sandLedger1.send(ledgerTransfer);

        // Sender has sent money from ledger1, but her accounts on other ledgers should be left untouched.
        assertLedgerAccount(sandLedger1, ALICE, "399.99");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, ALICE, INITIAL_AMOUNT);

        // Receivers accounts should not be affected until he accepts the transfer.
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, BOB, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR1, "600.01");
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR1, INITIAL_AMOUNT);

        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, INITIAL_AMOUNT);

        // Escrows on all ledgers are funded...
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger3, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates an optimistic-mode, fixed-source-amount payment from one account to another on different
     * ledgers that have different asset types, connected by a single connector.
     * <p>
     * In this case, Alice will send SND 100 from {@code sandLedger1} to Bob's account on {@code dirtLedger1}.  This
     * test assumes a 1.1:1.0 exchange-rate between SND and DRT.
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario4a_OneConnectors_LedgersWithDifferentAssetTypes_FixedSourceAmount() {

        // Step1: Assemble and send an amount of SND to Bob on the Dirt Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, DIRT_LEDGER1),
                MoneyUtils.toMonetaryAmount("100.00", SND)
        );

        this.assertInitialAmounts();

        sandLedger1.send(ledgerTransfer);

        // SND 100 == DRT 110
        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "399.99");
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, ALICE, INITIAL_AMOUNT);
        //Bob has received funds in dirtLedger1
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, BOB, "608.9");

        // Connector1
        assertLedgerAccount(sandLedger1, CONNECTOR1, "600");
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, CONNECTOR1, "391.1");

        // Connector2
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger3, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, CONNECTOR2, INITIAL_AMOUNT);

        // Escrow should be SND 100 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(dirtLedger1, ESCROW, ZERO_AMOUNT);
    }

    /**
     * This test simulates an optimistic-mode, fixed-destination-amount payment from one account to another on different
     * ledgers that have different asset types, connected by a single connector.
     * <p>
     * In this case, Alice will send DRT 100 from {@code sandLedger1} to Bob's account on {@code dirtLedger1}.  This
     * test assumes a 1.1:1.0 exchange-rate between SND and DRT.
     *
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md#without-holds-optimistic-mode"
     */
    @Test
    public void testScenario4b_OneConnector_LedgersWithDifferentAssetTypes_FixedDestinationAmount() {

        // Step1: Assemble and send an amount of DRT to Bob on the Sand2 Ledger.
        final IlpTransactionId ilpTransactionId = IlpTransactionId.of(UUID.randomUUID().toString());
        final LedgerTransfer ledgerTransfer = new InitialLedgerTransferImpl(
                ilpTransactionId,
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, DIRT_LEDGER1),
                MoneyUtils.toMonetaryAmount("100.00", DRT)
        );

        this.assertInitialAmounts();

        sandLedger1.send(ledgerTransfer);

        // DRT 100 == SND 90.91

        // Sender has sent money, so that account should be reduced.
        assertLedgerAccount(sandLedger1, ALICE, "409.09"); // Alice only needed SND 90.09 to transfer DRT 100
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, ALICE, INITIAL_AMOUNT);
        //Bob has received funds in dirtLedger1
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, BOB, "600");
        // The Connector will have paid and received funds since this is an OM payment.
        assertLedgerAccount(sandLedger1, CONNECTOR1, "590.91");
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(dirtLedger1, CONNECTOR1, "400");
        // Escrow should be SND 100 to reflect escrowed funds in each ledger.
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(dirtLedger1, ESCROW, ZERO_AMOUNT);
    }

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
            // While on Connector1, the only way to get funds to Bob on SL2 is to send from Connector1 on SL2.
            final DefaultRoute defaultRouteToBobAtSand2 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER2))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER2))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER2))))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand2));
            when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER2)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand2));

            // While on Connector2, the only way to get funds to Bob on SL3 is to send from Connector2 on SL2.
            final DefaultRoute defaultRouteToBobAtSand3 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER2))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER3))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER3)))).thenReturn(
                    Optional.of(defaultRouteToBobAtSand3));
            when(fluidConnector1.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER3)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
        }
        ///////////////////////////////
        // Routing Table for Connector2
        ///////////////////////////////

        {
            // While on Connector2, the only way to get funds to Bob on SL3 is to send from Connector2 on SL3.
            final DefaultRoute defaultRouteToBobAtSand3 = DefaultRoute.builder()
                    .sourceAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER3))
                    .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER3))
                    .optExpiresAt(Optional.empty())
                    .build();
            when(fluidConnector2.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER3))))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
            when(fluidConnector2.getRoutingService().bestHopForDestinationAmount(eq(
                    IlpAddress.of(BOB, SAND_LEDGER3)), any()))
                    .thenReturn(Optional.of(defaultRouteToBobAtSand3));
        }

    }

    private void initializeExchangeRateService() {
        {
//            // SND 100 translates to DRT 110
//            final MonetaryAmount sndAmount = MoneyUtils.toMonetaryAmount("100", SND);
//            final MonetaryAmount drtAmount = MoneyUtils.toMonetaryAmount("110", DRT);
//            final ExchangeRateInfo exchangeRateInfoMock = new ExchangeRateInfo(sndAmount, drtAmount);


            final Answer<ExchangeRateInfo> answer = (invocation) -> {
                Object[] args = invocation.getArguments();
                final MonetaryAmount amount = (MonetaryAmount) args[0];
                final CurrencyUnit destinationCurrencyUnit = (CurrencyUnit) args[1];

                if (amount.getCurrency().equals(destinationCurrencyUnit)) {
                    final ExchangeRateInfo exchangeRateInfo = ExchangeRateInfo.builder()
                            .sourceAmount(amount)
                            .destinationAmount(MoneyUtils.toMonetaryAmount(
                                    amount.multiply(.99).getNumber().toString(),
                                    amount.getCurrency().getCurrencyCode()
                            ))
                            .build();
                    return exchangeRateInfo;
                } else if (destinationCurrencyUnit.getCurrencyCode().equals(SND)) {
                    final ExchangeRateInfo exchangeRateInfo = ExchangeRateInfo.builder()
                            .sourceAmount(amount)
                            .destinationAmount(MoneyUtils.toMonetaryAmount(
                                    amount.multiply(.91).getNumber().toString(),
                                    amount.getCurrency().getCurrencyCode()
                            ))
                            .build();
                    return exchangeRateInfo;
                } else {
                    // Assume DRT.
                    final ExchangeRateInfo exchangeRateInfo = ExchangeRateInfo.builder()
                            .sourceAmount(amount)
                            .destinationAmount(MoneyUtils.toMonetaryAmount(
                                    amount.multiply(1.1).getNumber().toString(),
                                    amount.getCurrency().getCurrencyCode()
                            ))
                            .build();
                    return exchangeRateInfo;
                }
            };

            when(fluidConnector1.getExchangeRateService().getExchangeRate(any(), any())).thenAnswer(answer);
            when(fluidConnector2.getExchangeRateService().getExchangeRate(any(), any())).thenAnswer(answer);
        }
//        {
//            // SND 100 translates to SND 100
//            final MonetaryAmount sndAmount = MoneyUtils.toMonetaryAmount("100", SND);
//            final MonetaryAmount drtAmount = MoneyUtils.toMonetaryAmount("100", SND);
//            final ExchangeRateInfo exchangeRateInfoMock = new ExchangeRateInfo(sndAmount, drtAmount);
//            when(fluidConnector1.getExchangeRateService().getExchangeRate(sndAmount, Monetary.getCurrency(SND)))
//                    .thenReturn(exchangeRateInfoMock);
//            when(fluidConnector2.getExchangeRateService().getExchangeRate(sndAmount, Monetary.getCurrency(SND)))
//                    .thenReturn(exchangeRateInfoMock);
//        }
//        {
//            // DRT 100 translates to SND 90.91
//            final MonetaryAmount drtAmount = MoneyUtils.toMonetaryAmount("100", DRT);
//            final MonetaryAmount sndAmount = MoneyUtils.toMonetaryAmount("90.91", SND);
//            final ExchangeRateInfo exchangeRateInfoMock = new ExchangeRateInfo(drtAmount, sndAmount);
//            when(fluidConnector1.getExchangeRateService().getExchangeRate(drtAmount, Monetary.getCurrency(SND)))
//                    .thenReturn(exchangeRateInfoMock);
//            when(fluidConnector2.getExchangeRateService().getExchangeRate(drtAmount, Monetary.getCurrency(SND)))
//                    .thenReturn(exchangeRateInfoMock);
//        }
//        {
//            // DRT 100 translates to DRT 100
//            final MonetaryAmount drtAmount = MoneyUtils.toMonetaryAmount("100", DRT);
//            final MonetaryAmount sndAmount = MoneyUtils.toMonetaryAmount("100", DRT);
//            final ExchangeRateInfo exchangeRateInfoMock = new ExchangeRateInfo(drtAmount, sndAmount);
//            when(fluidConnector1.getExchangeRateService().getExchangeRate(drtAmount, Monetary.getCurrency(DRT)))
//                    .thenReturn(exchangeRateInfoMock);
//            when(fluidConnector2.getExchangeRateService().getExchangeRate(drtAmount, Monetary.getCurrency(DRT)))
//                    .thenReturn(exchangeRateInfoMock);
//        }

    }

    // Mock the Connector to charge a 1% fee on all transactions...
//    private void initializeConnectorFeeService() {
//
//        final Answer<ConnectorFeeInfo> answer = (invocation) -> {
//            Object[] args = invocation.getArguments();
//            final MonetaryAmount originalAmount = (MonetaryAmount) args[0];
//
//            return ConnectorFeeInfo.builder()
//                    .originalAmount(originalAmount)
//                    .amountAfterFee(originalAmount.multiply(0.99))
//                    .build();
//        };
//
//        when(fluidConnector1.getConnectorFeeService().calculateConnectorFee(any())).thenAnswer(answer);
//        when(fluidConnector2.getConnectorFeeService().calculateConnectorFee(any())).thenAnswer(answer);
//    }

    /**
     * Helper method to initialize accounts for Alice, Bob, Chloe, and Connector on the supplied {@link
     * LedgerAccountManager}.
     */

    private void initializeLedgerAccounts(final InMemoryLedgerAccountManager ledgerAccountManager) {
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
            final InMemoryLedgerAccountManager ledgerAccountManager, final IlpAddress ilpAddress
    ) {
        ledgerAccountManager.getAccount(ilpAddress).orElseGet(() -> ledgerAccountManager.createAccount(
                ilpAddress,
                MoneyUtils.toMonetaryAmount("500.00", ledgerAccountManager.getLedgerInfo().getCurrencyCode())
        ));
    }

    private void assertLedgerAccount(
            final Ledger ledger,
            final LedgerAccountId ledgerAccountId,
            final String amount
    ) {
        final IlpAddress ledgerAccountAddress = IlpAddress.of(
                ledgerAccountId, ledger.getLedgerInfo().getLedgerId());
        final LedgerAccount ledgerAccount = ledger.getLedgerAccountManager().getAccount(ledgerAccountAddress).get();
        assertThat(
                ledgerAccount.getBalance(),
                is(MoneyUtils.toMonetaryAmount(amount, ledger.getLedgerInfo().getCurrencyCode()))
        );
    }

    private void assertLedgerAccount(
            final Ledger ledger,
            final LedgerAccountId ledgerAccountId,
            final MonetaryAmount expectedAmount
    ) {
        final IlpAddress ledgerAccountAddress = IlpAddress.of(
                ledgerAccountId, ledger.getLedgerInfo().getLedgerId());
        final LedgerAccount ledgerAccount = ledger.getLedgerAccountManager().getAccount(ledgerAccountAddress).get();
        assertThat(ledgerAccount.getBalance(), is(expectedAmount)
                   //is(Money.of(new BigDecimal("475.00"), SND))
        );
    }

    /**
     * Mock the QuotingService from the perspective of a {@link Ledger}.  This duplicates the ExchangeRate service
     * (which is a Connector service) but is necessary since this test is actually testing two different systems in one.
     */
    private void initializeMockQuotingServices() {

        // Connector1 on SandLedger1 is the best route for Bob on SandLedger2.
        {
            final IlpAddress destinationAddress = IlpAddress.of(BOB, SAND_LEDGER2);
            final MonetaryAmount destinationAmount = MoneyUtils.toMonetaryAmount("100.00", "SND");

            final money.fluid.ilp.ledger.model.ConnectorInfo ledgerConnectorInfo =
                    money.fluid.ilp.ledger.model.ConnectorInfo.builder()
                            .connectorId(fluidConnector1.getConnectorInfo().getConnectorId())
                            .ilpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build();
            final LedgerQuote ledgerQuote = LedgerQuote.builder()
                    .destinationConnectorInfo(ledgerConnectorInfo)
                    .transferAmount(
                            this.mockWithFX(destinationAmount, sandLedger1.getLedgerInfo().getCurrencyCode()))
                    .build();

            when(sandLedger1.getQuotingService().findBestConnector(destinationAddress, destinationAmount))
                    .thenReturn(Optional.of(ledgerQuote));
        }

        // Connector1 on SandLedger1 is the best route for Bob on SandLedger3.
        {
            final IlpAddress destinationAddress = IlpAddress.of(BOB, SAND_LEDGER3);
            final MonetaryAmount destinationAmount = MoneyUtils.toMonetaryAmount("100.00", "SND");

            final money.fluid.ilp.ledger.model.ConnectorInfo ledgerConnectorInfo =
                    money.fluid.ilp.ledger.model.ConnectorInfo.builder()
                            .connectorId(fluidConnector1.getConnectorInfo().getConnectorId())
                            .ilpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build();
            final LedgerQuote ledgerQuote;
            ledgerQuote = LedgerQuote.builder()
                    .destinationConnectorInfo(ledgerConnectorInfo)
                    .transferAmount(
                            this.mockWithFX(destinationAmount, sandLedger1.getLedgerInfo().getCurrencyCode()))
                    .build();
            when(sandLedger1.getQuotingService().findBestConnector(destinationAddress, destinationAmount))
                    .thenReturn(Optional.of(ledgerQuote));
        }

        // Connector2 on SandLedger2 is the best route for Bob on SandLedger3.
        {
            final IlpAddress destinationAddress = IlpAddress.of(BOB, SAND_LEDGER3);
            final MonetaryAmount destinationAmount = MoneyUtils.toMonetaryAmount("100.00", "SND");

            final money.fluid.ilp.ledger.model.ConnectorInfo ledgerConnectorInfo =
                    money.fluid.ilp.ledger.model.ConnectorInfo.builder()
                            .connectorId(fluidConnector2.getConnectorInfo().getConnectorId())
                            .ilpAddress(IlpAddress.of(CONNECTOR2, SAND_LEDGER2))
                            .build();
            final LedgerQuote ledgerQuote = LedgerQuote.builder()
                    .destinationConnectorInfo(ledgerConnectorInfo)
                    .transferAmount(this.mockWithFX(destinationAmount, sandLedger1.getLedgerInfo().getCurrencyCode()))
                    .build();
            when(sandLedger2.getQuotingService().findBestConnector(destinationAddress, destinationAmount))
                    .thenReturn(Optional.of(ledgerQuote));
        }

        // Connector1 on SandLedger1 is the best route for Bob on DirtLedger1.
        {
            final IlpAddress destinationAddress = IlpAddress.of(BOB, DIRT_LEDGER1);
            final MonetaryAmount destinationAmount = MoneyUtils.toMonetaryAmount("100.00", "SND");

            final money.fluid.ilp.ledger.model.ConnectorInfo ledgerConnectorInfo =
                    money.fluid.ilp.ledger.model.ConnectorInfo.builder()
                            .connectorId(fluidConnector2.getConnectorInfo().getConnectorId())
                            .ilpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build();
            final LedgerQuote ledgerQuote = LedgerQuote.builder()
                    .destinationConnectorInfo(ledgerConnectorInfo)
                    .transferAmount(this.mockWithFX(destinationAmount, sandLedger1.getLedgerInfo().getCurrencyCode()))
                    .build();
            when(sandLedger1.getQuotingService().findBestConnector(destinationAddress, destinationAmount))
                    .thenReturn(Optional.of(ledgerQuote));
        }
        // When on SandLedger1, CONNECTOR1 is best for routing to DirtLedger1 in DRT
        {
            final IlpAddress destinationAddress = IlpAddress.of(BOB, DIRT_LEDGER1);
            final MonetaryAmount destinationAmount = MoneyUtils.toMonetaryAmount("100.00", "DRT");

            final money.fluid.ilp.ledger.model.ConnectorInfo ledgerConnectorInfo =
                    money.fluid.ilp.ledger.model.ConnectorInfo.builder()
                            .connectorId(fluidConnector2.getConnectorInfo().getConnectorId())
                            .ilpAddress(IlpAddress.of(CONNECTOR1, SAND_LEDGER1))
                            .build();
            final LedgerQuote ledgerQuote = LedgerQuote.builder()
                    .destinationConnectorInfo(ledgerConnectorInfo)
                    .transferAmount(this.mockWithFX(destinationAmount, sandLedger1.getLedgerInfo().getCurrencyCode()))
                    .build();
            when(sandLedger1.getQuotingService().findBestConnector(destinationAddress, destinationAmount))
                    .thenReturn(Optional.of(ledgerQuote));
        }
    }

    /**
     * A helper method to compute the amount of a particular ledger asset to send to another ledger.  For example, if
     * the {@code destinationAmount} is in the same currency as the ledger to debit from, then simply charge a 1%
     * service fee.  Otherwise, quote
     *
     * @param destinationAmount
     * @param currencyCodeOfSourceLedger
     * @return
     */
    private MonetaryAmount mockWithFX(final MonetaryAmount destinationAmount, final String currencyCodeOfSourceLedger) {
        if (destinationAmount.getCurrency().getCurrencyCode().equals(currencyCodeOfSourceLedger)) {
            // It will take 101% if there's no FX since each connector charges a 1% fee in the absence of FX...
            final MonetaryAmount amountToReturn = MoneyUtils.toMonetaryAmount(
                    destinationAmount.multiply(1.01).getNumber().toString(), currencyCodeOfSourceLedger);
            return amountToReturn;
        } else if (destinationAmount.getCurrency().getCurrencyCode().equals(SND) && currencyCodeOfSourceLedger.equals(
                DRT)) {
            // Assume the incoming amount is in SND, and the destination is DRT, so we need to compute an amount of SND that will fulfill the transfer.
            final MonetaryAmount amountToReturn = MoneyUtils.toMonetaryAmount(
                    destinationAmount.multiply(1.1).getNumber().toString(), currencyCodeOfSourceLedger);
            return amountToReturn;
        } else if (destinationAmount.getCurrency().getCurrencyCode().equals(DRT) && currencyCodeOfSourceLedger.equals(
                SND)) {
            // Assume the incoming amount is in DRT, and the destination is SND, so we need to compute an amount of DRT
            // that will fulfill the transfer.
            final MonetaryAmount amountToReturn = MoneyUtils.toMonetaryAmount(
                    destinationAmount.multiply(0.909090).getNumber().toString(), currencyCodeOfSourceLedger);
            return amountToReturn;
        } else {
            throw new RuntimeException(
                    String.format(
                            "Unhandled currency conversion mock: %s, currencyCodeOfSourceLedger: %s", destinationAmount,
                            currencyCodeOfSourceLedger
                    ));
        }
    }


    private void assertInitialAmounts() {
        assertLedgerAccount(sandLedger1, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, ALICE, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, BOB, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR1, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger2, CONNECTOR2, INITIAL_AMOUNT);
        assertLedgerAccount(sandLedger1, ESCROW, ZERO_AMOUNT);
        assertLedgerAccount(sandLedger2, ESCROW, ZERO_AMOUNT);
    }
}
