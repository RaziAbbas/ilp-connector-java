package money.fluid.ilp.connector.services;

import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.ledger.model.LedgerId;
import money.fluid.ilp.connector.model.AssetPair;
import money.fluid.ilp.connector.model.quotes.LedgerAmount;

import java.util.Collection;

/**
 * A service that indicates if an ILP transaction with a given asset-pair can be serviced by this connector or not.
 * Assets exist independent of Ledger, but are tracked inside of various ledgers.  Thus, in order to support a given
 * asset, this connector must be connected to a Ledger that tracks that asset.
 */
public interface SupportedAssetsService {

    /**
     * Get the main {@link AssetId} for a specified {@link LedgerId}.  Note that in ILP, a given ledger may only support
     * a single asset.
     *
     * @param ledgerId An instance of {@link LedgerId}.
     * @return
     */
    AssetId getAssetIdForLedger(LedgerId ledgerId);

    /**
     * Get the main {@link LedgerId} for a specified {@link AssetId}.  Note that in ILP, a given ledger may only support
     * a single asset.
     *
     * @param assetId An instance of {@link AssetId}.
     * @return
     */
    LedgerId getLedgerIdForAsset(AssetId assetId);

    /**
     * Get a {@link Collection} of all supported assets that this Connector can facilitate ILP transactions for.
     *
     * @return
     */
    Collection<AssetPair> getSupportedAssets();

    /**
     * Determines if the requested transfer can be fulfilled solely by this connector across two ledgers.  If so, this
     * means the connector has an account on both the source and destination accounts, and can coordinate the transfer
     * by itself.
     *
     * @param sourceLedgerAmount      An instance of {@link LedgerAmount} for the source of the transfer being
     *                                requested.
     * @param destinationLedgerAmount An instance of {@link LedgerAmount} for the destination of the transfer being
     *                                requested.
     * @return {@code true} if this connector can fulfil the requested transfer; {@code false} otherwise.
     */
    boolean isLocallyServiced(LedgerAmount sourceLedgerAmount, LedgerAmount destinationLedgerAmount);

    /**
     * Determines if the requested transfer can be fulfilled by a combination of this connector (source) and a different
     * connector (destination). If so, this connector may charge a transaction fee, which will be coupled with any
     * transaction fees charged by downstream connectors.
     *
     * @param sourceLedgerAmount      An instance of {@link LedgerAmount} for the source of the transfer being
     *                                requested.
     * @param destinationLedgerAmount An instance of {@link LedgerAmount} for the destination of the transfer being
     *                                requested.
     * @return {@code true} if this connector can fulfil the source portion of the requested transfer, and also has a
     * connection to another Connector that can fulfill the transfer; {@code false} otherwise.
     */
    boolean isRemotelyServiced(LedgerAmount sourceLedgerAmount, LedgerAmount destinationLedgerAmount);
}