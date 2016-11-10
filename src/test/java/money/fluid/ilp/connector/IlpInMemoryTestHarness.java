package money.fluid.ilp.connector;

import com.google.common.collect.ImmutableSet;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.connector.services.routing.DefaultRoute;
import money.fluid.ilp.connector.services.routing.RoutingService;
import money.fluid.ilp.ledger.LedgerAccountManager;
import money.fluid.ilp.ledger.QuotingService;
import money.fluid.ilp.ledger.inmemory.InMemoryLedger;
import money.fluid.ilp.ledger.inmemory.model.DefaultLedgerInfo;
import money.fluid.ilp.ledger.inmemory.model.DefaultLedgerTransfer;
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
import org.javamoney.moneta.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

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

    // The currency code for the "Sand" ledger.  Denominated in granules of sand.
    private static final String SND = "SND";
    private static final String SAND_CURRENCY_SYMBOL = "(S)";

    // The currency code for the "Dirt" ledger.  Denominated in granules of sand.
    private static final String DRT = "DRT";
    private static final String DIRT_CURRENCY_SYMBOL = "(D)";

    // This is the unique account identifier for the connector.  For simplicity, it is the same on each in-memory ledger
    // for purposes of this test.  In reality, this account identifier would be a configuration option of the connector
    // on a per-ledger basis.
    private static final LedgerAccountId CONNECTOR = LedgerAccountId.of("fluid-connector-27");

    private static final LedgerAccountId ALICE = LedgerAccountId.of("alice");
    private static final LedgerAccountId BOB = LedgerAccountId.of("bob");
    private static final LedgerAccountId CHLOE = LedgerAccountId.of("chloe");
    private static final LedgerAccountId ESCROW = LedgerAccountId.of("__escrow__");

    private static final LedgerId SAND_LEDGER1 = LedgerId.of("sand-ledger1.example.com");
    private static final LedgerId SAND_LEDGER2 = LedgerId.of("sand-ledger2.example.com");
    private static final LedgerId DIRT_LEDGER1 = LedgerId.of("dirt-ledger1.example.com");
    private static final LedgerId DIRT_LEDGER2 = LedgerId.of("dirt-ledger2.example.com");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Mock
    private RoutingService routingServiceMock;

    @Mock
    private QuotingService quotingServiceMock;

    //////////
    // Ledgers
    //////////

    // Ledgers for tracking the SND asset.
    private InMemoryLedger sandLedger1;
    private InMemoryLedger sandLedger2;
    // Ledgers for tracking the DRT asset.
    private InMemoryLedger dirtLedger1;

    /////////////////
    // Ledger Clients
    /////////////////

    // This is the ILP code that allows this connector to interface with the Sand Ledger.
    private LedgerClient sandLedger1Client;
    private LedgerClient sandLedger2Client;

    // This is the ILP code that allows this connector to inteface with the Dirt Ledger.
    private LedgerClient dirtLedger1Client;

    /////////////
    // Connectors
    /////////////

    // The Fluid Money connector that has accounts on both the Sand and Dirt ledgers.
    private Connector fluidConnector;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

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
        this.sandLedger1 = new InMemoryLedger("Sand Ledger 1", sandLedger1Info, quotingServiceMock);
        this.initializeLedgerAccounts(sandLedger1.getLedgerAccountManager());

        // Initialize Sand Ledger2
        final LedgerInfo sandLedger2Info = new DefaultLedgerInfo(
                precision, scale, SND, SAND_CURRENCY_SYMBOL, SAND_LEDGER2
        );
        this.sandLedger2 = new InMemoryLedger("Sand Ledger 2", sandLedger2Info, quotingServiceMock);
        this.initializeLedgerAccounts(sandLedger2.getLedgerAccountManager());

        // Initialize Dirt Ledger1
        final LedgerInfo dirtLedger1Info = new DefaultLedgerInfo(
                precision, scale, DRT, DIRT_CURRENCY_SYMBOL, DIRT_LEDGER1
        );
        this.dirtLedger1 = new InMemoryLedger("Dirt Ledger 1", dirtLedger1Info, quotingServiceMock);
        this.initializeLedgerAccounts(dirtLedger1.getLedgerAccountManager());

        //######################
        // Initialize Ledger Clients
        //######################

        // Just a random connector id...don't read too much into it.  Hard-coded for now so the ledgerAccountId and the
        // connectorId are the same for simulation purposes.
        final ConnectorId connectorId = ConnectorId.of(CONNECTOR.getId());

        // Each LedgerClient creates a single connection to a connector, and then handles all events via that
        // single connection.
        this.sandLedger1Client = new InMemoryLedgerClient(
                ConnectionInfo.builder().clientId("SandLedger1").clientVersion("0.0.1").connectorId(
                        connectorId).connectorId(
                        connectorId).build(),
                sandLedger1
        );
        this.sandLedger2Client = new InMemoryLedgerClient(
                ConnectionInfo.builder().clientId("SandLedger2").clientVersion("0.0.1").connectorId(
                        connectorId).build(),
                sandLedger2
        );
        this.dirtLedger1Client = new InMemoryLedgerClient(
                ConnectionInfo.builder().clientId("DirtLedger1").clientVersion("0.0.1").connectorId(
                        connectorId).build(),
                dirtLedger1
        );

        //######################
        // Initialize Connector(s)
        //######################

        final ConnectorInfo fluidConnectorInfo = ConnectorInfo.builder().connectorId(connectorId).optLedgerAccountId(
                Optional.empty()).build();

        // For simulation purposes, this connector has the same account identifier on all in-memory ledgers.
        final ImmutableSet<LedgerClient> ledgerClients = ImmutableSet.of(
                sandLedger1Client, sandLedger2Client, dirtLedger1Client);

        this.fluidConnector = new DefaultConnector(fluidConnectorInfo, ledgerClients, this.routingServiceMock);
    }


    @After
    public void tearDown() {
        // Disconnect each LedgerClient...
        fluidConnector.getLedgerClients().stream().forEach(simpleLedger -> simpleLedger.disconnect());
    }

    /**
     * This test simulates a payment from one account to another on the same ledger, in this case the "Sand" ledger.
     * <p>
     * The "Sand" ledger is a simulated in-memory ledger implemented by {@link InMemoryLedger} that tracks granules of
     * sand using the currency code "SND".  In this test, Alice will send SND 25 to Bob.  Since the transfer occurs on
     * the same ledger, there is no need for a crypto-condition, or any other typical ILP facilities.  This is because
     * the ledger knows both sides of the transaction and can provide all trust, liquidity, and transfer services.
     * <p>
     * However, since the transfer came into the Ledger with ILP instructions and ILP recipients, I would make the case
     * that the Ledger _should_ still process this as an ILP transaction, as best it can (i.e., without needing to
     * involve a connector and likely without needing to involve crypto-conditions (?)).  In other words, the ledger
     * should still put money on hold and give the recipient a chance to reject the money since in general a ledger
     * doesn't want to allow its account holders to send money into accounts without the receiving account holder's
     * approval of the transfer.
     * <p>
     * Counter-point: there is a strong case that this kind of thing doesn't need to involve ILP at all, so just let
     * each ledger do its thing.
     */
    @Test
    public void testScenario1() {
        //Mock the QuotingService for now...
        //TODO: eventually this will be replaced with a real service that integrates with routing.
        when(quotingServiceMock.findBestConnector(any())).thenReturn(
                Optional.of(this.fluidConnector.getConnectorInfo()));

        final DefaultLedgerTransfer ledgerTransfer =
                new DefaultLedgerTransfer(
                        IlpAddress.of(ALICE, SAND_LEDGER1),
                        IlpAddress.of(BOB, SAND_LEDGER1),
                        Money.of(25, "SND")
                );

        // Simulate a wallet or other entity initiating a transfer from Alice to Bob.
        sandLedger1.send(ledgerTransfer);

        // Simulate Bob's acceptance of the Transfer by fulfilling by ILP Transaction Id...
        sandLedger1.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(IlpAddress.of(ALICE, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(new BigDecimal("475.00"), SND))
        );
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(IlpAddress.of(BOB, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(new BigDecimal("525.00"), SND))
        );
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(
                        IlpAddress.of(CONNECTOR, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(new BigDecimal("500"), SND))
        );
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(
                        IlpAddress.of(ESCROW, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(BigDecimal.ZERO, SND))
        );
    }

    /**
     * This test simulates a fixed-source-amount payment from one account to another on different ledgers that share the
     * same asset type, connected by a single connector.
     * <p>
     * In this case, Alice will send SND 100 from {@code sandLedger1} to Bob's account on {@code sandLedger2} via the
     * Fluid Connector.  This test operates as follows:
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
    public void testScenario2a_FixedSourceAmount_DifferentLedgerWithSameAssetType() {
        //Mock the QuotingService for now...
        //TODO: eventually this will be replaced with a real service that integrates with routing.
        when(quotingServiceMock.findBestConnector(any())).thenReturn(
                Optional.of(this.fluidConnector.getConnectorInfo()));

        // Step1: Assemble and send an amount of SND to Bob on the Sand2 Ledger.
        final LedgerTransfer ledgerTransfer = new DefaultLedgerTransfer(
                IlpAddress.of(ALICE, SAND_LEDGER1),
                IlpAddress.of(BOB, SAND_LEDGER2),
                Money.of(100, "SND")
        );

        // SandLedger 2 is configured to automatically accept any payment and fulfill the condition, so there's nothing
        // more to do here except wait for the synchronous #send call to finish.  In a real system, the processing would
        // be async, so for a true integration test/simulation we would need to wait some time before checking assertions.
        sandLedger1.send(ledgerTransfer);

        // Simulate BOB accepting funds on Ledger 2.
        sandLedger2.fulfillCondition(ledgerTransfer.getInterledgerPacketHeader().getIlpTransactionId());

        // Immediately after sandLedger2 fulfills the condition (i.e., accepts the payment), all ledgers will have
        // received funds since this is an optimistic transfer and a synchronous unit test.
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(IlpAddress.of(ALICE, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(new BigDecimal("400"), SND))
        );
        assertThat(
                sandLedger2.getLedgerAccountManager().getAccount(IlpAddress.of(BOB, SAND_LEDGER2)).get().getBalance(),
                is(Money.of(new BigDecimal("600"), SND))
        );
        // Connector has no more than it started with since there are no transaction fees in this test.
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(
                        IlpAddress.of(CONNECTOR, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(new BigDecimal("600"), SND))
        );
        // Connector has no more than it started with since there are no transaction fees in this test.
        assertThat(
                sandLedger2.getLedgerAccountManager().getAccount(
                        IlpAddress.of(CONNECTOR, SAND_LEDGER2)).get().getBalance(),
                is(Money.of(new BigDecimal("400"), SND))
        );

        // Escrow accounts on both ledgers should be 0 since all escrowed funds have been executed.
        assertThat(
                sandLedger1.getLedgerAccountManager().getAccount(
                        IlpAddress.of(ESCROW, SAND_LEDGER1)).get().getBalance(),
                is(Money.of(BigDecimal.ZERO, SND))
        );
        assertThat(
                sandLedger2.getLedgerAccountManager().getAccount(
                        IlpAddress.of(ESCROW, SAND_LEDGER2)).get().getBalance(),
                is(Money.of(BigDecimal.ZERO, SND))
        );
    }

    // TODO: Implement the following scenarios

    // OPTIMISTIC Mode Rejections
    // * fixed source/destination-amount payments from one account to another on different ledgers that have same/different asset types (1 Connector) where the recipient rejects funds.

    // OPTIMISTIC Mode
    // * fixed source/destination-amount payments from one account to another on different ledgers that have different asset types (1 Connector)
    // * fixed source/destination-amount payments from one account to another on different ledgers that share the same asset type (N Connectors) -> Chase to BofA
    // * fixed source/destination-amount payments from one account to another on different ledgers that have different asset types (N Connectors) -> Chase to Bitcoin

    // With Conditions
    // * fixed source/destination-amount payments from one account to another on different ledgers that have the same asset types (1 Connector)
    // * fixed source/destination-amount payments from one account to another on different ledgers that have different asset types (1 Connector)
    // * fixed source/destination-amount payments from one account to another on different ledgers that share the same asset type (N Connectors) -> Chase to BofA
    // * fixed source/destination-amount payments from one account to another on different ledgers that have different asset types (N Connectors) -> Chase to Bitcoin

    // With transaction fess...

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
        // These are merely simply routes to enable the test harness so we don't have to implement the entire routing library.
        final DefaultRoute defaultRouteToAliceAtSand1 = DefaultRoute.builder()
                .sourceAddress(IlpAddress.of(CONNECTOR, SAND_LEDGER1))
                .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER1))
                .optExpiresAt(Optional.empty())
                .build();
        final DefaultRoute defaultRouteToBobAtSand1 = DefaultRoute.builder()
                .sourceAddress(IlpAddress.of(CONNECTOR, SAND_LEDGER1))
                .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER1))
                .optExpiresAt(Optional.empty())
                .build();
        final DefaultRoute defaultRouteToBobAtSand2 = DefaultRoute.builder()
                .sourceAddress(IlpAddress.of(CONNECTOR, SAND_LEDGER2))
                .destinationAddress(IlpAddress.of(BOB, SAND_LEDGER2))
                .optExpiresAt(Optional.empty())
                .build();

        when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(ALICE, SAND_LEDGER1)), any()))
                .thenReturn(Optional.of(defaultRouteToAliceAtSand1));
        when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER1)), any()))
                .thenReturn(Optional.of(defaultRouteToBobAtSand1));
        when(routingServiceMock.bestHopForDestinationAmount(eq(IlpAddress.of(BOB, SAND_LEDGER2)), any()))
                .thenReturn(Optional.of(defaultRouteToBobAtSand2));
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
                ledgerAccountManager, IlpAddress.of(CONNECTOR, ledgerAccountManager.getLedgerInfo().getLedgerId()));
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

}
