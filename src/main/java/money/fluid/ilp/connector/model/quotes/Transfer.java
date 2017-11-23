package money.fluid.ilp.connector.model.quotes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * A part of a transaction, such as a debit or credit.
 */
public interface Transfer {

    /**
     * m, Retrieves the {@link LedgerId} associated with this {@link Transfer}.
     *
     * @return An instance of {@link LedgerId}.
     */
    LedgerId getLedgerId();

    /**
     * The AccountId for this credit.  Optional because sometimes the accountId is not know, and it not required to be
     * known, such as for quoting.
     *
     * @return
     */
    Optional<LedgerAccountId> getOptAccountId();

    /**
     * Return the number of milliseconds between when the transfer is proposed and when it expires.
     *
     * @return An instance of {@link Long} that represents the expiry duration.
     */
    Optional<Long> getOptExpiryDuration();

    /**
     * The amount of this credit.
     *
     * @return
     */
    BigDecimal getAmount();

    // FIXME!
    <E> E toTransfer();

    @Getter
    @ToString
    @EqualsAndHashCode
    class Builder {

        protected LedgerId ledgerId;
        protected Optional<LedgerAccountId> optAccountId;
        protected BigDecimal amount;
        protected Optional<Long> optExpiryDuration;

        public LedgerId getLedgerId() {
			return ledgerId;
		}

		public Optional<LedgerAccountId> getOptAccountId() {
			return optAccountId;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public Optional<Long> getOptExpiryDuration() {
			return optExpiryDuration;
		}

		/**
         * Required-args Constructor.
         *
         * @param amount
         */
        public Builder(final LedgerId ledgerId, final BigDecimal amount) {
            this.ledgerId = Objects.requireNonNull(ledgerId);
            this.optAccountId = Optional.empty();
            this.amount = Objects.requireNonNull(amount);
            this.optExpiryDuration = Optional.empty();
        }

        /**
         * Build method.
         *
         * @return A new instance of {@link Transfer}.
         */
        public Transfer build() {
            return new Impl(this);
        }

        public Builder(final Transfer transfer) {
            Objects.requireNonNull(transfer);
            this.ledgerId = Objects.requireNonNull(transfer.getLedgerId());
            this.optAccountId = Objects.requireNonNull(transfer.getOptAccountId());
            this.optExpiryDuration = Objects.requireNonNull(transfer.getOptExpiryDuration());
            this.amount = Objects.requireNonNull(transfer.getAmount());
        }

        public Builder withAmount(final BigDecimal amount) {
            this.amount = Objects.requireNonNull(amount);
            return this;
        }

        public Builder withOptAccountId(final Optional<LedgerAccountId> optAccountId) {
            this.optAccountId = Objects.requireNonNull(optAccountId);
            return this;
        }

        public Builder withAccountId(final LedgerAccountId accountId) {
            this.optAccountId = Optional.of(accountId);
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

        @Getter
        @ToString
        @EqualsAndHashCode
        protected class Impl implements Transfer {

            @JsonProperty("ledger_id")
            private final LedgerId ledgerId;

            // Optional because sometimes the accountId is not know, and it not required to be known, such as for quoting.
            @JsonProperty("account_id")
            private final Optional<LedgerAccountId> optAccountId;

            @JsonProperty("amount")
            private final BigDecimal amount;

            @JsonProperty("expiry_duration")
            private final Optional<Long> optExpiryDuration;

            public LedgerId getLedgerId() {
				return ledgerId;
			}

			public Optional<LedgerAccountId> getOptAccountId() {
				return optAccountId;
			}

			public BigDecimal getAmount() {
				return amount;
			}

			public Optional<Long> getOptExpiryDuration() {
				return optExpiryDuration;
			}
            
            /**
             * Required-args Constructor.
             *
             * @param builder
             */
            public Impl(final Builder builder) {
                Objects.requireNonNull(builder);

                this.ledgerId = Objects.requireNonNull(builder.getLedgerId());
                this.optAccountId = Objects.requireNonNull(builder.getOptAccountId());
                this.amount = Objects.requireNonNull(builder.getAmount());
                this.optExpiryDuration = Objects.requireNonNull(builder.getOptExpiryDuration());
            }

            // FIXME!
            @Override
            public <E> E toTransfer() {
                return null;
            }
        }
    }
}
