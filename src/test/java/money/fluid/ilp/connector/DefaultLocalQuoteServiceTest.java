package money.fluid.ilp.connector;

import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.connector.services.ConnectedLedgerService;
import money.fluid.ilp.connector.services.ConnectorFeeService;
import money.fluid.ilp.connector.services.WhoAmIService;
import money.fluid.ilp.connector.services.quoting.QuoteService;
import money.fluid.ilp.connector.services.quoting.impl.DefaultLocalQuoteService;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Unit tests for {@link DefaultLocalQuoteService}.
 */
public class DefaultLocalQuoteServiceTest {

    private static final LedgerId SOURCE_LEDGER_ID = new LedgerId("sourceLedger");
    private static final LedgerId DESTINATION_LEDGER_ID = new LedgerId("destinationLedger");

    private static final LedgerAccountId SOURCE_ESCROW_ACCOUNT_ID = new LedgerAccountId("sourceEscrowAccount");
    private static final LedgerAccountId DESTINATION_ESCROW_ACCOUNT_ID = new LedgerAccountId("destinationEscrowAccount");

    private static final Optional<LedgerAccountId> OPT_SOURCE_ESCROW_ACCOUNT_ID = Optional.of(SOURCE_ESCROW_ACCOUNT_ID);
    private static final Optional<LedgerAccountId> OPT_DESTINATION_ESCROW_ACCOUNT_ID = Optional.of(
            DESTINATION_ESCROW_ACCOUNT_ID);

    private static final LedgerAccountId SOURCE_FEE_ACCOUNT_ID = new LedgerAccountId("sourceFeeAccount");
    private static final LedgerAccountId DESTINATION_FEE_ACCOUNT_ID = new LedgerAccountId("destinationFeeAccount");

    private static final Optional<LedgerAccountId> OPT_SOURCE_FEE_ACCOUNT_ID = Optional.of(SOURCE_FEE_ACCOUNT_ID);
    private static final Optional<LedgerAccountId> OPT_DESTINATION_FEE_ACCOUNT_ID = Optional.of(DESTINATION_FEE_ACCOUNT_ID);


    private static final Optional<BigDecimal> SOURCE_AMOUNT = Optional.of(BigDecimal.TEN);
    private static final Optional<BigDecimal> DESTINATION_AMOUNT = Optional.of(BigDecimal.TEN);
    private static final BigDecimal CONNECTOR_FEE = new BigDecimal("0.015");

    @Mock
    private WhoAmIService whoAmIServiceMock;
    @Mock
    private ConnectedLedgerService connectedLedgerServiceMock;
    @Mock
    private ConnectorFeeService connectorFeeServiceMock;

