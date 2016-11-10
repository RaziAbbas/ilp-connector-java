package money.fluid.ilp.connector.model.quotes;


import com.sappenin.utils.StringId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.Ledger;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * An object that connects an asset value with a ledger for purposes of transferance.
 */
public interface TransferRequest {

    /**
     * Retrieves the {@link Ledger} associated with this {@link TransferRequest}.
     *
     * @return An instance of {@link Ledger}.
     */
    StringId<Ledger> getLedgerId();

    /**
     * Return the number of milliseconds between when the transfer is proposed and when it expires.
     *
     * @return An instance of {@link Long} that represents the expiry duration.
     */
    Optional<Long> getOptExpiryDuration();

    /**
     * Retrieves the optionally present decimal amount of the asset associated with this {@link TransferRequest}.
     *
     * @return An instance of {@link BigDecimal}.
     */
    Optional<MonetaryAmount> getOptAmount();

    // TODO FIXME!
    <E> E toTransfer();


    @Getter
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    final class Builder {

        private StringId<Ledger> ledgerId;
        private Optional<Long> optExpiryDuration;
        private Optional<MonetaryAmount> optAmount;

        /**
         * Build method.
         *
         * @return A new instance of {@link Quote}.
         */
        public TransferRequest build() {
            Objects.requireNonNull(ledgerId);
            Objects.requireNonNull(optExpiryDuration);
            Objects.requireNonNull(optAmount);

            return new TransferRequest.Builder.Impl(this);
        }

        /**
         * Required-args Constructor.
         *
         * @param transfer An instance of {@linkn Transfer} to initialize this builder from.
         */
        public Builder(final TransferRequest transfer) {
            Objects.requireNonNull(transfer);
            this.ledgerId = transfer.getLedgerId();
            this.optExpiryDuration = transfer.getOptExpiryDuration();
            this.optAmount = transfer.getOptAmount();
        }

        public Builder withLedgerId(final StringId<Ledger> ledgerId) {
            this.ledgerId = ledgerId;
            return this;
        }

        public Builder withAmount(final Optional<MonetaryAmount> optAmount) {
            this.optAmount = optAmount;
            return this;
        }

        public Builder withExpiryDuration(final Optional<Long> optExpiryDuration) {
            this.optExpiryDuration = optExpiryDuration;
            return this;
        }

        /**
         * An internal implementation of {@link TransferRequest} for production by the builder.
         */
        @RequiredArgsConstructor
        @Getter
        @EqualsAndHashCode
        @ToString
        private final class Impl implements TransferRequest {

            private final StringId<Ledger> ledgerId;
            private final Optional<Long> optExpiryDuration;
            private final Optional<MonetaryAmount> optAmount;

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

            // FIXME!
            @Override
            public <E> E toTransfer() {
                return null;
            }
        }
    }
}
