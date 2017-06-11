package money.fluid.ilp.connector.services.quoting.impl;

import money.fluid.ilp.connector.services.quoting.QuoteService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * An implementation of {@link QuoteService} that operates as if it has infinite liquidity for quoting purposes.  In
 * other words, this service will always return a valid quote that includes a 2% service fee.
 */
@Service
@Qualifier("Inifinite")
public class InfiniteLiquidityQuoteService { // implements QuoteService {

//    private final MeService meService;
//
//    /**
//     * Required args Constructor.
//     *
//     * @param meService           An instance of {@link MeService}.
//     * @param ledgerLookupService
//     */
//    public InfiniteLiquidityQuoteService(final MeService meService, final LedgerLookupService ledgerLookupService) {
//        this.meService = Objects.requireNonNull(meService);
//        this.ledgerLookupService = Objects.requireNonNull(ledgerLookupService);
//    }
//
//    /**
//     * The main entry-point for creating a quote for the supplied transfer requests.  This method must do several
//     * things:
//     * <p/>
//     * <ol> <li> Determine which accounts on the sender and destination are owned by the connector. </li> <li>Determine
//     * which account (sender or destination) will collect the commission.</li> </ol>
//     * <p/>
//     * Note: Asset accounts are increased with a "debit" and decreased with a "credit".
//     *
//     * @param sourceTransferRequest      An instance of {@link Transfer} with information about the source of the asset
//     *                                   being transferred.
//     * @param destinationTransferRequest An instance of {@link Transfer} with information about the destination of the
//     *                                   asset being transferred.
//     * @return
//     */
//    //@Override
//    public Quote getQuote(
//            final TransferRequest sourceTransferRequest, final TransferRequest destinationTransferRequest
//    ) {
//
//        Objects.requireNonNull(sourceTransferRequest);
//        Objects.requireNonNull(destinationTransferRequest);
//
//        final Collection<Transfer> sourceTransfers;
//        final Collection<Transfer> destinationTransfers;
//
//        // Take a commission from the source first, and advertise where commission payments will go?
//        if (sourceTransferRequest.getOptAmount().isPresent()) {
//
//            // This amount requested by the transferor to send to a transferee.
//            final MonetaryAmount sourceTransferAmount = sourceTransferRequest.getOptAmount().get();
//
//
//            // Get the Account on the source ledger.
//
//            final Optional<Account> optCommissionAccount = this.ledgerLookupService.getAccountForLedger(
//                    sourceTransferRequest.getLedgerId());
//            if (!optCommissionAccount.isPresent()) {
//                throw new InsufficientFundsException("This connector has no Account on the requested source Ledger!");
//            } else {
//                final Account commissionAccount = optCommissionAccount.get();
//
//                // Calculate the commission based upon the sender amount.
//                final MonetaryAmount commission = sourceTransferRequest.getOptAmount().get().multiply(
//                        DEFAULT_COMMISSION_RATE);
//
//
//                // Make 2 source transfers (1 for the real transfer and 1 for the commission) and 1 destination transfer.
//
//                // Add to this asset in order to capture the commission.
//                // TODO: FIXME!
//                final Credit sourceCommisionDebit = null;
//                //new Credit.Builder().withAccountId(commissionAccount.getLedgerAccountIlpAddress()).withAmount(commission).build();
//
//                final MonetaryAmount adjustedSourceTransferAmount = this.computeSourceCreditAfterCommission(
//                        sourceTransferAmount, commission);
//
//
//                // Reduce this asset in order to transfer funds.
//                // TODO: FIXME!
//                final Credit sourceCredit = null;
//                //new Credit.Builder().withAccountId(
//                //      this.ledgerLookupService.getAccountForLedger(sourceTransferRequest.getLedgerId())).withAmount(
//                //    adjustedSourceTransferAmount).build();
//
//
////                // TODO: The accountId of the source transfer should probably be available.  However, if this connector
////                // doesn't need to know that information, then consider using a debitQuote object that doesn't have this value as opposed to a Debit object.
////
////                // The reduction of the asset in the sender's ledger.
////                final Credit commissionCredit = new Credit.Builder().withAmount(commission).withAccountId(null).build();
////                final Debit commissionDebit = new Debit.Builder().withAmount(commission).withAccountId(this.meService.get).build();
////
////                // Adjust the source amount down to account for the commission being taken out.
////
////
////                final Debit sourceTransferDebit = new Debit.Builder().withAmount()
//
//                // The commission source will be from the original source of funds.
//
//                // The amount of asset to add to the source connector asset account, as a Credit
//                // TODO: FIXME!
//                final Credit commissionDebit = null;
//                //new Credit.Builder().withAmount(commission).withAccountId(null).build();
//
//                //TODO FIXME!
//                Object commissionCredit = null;
//                final Transfer commissionSourceTransfer = new Transfer.Builder(null)
//                        //.withLedgerId(sourceTransferRequest.getLedgerId())
//                        //.withExpiryDuration(sourceTransferRequest.getOptExpiryDuration())
//                        //.withCredits(commissionCredit)
//                        //.withAmount(Optional.of(commission))
//                        .build();
//
//                // This originalTransfer will go to this connector's ledger account used for collecting commissions.
//                // TODO: FIXME!
//                final Transfer commissionDestinationTransfer = new Transfer.Builder(null)
//                        //.withLedgerId(this.meService.getLedgerId())
//                        //.withAmount(Optional.of(commission))
//                        //.withExpiryDuration(Optional.of(this.meService.getDefaultExpiration()))
//                        .build();
//
//
//                final Transfer adjustedSourceTransfer = this.reduceTransferForCommission(
//                        sourceTransferRequest, commission);
//                sourceTransfers = ImmutableList.of(commissionSourceTransfer, adjustedSourceTransfer);
//                // No adjustment for the destination transfer
//                destinationTransfers = ImmutableList.of(
//                        commissionDestinationTransfer.toTransfer(), destinationTransferRequest.toTransfer());
//
//
//            }
//
//
//        } else if (destinationTransferRequest.getOptAmount().isPresent()) {
//
//            // Calculate the commission based upon the receiver amount.
//            final MonetaryAmount commission = destinationTransferRequest.getOptAmount().get().multiply(
//                    DEFAULT_COMMISSION_RATE);
//
//            // TODO: How to compute a source and destination????
//
//            // This commission will come from the destination of funds.
//            // TODO: FIXME!
//            final Transfer commissionSourceTransfer = null;
////                    new Transfer.Builder()
////                    .withLedgerId(destinationTransferRequest.getLedgerId())
////                    .withAmount(Optional.of(commission))
////                    .withExpiryDuration(destinationTransferRequest.getOptExpiryDuration())
////                    .build();
//
//            // This commission will go to the destination connector's ledger account used for collecting commissions, which will have an account owned by the owner of this connector.
//            // TODO: FIXME!
//            final Transfer commissionDestinationTransfer = new Transfer.Builder(null)
//                    //.withLedgerId(destinationTransferRequest.getLedgerId())
//                    //.withAmount(Optional.of(commission))
//                    //.withExpiryDuration(destinationTransferRequest.getOptExpiryDuration())
//                    .build();
//
//
//            // Adjust the source amount down to account for the commission being taken out.
//            final Transfer adjustedSourceTransfer = this.reduceTransferForCommission(sourceTransferRequest, commission);
//            sourceTransfers = ImmutableList.of(commissionSourceTransfer, adjustedSourceTransfer);
//
//            // No adjustment for the destination transfer
//            // TODO: FIXME!
//            destinationTransfers = ImmutableList.of(
//                    commissionDestinationTransfer.toTransfer(), destinationTransferRequest.toTransfer());
//
//
//        } else {
//            throw new RuntimeException("Either the source or destination amount must be specified!");
//        }
//
//
//        // FIXME!
//        Transaction transaction = null;
//        return new Quote.Builder(transaction)
//                //.withSourceTransfers(sourceTransfers)
//                //.withDestinationTransfers(destinationTransfers)
//                .build();
//    }
//
//    // TODO: FIXME!
//    private Transfer reduceTransferForCommission(TransferRequest sourceTransferRequest, MonetaryAmount commission) {
//        return null;
//    }
//
//    private MonetaryAmount computeSourceCreditAfterCommission(
//            final MonetaryAmount transferAmount, final MonetaryAmount commission
//    ) {
//        Objects.requireNonNull(transferAmount);
//        Objects.requireNonNull(commission);
//
//        // The original transfer minus the commission.
//        final MonetaryAmount adjustedTransferAmount = transferAmount.subtract(commission);
//
//        if (adjustedTransferAmount.isNegativeOrZero()) {
//            throw new InsufficientFundsException(
//                    "Not enough money in the source transfer to charge a commission and still make a transfer!");
//        } else {
//            return adjustedTransferAmount;
//        }
//    }
//
//
//    // 1% Commission
//    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.01");
//
//    /**
//     * Calculates a commission for an Interledger originalTransfer.  For this implementation, the commission amount is a
//     * fixed 1% of the source or destination amount.
//     *
//     * @param optSourceAmount
//     * @param optDestinationAmount
//     * @return
//     */
//    @VisibleForTesting
//    MonetaryAmount calculateCommission(
//            final Optional<MonetaryAmount> optSourceAmount,
//            final Optional<MonetaryAmount> optDestinationAmount
//    ) {
//
//        Objects.requireNonNull(optSourceAmount);
//        Objects.requireNonNull(optDestinationAmount);
//
//        if (optSourceAmount.isPresent()) {
//            return optSourceAmount.get().multiply(DEFAULT_COMMISSION_RATE);
//        } else if (optDestinationAmount.isPresent()) {
//
//        } else {
//            throw new RuntimeException("Either the source or destination amount must be specified!");
//        }
//
//        //TODO: FIXME!
//        return null;
//    }
//
//    // TODO: IMPLEMENT ME?
//    @Override
//    public Quote getQuote(
//            QuoteRequest sourceQuoteRequest, QuoteRequest destinationQuoteRequest
//    ) {
//        return null;
//    }


//    public abstract class QuoteHandler {
//        abstract MonetaryAmount handleSoureTransfer(final MonetaryAmount transferAmount);
//
//        abstract MonetaryAmount handleDestinationTransfer(final MonetaryAmount transferAmount);
//
//        public final MonetaryAmount handle(
//                final Optional<MonetaryAmount> optSourceAmount,
//                final Optional<MonetaryAmount> optDestinationAmount
//        ) {
//
//            Objects.requireNonNull(optSourceAmount);
//            Objects.requireNonNull(optDestinationAmount);
//
//            if (optSourceAmount.isPresent()) {
//                return this.handleSoureTransfer(optSourceAmount.get());
//            } else if (optDestinationAmount.isPresent()) {
//                return this.handleDestinationTransfer(optDestinationAmount.get());
//            } else {
//                throw new RuntimeException("Either the source or destination amount must be specified!");
//            }
//        }
//    }


}
