package money.fluid.ilp.connector.services.quoting.impl;

import com.google.common.base.Preconditions;
import money.fluid.ilp.connector.exceptions.InsufficientFundsException;
import money.fluid.ilp.connector.exceptions.InvalidQuoteRequestException;
import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.connector.model.quotes.Credit;
import money.fluid.ilp.connector.model.quotes.Debit;
import money.fluid.ilp.connector.model.quotes.Quote;
import money.fluid.ilp.connector.model.quotes.QuoteRequest;
import money.fluid.ilp.connector.model.quotes.Transaction;
import money.fluid.ilp.connector.services.ConnectedLedgerService;
import money.fluid.ilp.connector.services.ConnectorFeeService;
import money.fluid.ilp.connector.services.ConnectorFeeService.ConnectorFeeInfo;
import money.fluid.ilp.connector.services.ExchangeRateService;
import money.fluid.ilp.connector.services.ExchangeRateService.ExchangeRateInfo;
import money.fluid.ilp.connector.services.SupportedAssetsService;
import money.fluid.ilp.connector.services.quoting.QuoteService.LocalQuoteService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * An implementation of {@link LocalQuoteService} that always returns a valid quote with a service fee calculated by the
 * supplied {@link ConnectorFeeService}.
 * <p>
 * WARNING: This implementation assumes infinite liquidity, and should therefore not be used in a production scenario.
 * Under normal circumstances, infinite liquidity is generally not possible except perhaps for a connector serviced by a
 * government entity. Thus, an improved implementation of this service would potentially increase its service fee as
 * liquidity decreases.
 */
@Service
@Qualifier("default")
public class DefaultLocalQuoteService extends AbstractQuoteService implements LocalQuoteService {

    //TODO: Make the attributes of this connector configurable so that installers may set various attributes.
    //TODO: Make this implementation pluggable so that implementors can supply their own implementations.

    private final SupportedAssetsService supportedAssetsService;
    private final ConnectedLedgerService connectedLedgerService;
    private final ConnectorFeeService connectorFeeService;
    private final ExchangeRateService exchangeRateService;

    /**
     * Required args Constructor.
     *
     * @param supportedAssetsService An instance of {@link SupportedAssetsService}.
     * @param connectedLedgerService An instance of {@link ConnectedLedgerService}.
     * @param connectorFeeService    An instance of {@link ConnectorFeeService}.
     * @param exchangeRateService    An instance of {@link ExchangeRateService}.
     */
    @Inject
    public DefaultLocalQuoteService(
            final SupportedAssetsService supportedAssetsService, final ConnectedLedgerService connectedLedgerService,
            final ConnectorFeeService connectorFeeService, final ExchangeRateService exchangeRateService
    ) {
        this.supportedAssetsService = Objects.requireNonNull(supportedAssetsService);
        this.connectedLedgerService = Objects.requireNonNull(connectedLedgerService);
        this.connectorFeeService = Objects.requireNonNull(connectorFeeService);
        this.exchangeRateService = Objects.requireNonNull(exchangeRateService);
    }

