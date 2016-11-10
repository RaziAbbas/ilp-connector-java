package money.fluid.ilp.connector.model.quotes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.LedgerAccountId;
import money.fluid.ilp.ledger.model.LedgerId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * A credit, or addition, of funds to an account.
 */
public interface Credit extends Transfer {

    @Getter
    @ToString
    @EqualsAndHashCode
    final class Builder extends Transfer.Builder {

        /**
         * Required-args Constructor.
         *
         * @param ledgerId
         * @param amount
         */
        public Builder(final LedgerId ledgerId, final BigDecimal amount) {
            super(ledgerId, amount);
        }

        @Override
        public Builder withAmount(final BigDecimal amount) {
            super.amount = Objects.requireNonNull(amount);
            return this;
        }

        @Override
        public Builder withOptAccountId(final Optional<LedgerAccountId> optAccountId) {
            super.optAccountId = Objects.requireNonNull(optAccountId);
            return this;
        }

        @Override
        public Builder withAccountId(final LedgerAccountId accountId) {
            super.optAccountId = Optional.of(accountId);
            return this;
        }

        @Override
        public Builder withOptExpiryDuration(final Optional<Long> optExpiryDuration) {
            super.optExpiryDuration = Objects.requireNonNull(optExpiryDuration);
            return this;
        }

        @Override
        public Builder withExpiryDuration(final Long expiryDuration) {
            super.optExpiryDuration = Optional.of(expiryDuration);
            return this;
        }

        /**
         * Build method.
         *
         * @return A new instance of {@link Transfer}.
         */
        @Override
        public Credit build() {
            return new Impl(this);
        }

        @Getter
        @ToString
        @EqualsAndHashCode
        private final class Impl extends Transfer.Builder.Impl implements Credit {

            /**
             * Required-args Constructor.
             *
             * @param builder
             */
            public Impl(final Credit.Builder builder) {
                super(builder);
            }
        }
    }
}
