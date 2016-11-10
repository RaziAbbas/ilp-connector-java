package money.fluid.ilp.connector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import money.fluid.ilp.connector.model.constants.JsonConstants;
import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.ledger.model.LedgerId;


/**
 * A pair of Assets representing two assets that can be exchanged via this connector using ILP.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class AssetPair {

    @NonNull
    @JsonProperty(JsonConstants.SOURCE_LEDGER)
    private final LedgerId sourceLedger;

    @NonNull
    @JsonProperty(JsonConstants.SOURCE_ASSET)
    private final AssetId sourceAsset;

    @NonNull
    @JsonProperty(JsonConstants.DESTINATION_LEDGER)
    private final LedgerId destinationLedger;

    @NonNull
    @JsonProperty(JsonConstants.DESTINATION_ASSET)
    private final AssetId destinationAsset;
}