    @Override
    protected Quote getQuoteForFixedSourceAmount(
            final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest
    ) {
        Objects.requireNonNull(sourceQuoteRequest);
        Objects.requireNonNull(destinationQuoteRequest);

        //////////////
        // Source
        //////////////

        // Capture the amount requested by the sender to send to a recipient account.  Since the source is fixed, the destination
        // amount will fluctuate to "pay" for the connectorFee, if any.
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(sourceQuoteRequest.getOptAmount().isPresent());
        final BigDecimal sourceTransferAmount = sourceQuoteRequest.getOptAmount().get();

        // Capture the Account on the source ledger that will fund the connector.
        final Optional<LedgerAccountId> optConnectorSourceEscrowAccount = this.connectedLedgerService.getEscrowAccountIdForLedger(
                sourceQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(optConnectorSourceEscrowAccount.isPresent());
        final LedgerAccountId connectorSourceEscrowAccountId = optConnectorSourceEscrowAccount.get();

        //////////////
        // Destination
        //////////////

        // Get the Account on the destination ledger that will fund the connector.
        final Optional<LedgerAccountId> optConnectorDestinationAccount = this.connectedLedgerService.getEscrowAccountIdForLedger(
                destinationQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(optConnectorDestinationAccount.isPresent());
        final LedgerAccountId connectorDestinationEscrowAccountId = optConnectorDestinationAccount.get();

        ////////////////////
        // Connector Fees
        ////////////////////

        // Since this is a fixed-source transfer, the connectorFee must be deducted from the amount sent to the other side of
        // the connector.  Thus, we quote the connectorFee in terms of the destination Leger/Account combination.
        final Optional<LedgerAccountId> optCommissionAccount = this.connectedLedgerService.getConnectorFeeAccountIdForLedger(
                destinationQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        final LedgerAccountId connectorDestinationCommissionAccountId = optCommissionAccount.get();


        final AssetId sourceAssetId = this.supportedAssetsService.getAssetIdForLedger(sourceQuoteRequest.getLedgerId());
        final AssetId destinationAssetId = this.supportedAssetsService.getAssetIdForLedger(
                destinationQuoteRequest.getLedgerId());
        final ExchangeRateInfo exchangeRateInfo = this.exchangeRateService.getExchangeRate(
                sourceAssetId, destinationAssetId);

        // Since the source is fixed, the destination amount will fluctuate to "pay" for the connectorFee, if any.
        final ConnectorFeeInfo connectorFeeInfo = this.connectorFeeService.calculateConnectorFee(
                exchangeRateInfo.getDestinationAssetId(), exchangeRateInfo.convert(sourceTransferAmount)
        );

        if (!isPositive(connectorFeeInfo.getAmountAfterFee())) {
            throw new InsufficientFundsException(
                    "Not enough funds in the source transfer amount to fund the destination transfer!");
        }

        // This transaction will include various transfers:
        // SOURCE LEDGER
        // 1.) A debit from the sender's account to fund the transfer.
        // 2.) A credit to the connector's escrow account on the source ledger.
        //
        // DESTINATION LEDGER
        // 3.) A debit from the connector's escrow account to fund the transfer.
        // 4.) A credit to the recipient's account for the actual transfer (minus connector fees).
        // 5.) A credit to the connector's fee account on the destination ledger for the connector fee.

        // #1
        // The accountId is unknown for a quote, to preserve privacy.  Only the ledger is known.
        final Debit sourceDebitFromSender
                = new Debit.Builder(sourceQuoteRequest.getLedgerId(), sourceTransferAmount)
                .withOptExpiryDuration(sourceQuoteRequest.getOptExpiryDuration())
                .build();

        // #2
        final Credit sourceCreditToConnector
                = new Credit.Builder(sourceQuoteRequest.getLedgerId(), sourceTransferAmount)
                .withAccountId(connectorSourceEscrowAccountId)
                // TODO: Ensure the specified expiry isn't longer than the max allowed by the ledger, or too short.
                .withExpiryDuration(
                        sourceQuoteRequest.getOptExpiryDuration().orElse(
                                this.connectedLedgerService.getDefaultExpiration(sourceQuoteRequest.getLedgerId())))
                .build();

        // #3
        final Debit destinationDebitFromConnector
                = new Debit.Builder(destinationQuoteRequest.getLedgerId(), connectorFeeInfo.getOriginalAmount())
                .withAccountId(connectorDestinationEscrowAccountId)
                // TODO: Ensure the specified expiry isn't longer than the max allowed by the ledger, or too short.
                .withExpiryDuration(destinationQuoteRequest.getOptExpiryDuration().orElse(
                        this.connectedLedgerService.getDefaultExpiration(destinationQuoteRequest.getLedgerId())))
                .build();

        // #4
        final Credit destinationCreditForRecipient
                = new Credit.Builder(destinationQuoteRequest.getLedgerId(), connectorFeeInfo.getAmountAfterFee())
                .withOptExpiryDuration(destinationQuoteRequest.getOptExpiryDuration())
                .build();

        // #5
        final Credit destinationCreditForConnectorFees
                = new Credit.Builder(destinationQuoteRequest.getLedgerId(), connectorFeeInfo.getFee())
                .withAccountId(connectorDestinationCommissionAccountId)
                .withOptExpiryDuration(destinationQuoteRequest.getOptExpiryDuration())
                .build();

        final Transaction transaction = new Transaction.Builder()
                .withDebits(sourceDebitFromSender, destinationDebitFromConnector)
                .withCredits(sourceCreditToConnector, destinationCreditForConnectorFees, destinationCreditForRecipient)
                .build();

        // Ensure that debits and credit equal each other for each ledgerId...
        transaction.getCredits().stream().map((credit -> credit.getLedgerId())).distinct().forEach((ledgerId -> {
            final BigDecimal creditTotal = transaction.getCredits().stream()
                    .filter(credit -> credit.getLedgerId().equals(ledgerId))
                    .map(Credit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal debitTotal = transaction.getCredits().stream()
                    .filter(debit -> debit.getLedgerId().equals(ledgerId))
                    .map(Credit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Preconditions.checkArgument(creditTotal.equals(debitTotal));
        }));

        return new Quote.Builder(transaction)
                .withSourceLedgerId(sourceQuoteRequest.getLedgerId())
                .withSourceAssetId(sourceAssetId)
                .withDestinationLedgerId(destinationQuoteRequest.getLedgerId())
                .withDestinationAssetId(destinationAssetId)
                .withExchangeRate(exchangeRateInfo.getExchangeRate())
                .withConnectorFee(connectorFeeInfo.getFee())
                .withConnectorFeeAssetId(connectorFeeInfo.getFeeAssetId())
                .build();
    }

    @Override
    protected Quote getQuoteForFixedDestinationAmount(
            final QuoteRequest sourceQuoteRequest, final QuoteRequest destinationQuoteRequest
    ) {
        Objects.requireNonNull(sourceQuoteRequest);
        Objects.requireNonNull(destinationQuoteRequest);

        //////////////
        // Source
        //////////////

        // Capture the Account on the source ledger that will fund the connector.
        final Optional<LedgerAccountId> optConnectorSourceEscrowAccount = this.connectedLedgerService.getEscrowAccountIdForLedger(
                sourceQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(optConnectorSourceEscrowAccount.isPresent());
        final LedgerAccountId connectorSourceEscrowAccountId = optConnectorSourceEscrowAccount.get();

        //////////////
        // Destination
        //////////////

        // Capture the amount requested by the sender to send to a recipient account.
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(destinationQuoteRequest.getOptAmount().isPresent());
        final BigDecimal destinationTransferAmount = destinationQuoteRequest.getOptAmount().get();

        // Get the Account on the destination ledger that will fund the connector.
        final Optional<LedgerAccountId> optConnectorDestinationAccount = this.connectedLedgerService.getEscrowAccountIdForLedger(
                destinationQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        Preconditions.checkArgument(optConnectorDestinationAccount.isPresent());
        final LedgerAccountId connectorDestinationEscrowAccountId = optConnectorDestinationAccount.get();


        ////////////////////
        // Connector Fees
        ////////////////////

        // Since this is a fixed-destination transfer, the connectorFee must be deducted from the amount sent to the other
        // side of the connector.  Thus, we quote the connectorFee in terms of the source Leger/Account combination.
        final Optional<LedgerAccountId> optCommissionAccount = this.connectedLedgerService.getConnectorFeeAccountIdForLedger(
                sourceQuoteRequest.getLedgerId());
        // If this is not present, then there's a bug -- this method should not have been called!
        final LedgerAccountId connectorSourceCommissionAccountId = optCommissionAccount.get();

        final AssetId sourceAssetId = this.supportedAssetsService.getAssetIdForLedger(
                sourceQuoteRequest.getLedgerId());
        final AssetId destinationAssetId = this.supportedAssetsService.getAssetIdForLedger(
                destinationQuoteRequest.getLedgerId());
        final ExchangeRateInfo exchangeRateInfo = this.exchangeRateService.getExchangeRate(
                sourceAssetId, destinationAssetId);

        // Since the destination is fixed, the source amount will fluctuate to "pay" for the connectorFee, if any.
        final ConnectorFeeInfo connectorFeeInfo = this.connectorFeeService.calculateConnectorFee(
                exchangeRateInfo.getSourceAssetId(), exchangeRateInfo.convert(destinationTransferAmount)
        );

        if (!isPositive(connectorFeeInfo.getAmountAfterFee())) {
            throw new InsufficientFundsException(
                    "Not enough funds in the source transfer amount to fund the destination transfer!");
        }


        // This transaction will include various transfers:
        // SOURCE LEDGER
        // 1.) A debit from the sender's account to fund the transfer.
        // 2.) A credit to the connector's escrow account on the source ledger.
        //
        // DESTINATION LEDGER
        // 3.) A debit from the connector's escrow account to fund the transfer.
        // 4.) A credit to the recipient's account for the actual transfer (minus connector fees).
        // 5.) A credit to the connector's fee account on the destination ledger for the connector fee.

        // #1
        // The accountId is unknown for a quote, to preserve privacy.  Only the ledger is known.
        final Debit sourceDebitFromSender
                = new Debit.Builder(sourceQuoteRequest.getLedgerId(), connectorFeeInfo.getAmountAfterFee())
                .withOptExpiryDuration(sourceQuoteRequest.getOptExpiryDuration())
                .build();

        // #2
        final Credit sourceCreditToConnector
                = new Credit.Builder(sourceQuoteRequest.getLedgerId(), connectorFeeInfo.getOriginalAmount())
                .withAccountId(connectorSourceEscrowAccountId)
                // TODO: Ensure the specified expiry isn't longer than the max allowed by the ledger, or too short.
                .withExpiryDuration(
                        sourceQuoteRequest.getOptExpiryDuration().orElse(
                                this.connectedLedgerService.getDefaultExpiration(sourceQuoteRequest.getLedgerId())))
                .build();

        // #3
        final Debit destinationDebitFromConnector
                = new Debit.Builder(destinationQuoteRequest.getLedgerId(), destinationTransferAmount)
                .withAccountId(connectorDestinationEscrowAccountId)
                // TODO: Ensure the specified expiry isn't longer than the max allowed by the ledger, or too short.
                .withExpiryDuration(destinationQuoteRequest.getOptExpiryDuration().orElse(
                        this.connectedLedgerService.getDefaultExpiration(destinationQuoteRequest.getLedgerId())))
                .build();

        // #4
        final Credit destinationCreditForRecipient
                = new Credit.Builder(destinationQuoteRequest.getLedgerId(), destinationTransferAmount)
                .withOptExpiryDuration(destinationQuoteRequest.getOptExpiryDuration())
                .build();

        // #5
        final Credit destinationCreditForConnectorFees
                = new Credit.Builder(destinationQuoteRequest.getLedgerId(), connectorFeeInfo.getFee())
                .withAccountId(connectorSourceCommissionAccountId)
                .withOptExpiryDuration(destinationQuoteRequest.getOptExpiryDuration())
                .build();

        final Transaction transaction = new Transaction.Builder()
                .withDebits(sourceDebitFromSender, destinationDebitFromConnector)
                .withCredits(sourceCreditToConnector, destinationCreditForConnectorFees, destinationCreditForRecipient)
                .build();

        // Ensure that debits and credit equal each other for each ledgerId...
        transaction.getCredits().stream().map((credit -> credit.getLedgerId())).distinct().forEach((ledgerId -> {
            final BigDecimal creditTotal = transaction.getCredits().stream()
                    .filter(credit -> credit.getLedgerId().equals(ledgerId))
                    .map(Credit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal debitTotal = transaction.getCredits().stream()
                    .filter(debit -> debit.getLedgerId().equals(ledgerId))
                    .map(Credit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Preconditions.checkArgument(creditTotal.equals(debitTotal));
        }));

        return new Quote.Builder(transaction)
                .withSourceLedgerId(sourceQuoteRequest.getLedgerId())
                .withSourceAssetId(sourceAssetId)
                .withDestinationLedgerId(destinationQuoteRequest.getLedgerId())
                .withDestinationAssetId(destinationAssetId)
                .withExchangeRate(exchangeRateInfo.getExchangeRate())
                .withConnectorFee(connectorFeeInfo.getFee())
                .withConnectorFeeAssetId(connectorFeeInfo.getFeeAssetId())
                .build();
    }

    /**
     * Validate the incoming request for an ILP Quote by performing the following:
     * <p>
     * <ol>
     * <p>
     * <li>Perform all super-class validation.</li>
     * <p>
     * <li>Validate that the source account exists on a ledger that this connector can orchestrate.</li>
     * <p>
     * <li>Validate that the destination account exists on a ledger that this connector can orchestrate.</li>
     * <p>
     * </ol>.
     *
     * @param sourceQuoteRequest      An instance of {@linkn QuoteRequest} representing the asset transfer coming from
     *                                an ILP source.
     * @param destinationQuoteRequest An instance of {@linkn QuoteRequest} representing the asset transfer going to an
     *                                ILP destination.
     * @throws InvalidQuoteRequestException
     */
    @Override
    protected void performExtendedValidation(
            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
    ) throws InvalidQuoteRequestException {

        final Optional<LedgerAccountId> optSourceAccountId = this.connectedLedgerService.getEscrowAccountIdForLedger(
                sourceQuoteRequest.getLedgerId());
        if (!optSourceAccountId.isPresent()) {
            throw new InsufficientFundsException("No Account found on the requested source Ledger!");
        }

        final Optional<LedgerAccountId> optDestinationAccountId = this.connectedLedgerService.getEscrowAccountIdForLedger(
                destinationQuoteRequest.getLedgerId());
        if (!optDestinationAccountId.isPresent()) {
            throw new InsufficientFundsException("No Account found on the requested destination Ledger!");
        }
    }

    /**
     * @param bigDecimal
     * @return
     * @deprecated Use a utility class instead.
     */
    @Deprecated
    private boolean isPositive(final BigDecimal bigDecimal) {
        return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Given supplied Forex information and a source amount (after fees), calculate the destination amount.
     *
     * @param exchangeRateInfo
     * @param sourceAmountAfterFees
     * @return
     */
    private BigDecimal calculateDestinationAmount(
            final ExchangeRateInfo exchangeRateInfo, final BigDecimal sourceAmountAfterFees
    ) {
        Objects.requireNonNull(exchangeRateInfo);
        Objects.requireNonNull(sourceAmountAfterFees);
        Preconditions.checkArgument(this.isPositive(sourceAmountAfterFees));

        // TODO: Handle non-monetary assets here?  Or perhaps with a CommissionServiceLocatorService that will return a
        // CommissionService based upon the asset type?  For example, the source transfer amount for a stock will basically
        // just be the source value unchanged, and the connector fee will be paid separately.

        // This will be the amount that the destination should receive, in the currency/asset type of the destination.
        return exchangeRateInfo.getExchangeRate().multiply(sourceAmountAfterFees).setScale(
                10, BigDecimal.ROUND_HALF_UP);
    }
}
