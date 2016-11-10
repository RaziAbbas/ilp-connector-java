package money.fluid.ilp.connector.services.quoting;

/**
 * An implementation of {@link QuoteService} that is used to supply cross-ledger quotes that this connector can fulfil.
 * Since this type of quote involves a cross-ledger transfer, likely involving some sort of cross-asset exchange, this
 * type of quote service will generally include a transaction fee.
 */
public interface CrossLedgerQuoteService extends QuoteService {

}
