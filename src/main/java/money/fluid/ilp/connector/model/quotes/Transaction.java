package money.fluid.ilp.connector.model.quotes;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Objects;

/**
 * An object that contains one or more {@link Debit} objects and one or more {@link Credit} objects that, in sum,
 * indicate a transfer transaction between two or more ledgers.
 */
public interface Transaction {

    Collection<Debit> getDebits();

    Collection<Credit> getCredits();

    @Getter
    @ToString
    @EqualsAndHashCode
    final class Builder {

        private Collection<Debit> debits;
        private Collection<Credit> credits;

        /**
         * Build method.
         *
         * @return A new instance of {@link Transaction}.
         */
        public Builder() {

        }


        /**
         * Build method.
         *
         * @return A new instance of {@link Transaction}.
         */
        public Transaction build() {
            Objects.requireNonNull(debits);
            Objects.requireNonNull(credits);

            return new Impl(this);
        }

        /**
         * Required-args Constructor.
         *
         * @param transfer An instance of {@linkn Transfer} to initialize this builder from.
         */
        public Builder(final Transaction transfer) {
            Objects.requireNonNull(transfer);
            this.debits = transfer.getDebits();
            this.credits = transfer.getCredits();
        }

        public Builder withDebits(final Collection<Debit> debits) {
            this.debits = ImmutableList.copyOf(Objects.requireNonNull(debits));
            return this;
        }

        public Builder withCredits(final Collection<Credit> credits) {
            this.credits = ImmutableList.copyOf(Objects.requireNonNull(credits));
            return this;
        }

        public Builder withDebits(final Debit... debits) {
            this.debits = ImmutableList.copyOf(Objects.requireNonNull(debits));
            return this;
        }

        public Builder withCredits(final Credit... credits) {
            this.credits = ImmutableList.copyOf(Objects.requireNonNull(credits));
            return this;
        }

        /**
         * An internal implementation of {@link Transaction} for production by the builder.
         */
        @RequiredArgsConstructor
        @Getter
        @EqualsAndHashCode
        @ToString
        private final class Impl implements Transaction {

            private final Collection<Debit> debits;
            private final Collection<Credit> credits;

            /**
             * Required-args Constructor.
             *
             * @param builder
             */
            private Impl(final Builder builder) {
                Objects.requireNonNull(builder);

                this.debits = builder.getDebits();
                this.credits = builder.getCredits();
            }
        }
    }
}
