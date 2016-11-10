package money.fluid.ilp.connector.model.quotes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.AssetId;
import money.fluid.ilp.ledger.model.LedgerId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * A pricing quote for a requested set of source and target destination.
 * <p>
 * <p>
 * // A quote may have N number of transactions in the source, and X number of transactions in the destination.
 */
public interface Quote {

    LedgerId getSourceLedgerId();

    AssetId getSourceAssetId();

    LedgerId getDestinationLedgerId();

    AssetId getDestinationAssetId();

    BigDecimal getExchangeRate();

    /**
     * Return a  {@link Transaction} objects that contains information about the intended source and destination credits
     * and debits.
     *
     * @return An instance {@link Transaction}.
     */
    Transaction getTransaction();

    @Getter
    @ToString
    @EqualsAndHashCode
    final class Builder {

        private UUID uuid;

        private LedgerId sourceLedgerId;

        private AssetId sourceAssetId;

        private LedgerId destinationLedgerId;

        private AssetId destinationAssetId;

        private BigDecimal exchangeRate;

        private BigDecimal connectorFee;

        private AssetId connectorFeeAssetId;

        private Transaction transaction;

        /**
         * Build method.
         *
         * @return A new instance of {@link Quote}.
         */
        public Quote build() {
            return new Impl(this);
        }

        public Builder(final Transaction transaction) {
            this.uuid = UUID.randomUUID();
            this.transaction = Objects.requireNonNull(transaction);
        }

        public Builder withId(final UUID uuid) {
            this.uuid = Objects.requireNonNull(uuid);
            return this;
        }


        public Builder withSourceLedgerId(final LedgerId sourceLedgerId) {
            this.sourceLedgerId = Objects.requireNonNull(sourceLedgerId);
            return this;
        }

        public Builder withSourceAssetId(final AssetId sourceAssetId) {
            this.sourceAssetId = Objects.requireNonNull(sourceAssetId);
            return this;
        }

        public Builder withDestinationLedgerId(final LedgerId destinationLedgerId) {
            this.destinationLedgerId = Objects.requireNonNull(destinationLedgerId);
            return this;
        }

        public Builder withDestinationAssetId(final AssetId destinationAssetId) {
            this.destinationAssetId = Objects.requireNonNull(destinationAssetId);
            return this;
        }

        public Builder withExchangeRate(final BigDecimal exchangeRate) {
            this.exchangeRate = Objects.requireNonNull(exchangeRate);
            return this;
        }

        public Builder withConnectorFee(final BigDecimal connectorFee) {
            this.connectorFee = Objects.requireNonNull(connectorFee);
            return this;
        }

        public Builder withConnectorFeeAssetId(final AssetId connectorFeeAssetId) {
            this.connectorFeeAssetId = Objects.requireNonNull(connectorFeeAssetId);
            return this;
        }

        public Builder withTransaction(final Transaction transaction) {
            this.transaction = Objects.requireNonNull(transaction);
            return this;
        }

        @RequiredArgsConstructor
        @Getter
        @EqualsAndHashCode
        @ToString
        public final class Impl implements Quote {

            @JsonProperty("id")
            private final UUID uuid;

            @JsonProperty("source_ledger_id")
            private final LedgerId sourceLedgerId;

            @JsonProperty("source_asset_id")
            private final AssetId sourceAssetId;

            @JsonProperty("destination_ledger_id")
            private final LedgerId destinationLedgerId;

            @JsonProperty("destination_asset_id")
            private final AssetId destinationAssetId;

            @JsonProperty("exchange_rate")
            private final BigDecimal exchangeRate;

            @JsonProperty("connector_fee")
            private final BigDecimal connectorFee;

            @JsonProperty("connector_fee_asset_id")
            private final AssetId connectorFeeAssetId;

            @JsonProperty("transaction")
            private final Transaction transaction;

            private Impl(final Builder builder) {
                Objects.requireNonNull(builder);

                this.uuid = Objects.requireNonNull(builder.uuid);

                this.sourceLedgerId = Objects.requireNonNull(builder.sourceLedgerId);
                this.sourceAssetId = Objects.requireNonNull(builder.sourceAssetId);

                this.destinationLedgerId = Objects.requireNonNull(builder.destinationLedgerId);
                this.destinationAssetId = Objects.requireNonNull(builder.destinationAssetId);

                this.exchangeRate = Objects.requireNonNull(builder.exchangeRate);

                this.connectorFee = Objects.requireNonNull(builder.connectorFee);
                this.connectorFeeAssetId = Objects.requireNonNull(builder.connectorFeeAssetId);

                this.transaction = Objects.requireNonNull(builder.transaction);
            }
        }
    }
}