    private QuoteService defaultLocalQuoteService;

//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//
//        when(this.connectedLedgerServiceMock.getConnectorFeeAccountIdForLedger(SOURCE_LEDGER_ID)).thenReturn(
//                OPT_SOURCE_FEE_ACCOUNT_ID);
//        when(this.connectedLedgerServiceMock.getConnectorFeeAccountIdForLedger(DESTINATION_LEDGER_ID)).thenReturn(
//                OPT_DESTINATION_FEE_ACCOUNT_ID);
//
//        when(this.connectedLedgerServiceMock.getEscrowAccountIdForLedger(SOURCE_LEDGER_ID)).thenReturn(
//                OPT_SOURCE_ESCROW_ACCOUNT_ID);
//        when(this.connectedLedgerServiceMock.getEscrowAccountIdForLedger(DESTINATION_LEDGER_ID)).thenReturn(
//                OPT_DESTINATION_ESCROW_ACCOUNT_ID);
//
//        when(this.connectorFeeServiceMock.calculateConnectorFee(any())).thenReturn(CONNECTOR_FEE);
//        when(this.connectorFeeServiceMock.calculateConnectorFee(any())).thenReturn(CONNECTOR_FEE);
//
//        this.defaultLocalQuoteService = new DefaultLocalQuoteService(
//                whoAmIServiceMock, connectedLedgerServiceMock, connectorFeeServiceMock
//        );
//    }
//
//    ////////////////////
//    // Constructor Tests
//    ////////////////////
//
//    @Test(expected = NullPointerException.class)
//    public void constructorTest_NullArg1() {
//        new DefaultLocalQuoteService(
//                null, connectedLedgerServiceMock, connectorFeeServiceMock
//        );
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void constructorTest_NullArg2() {
//        new DefaultLocalQuoteService(
//                whoAmIServiceMock, null, connectorFeeServiceMock
//        );
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void constructorTest_NullArg3() {
//        new DefaultLocalQuoteService(
//                whoAmIServiceMock, connectedLedgerServiceMock, null
//        );
//    }
//
//    ////////////////////
//    // Test #getQuoteForFixedSourceAmount
//    ////////////////////
//
//    @Test(expected = NullPointerException.class)
//    public void getQuoteForFixedSourceAmount_NullSource() throws Exception {
//        this.defaultLocalQuoteService.getQuote(null, mock(QuoteRequest.class));
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void getQuoteForFixedSourceAmount_NullDestination() throws Exception {
//        this.defaultLocalQuoteService.getQuote(mock(QuoteRequest.class), null);
//    }
//
//
//    @Test(expected = InvalidQuoteRequestException.class)
//    public void getQuoteForFixedSourceAmount_NullSourceLedgerId() throws Exception {
//        final QuoteRequest sourceRequest = mock(QuoteRequest.class);
//        when(sourceRequest.getLedgerId()).thenReturn(null);
//        final QuoteRequest destinationRequest = mock(QuoteRequest.class);
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InvalidQuoteRequestException e) {
//            assertThat(e.getMessage(), is("Source Ledger Id must be specified!"));
//            throw e;
//        }
//    }
//
//    @Test(expected = InvalidQuoteRequestException.class)
//    public void getQuoteForFixedSourceAmount_NullDestinationLedgerId() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(false);
//        when(destinationRequest.getLedgerId()).thenReturn(null);
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InvalidQuoteRequestException e) {
//            assertThat(e.getMessage(), is("Destination Ledger Id must be specified!"));
//            throw e;
//        }
//    }
//
//    @Test(expected = InvalidQuoteRequestException.class)
//    public void getQuoteForFixedSourceAmount_NoAmountsSpecified() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        when(sourceRequest.getOptAmount()).thenReturn(Optional.empty());
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(false);
//        when(destinationRequest.getOptAmount()).thenReturn(Optional.empty());
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InvalidQuoteRequestException e) {
//            assertThat(e.getMessage(), is("Either the Source or Destination amount must be specified!"));
//            throw e;
//        }
//    }
//
//    @Test(expected = InvalidQuoteRequestException.class)
//    public void getQuoteForFixedSourceAmount_BothAmountsSpecified() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(true);
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InvalidQuoteRequestException e) {
//            assertThat(e.getMessage(), is("Only the Source or Destination amount may be specified!"));
//            throw e;
//        }
//    }
//
//
//    @Test(expected = InsufficientFundsException.class)
//    public void getQuoteForFixedSourceAmount_ConnectorNotConnectedToSource() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(false);
//
//        when(this.connectedLedgerServiceMock.getEscrowAccountIdForLedger(SOURCE_LEDGER_ID)).thenReturn(
//                Optional.empty());
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InsufficientFundsException e) {
//            assertThat(e.getMessage(), is("No Account found on the requested source Ledger!"));
//            throw e;
//        }
//    }
//
//    @Test
//    public void getQuoteForFixedSourceAmount_ConnectorNotConnectedToDestination() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(false);
//
//        try {
//            this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//        } catch (InsufficientFundsException e) {
//            assertThat(e.getMessage(), is("No Account found on the requested destination Ledger!"));
//            throw e;
//        }
//    }

//    @Test
//    public void getQuoteForFixedSourceAmount() throws Exception {
//        final QuoteRequest sourceRequest = getSourceTransferRequestMock(true);
//        final QuoteRequest destinationRequest = getDestinationTransferRequestMock(false);
//
//        final Quote actual = this.defaultLocalQuoteService.getQuote(sourceRequest, destinationRequest);
//
//        assertThat(actual.getSourceTransfers().size(), is(2));
//        assertThat(actual.getDestinationTransfers().size(), is(3));
//
//
//        {
//            final Collection<Transfer> sourceTransfers = actual.getSourceTransfers();
//            // Source Escrow Credit
//            final Optional<Transfer> optSourceEscrow = this.findSourceEscrow(sourceTransfers);
//            assertThat(optSourceEscrow.isPresent(), is(true));
//            final Transfer sourceEscrow = optSourceEscrow.get();
//            assertThat(sourceEscrow.getLedgerId(), is(SOURCE_LEDGER_ID));
//            assertThat(sourceEscrow.getOptExpiryDuration(), is(Optional.empty()));
//            assertThat(sourceEscrow.getOptDebit().isPresent(), is(false));
//            assertThat(sourceEscrow.getOptCredit().get().getAmount(), is(SOURCE_AMOUNT.get()));
//            assertThat(sourceEscrow.getOptCredit().get().getOptAccountId().get(), is(SOURCE_ESCROW_ACCOUNT_ID));
//        }
//        {
//            final Collection<Transfer> sourceTransfers = actual.getSourceTransfers();
//            // Source Funds Debit
//            final Optional<Transfer> optSourceFunds = this.findSourceFunds(sourceTransfers);
//            assertThat(optSourceFunds.isPresent(), is(true));
//            final Transfer sourceFunds = optSourceFunds.get();
//            assertThat(sourceFunds.getLedgerId(), is(SOURCE_LEDGER_ID));
//            assertThat(sourceFunds.getOptExpiryDuration(), is(Optional.empty()));
//            assertThat(sourceFunds.getOptCredit().isPresent(), is(false));
//            assertThat(sourceFunds.getOptDebit().get().getAmount(), is(SOURCE_AMOUNT.get()));
//            assertThat(sourceFunds.getOptDebit().get().getOptAccountId(), is(Optional.empty()));
//        }
//        {
//            final Collection<Transfer> destinationTransfers = actual.getDestinationTransfers();
//
//            // Destination Escrow (Debit)
//            final Optional<Transfer> optDestinationEscrow = this.findDestinationEscrow(destinationTransfers);
//            assertThat(optDestinationEscrow.isPresent(), is(true));
//            final Transfer destinationEscrow = optDestinationEscrow.get();
//            assertThat(destinationEscrow.getLedgerId(), is(DESTINATION_LEDGER_ID));
//            assertThat(destinationEscrow.getOptExpiryDuration(), is(Optional.empty()));
//            assertThat(destinationEscrow.getOptCredit().isPresent(), is(false));
//            assertThat(destinationEscrow.getOptDebit().get().getAmount(), is(DESTINATION_AMOUNT.get()));
//            assertThat(
//                    destinationEscrow.getOptDebit().get().getOptAccountId().get(), is(DESTINATION_ESCROW_ACCOUNT_ID));
//        }
//        {
//            final Collection<Transfer> destinationTransfers = actual.getDestinationTransfers();
//
//            // Connector Fee (Credit)
//            final Optional<Transfer> optDestinationFee = this.findDestinationFee(destinationTransfers);
//            assertThat(optDestinationFee.isPresent(), is(true));
//            final Transfer destinationFee = optDestinationFee.get();
//            assertThat(destinationFee.getLedgerId(), is(DESTINATION_LEDGER_ID));
//            assertThat(destinationFee.getOptExpiryDuration(), is(Optional.empty()));
//            assertThat(destinationFee.getOptDebit().isPresent(), is(false));
//            assertThat(destinationFee.getOptCredit().get().getAmount(), is(CONNECTOR_FEE));
//            assertThat(destinationFee.getOptCredit().get().getOptAccountId().get(), is(DESTINATION_FEE_ACCOUNT_ID));
//        }
//        {
//            final Collection<Transfer> destinationTransfers = actual.getDestinationTransfers();
//
//            // Destination Funds (Credit)
//            final Optional<Transfer> optDestinationFunds = this.findDestinationFunds(destinationTransfers);
//            assertThat(optDestinationFunds.isPresent(), is(true));
//            final Transfer destinationFunds = optDestinationFunds.get();
//            assertThat(destinationFunds.getLedgerId(), is(DESTINATION_LEDGER_ID));
//            assertThat(destinationFunds.getOptExpiryDuration(), is(Optional.empty()));
//            assertThat(destinationFunds.getOptDebit().isPresent(), is(false));
//            assertThat(destinationFunds.getOptCredit().get().getAmount(), is(DESTINATION_AMOUNT.get()));
//            assertThat(destinationFunds.getOptCredit().get().getOptAccountId(), is(Optional.empty()));
//        }
//
//    }
//
//
//    ////////////////////
//    // Test #getQuoteForFixedDestinationAmount
//    ////////////////////
//
//    @Test
//    public void getQuoteForFixedDestinationAmount() throws Exception {
//        fail();
//    }
//
//
//    //////////////////
//    // Private Helpers
//    //////////////////
//
//    private QuoteRequest getSourceTransferRequestMock(final boolean specifyAmount) {
//        final QuoteRequest sourceRequest = mock(QuoteRequest.class);
//        when(sourceRequest.getLedgerId()).thenReturn(SOURCE_LEDGER_ID);
//        if (specifyAmount) {
//            when(sourceRequest.getOptAmount()).thenReturn(SOURCE_AMOUNT);
//        } else {
//            when(sourceRequest.getOptAmount()).thenReturn(Optional.empty());
//        }
//        when(sourceRequest.getOptExpiryDuration()).thenReturn(Optional.empty());
//        return sourceRequest;
//    }
//
//    private QuoteRequest getDestinationTransferRequestMock(final boolean specifyAmount) {
//        final QuoteRequest destinationRequest = mock(QuoteRequest.class);
//        when(destinationRequest.getLedgerId()).thenReturn(DESTINATION_LEDGER_ID);
//        if (specifyAmount) {
//            when(destinationRequest.getOptAmount()).thenReturn(DESTINATION_AMOUNT);
//        } else {
//            when(destinationRequest.getOptAmount()).thenReturn(Optional.empty());
//        }
//        when(destinationRequest.getOptExpiryDuration()).thenReturn(Optional.empty());
//        return destinationRequest;
//    }
//
//    private Optional<Transfer> findSourceEscrow(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(SOURCE_LEDGER_ID)
//                    && transferPart.getOptCredit().isPresent()
//                    && transferPart.getOptCredit().get().getOptAccountId().isPresent()
//                    && transferPart.getOptCredit().get().getOptAccountId().get().equals(
//                    SOURCE_ESCROW_ACCOUNT_ID)) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
//
//    private Optional<Transfer> findSourceFunds(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(SOURCE_LEDGER_ID)
//                    && !transferPart.getOptCredit().isPresent()
//                    && transferPart.getOptDebit().isPresent()
//                    && !transferPart.getOptDebit().get().getOptAccountId().isPresent()) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
//
//    private Optional<Transfer> findDestinationEscrow(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(DESTINATION_LEDGER_ID)
//                    && !transferPart.getOptCredit().isPresent()
//                    && transferPart.getOptDebit().isPresent()
//                    && transferPart.getOptDebit().get().getOptAccountId().isPresent()
//                    && transferPart.getOptDebit().get().getOptAccountId().get().equals(DESTINATION_ESCROW_ACCOUNT_ID)) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
//
//    private Optional<Transfer> findDestinationFunds(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(DESTINATION_LEDGER_ID)
//                    && !transferPart.getOptDebit().isPresent()
//                    && transferPart.getOptCredit().isPresent()
//                    && !transferPart.getOptCredit().get().getOptAccountId().isPresent()) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
//
//    private Optional<Transfer> findDestinationFee(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(DESTINATION_LEDGER_ID) && transferPart.getOptCredit().isPresent()
//                    && transferPart.getOptCredit().get().getOptAccountId().isPresent() && transferPart.getOptCredit().get().getOptAccountId().get().equals(
//                    DESTINATION_FEE_ACCOUNT_ID)) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
//
//    private Optional<Transfer> findSourceFee(Collection<Transfer> sourceTransfers) {
//        Objects.requireNonNull(sourceTransfers);
//        return sourceTransfers.stream().filter((transferPart -> {
//            if (transferPart.getLedgerId().equals(SOURCE_LEDGER_ID) && transferPart.getOptCredit().isPresent()
//                    && transferPart.getOptCredit().get().getOptAccountId().isPresent() && transferPart.getOptCredit().get().getOptAccountId().get().equals(
//                    SOURCE_FEE_ACCOUNT_ID)) {
//                return true;
//            } else {
//                return false;
//            }
//        })).findFirst();
//    }
}