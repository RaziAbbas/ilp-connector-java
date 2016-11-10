package money.fluid.ilp.connector.model;

import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.connector.model.quotes.Quote;
import money.fluid.ilp.connector.model.quotes.Transaction;

import java.math.BigDecimal;

/**
 * Created by dfuelling on 9/26/16.
 */
// TODO: Remove this class and use a single interface with a Builder pattern only.  The Impl in the builder should have JSON annotations.
public class QuoteJson implements Quote {
    public QuoteJson(Quote quote) {

    }

    @Override
    public LedgerId getSourceLedgerId() {
        return null;
    }

    @Override
    public AssetId getSourceAssetId() {
        return null;
    }

    @Override
    public LedgerId getDestinationLedgerId() {
        return null;
    }

    @Override
    public AssetId getDestinationAssetId() {
        return null;
    }

    @Override
    public BigDecimal getExchangeRate() {
        return null;
    }

    @Override
    public Transaction getTransaction() {
        return null;
    }
}
