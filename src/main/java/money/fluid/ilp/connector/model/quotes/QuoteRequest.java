package money.fluid.ilp.connector.model.quotes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.ledger.model.LedgerId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * An object that connects an asset value with a ledger for purposes of transferring between ILP connectors.
 */
public interface QuoteRequest extends LedgerAmount {

    /**
     * Return the number of milliseconds between when the transfer is proposed and when it expires.  If left
     * unspecified, the connector will apply a default value.
     *
     * @return An instance of {@link Long} that represents the expiry duration.
     */
    Optional<Long> getOptExpiryDuration();


    @Getter
    @ToString
    @EqualsAndHashCode
    final class Builder {

        private LedgerId ledgerId;
        private Optional<Long> optExpiryDuration;
        private Optional<BigDecimal> optAmount;

        /**
         * Build method.
         *
         * @return A new instance of {@link Quote}.
         */
        public QuoteRequest build() {
            Objects.requireNonNull(ledgerId);
            Objects.requireNonNull(optExpiryDuration);
            Objects.requireNonNull(optAmount);

            return new Impl(this);
        }

        /**
         * No-args Constructor.
         */
        public Builder(final LedgerId ledgerId) {
            this.ledgerId = Objects.requireNonNull(ledgerId);
            this.optExpiryDuration = Optional.empty();
            this.optAmount = Optional.empty();
        }

        /**
         * Required-args Constructor.
         *
         * @param transfer An instance of {@linkn Transfer} to initialize this builder from.
         */
        public Builder(final QuoteRequest transfer) {
            Objects.requireNonNull(transfer);
            this.ledgerId = transfer.getLedgerId();
            this.optExpiryDuration = transfer.getOptExpiryDuration();
            this.optAmount = transfer.getOptAmount();
        }

        public Builder withLedgerId(final LedgerId ledgerId) {
            this.ledgerId = ledgerId;
            return this;
        }

        public Builder withOptAmount(final Optional<BigDecimal> optAmount) {
            this.optAmount = Objects.requireNonNull(optAmount);
            return this;
        }

        public Builder withAmount(final BigDecimal amount) {
            this.optAmount = Optional.of(amount);
            return this;
        }

        public Builder withOptExpiryDuration(final Optional<Long> optExpiryDuration) {
            this.optExpiryDuration = Objects.requireNonNull(optExpiryDuration);
            return this;
        }

        public Builder withExpiryDuration(final Long expiryDuration) {
            this.optExpiryDuration = Optional.of(expiryDuration);
            return this;
        }

        /**
         * An internal implementation of {@link QuoteRequest} for production by the builder.
         */
        @RequiredArgsConstructor
        @Getter
        @EqualsAndHashCode
        @ToString
        private final class Impl implements QuoteRequest {

            @JsonProperty("ledger_id")
            private final LedgerId ledgerId;

            @JsonProperty("expiry_duration")
            private final Optional<Long> optExpiryDuration;

            @JsonProperty("amount")
            private final Optional<BigDecimal> optAmount;

            /**
             * Required-args Constructor.
             *
             * @param builder
             */
            private Impl(final Builder builder) {
                Objects.requireNonNull(builder);

                this.ledgerId = builder.getLedgerId();
                this.optAmount = builder.getOptAmount();
                this.optExpiryDuration = builder.getOptExpiryDuration();
            }

        }
    }
}
