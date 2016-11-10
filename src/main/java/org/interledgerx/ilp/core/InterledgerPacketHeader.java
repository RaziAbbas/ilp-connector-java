package org.interledgerx.ilp.core;

import com.google.common.base.MoreObjects;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import money.fluid.ilp.connector.model.ids.IlpTransactionId;
import org.interledger.cryptoconditions.Condition;

import javax.money.MonetaryAmount;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable Interledger Packet Header
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public class InterledgerPacketHeader {

    private final IlpTransactionId ilpTransactionId;
    private final IlpAddress sourceAddress;
    private final IlpAddress destinationAddress;
    private final MonetaryAmount amount;
    private final Optional<Condition> optCondition;
    private final Optional<Date> optExpiry;

    public InterledgerPacketHeader(
            final IlpTransactionId ilpTransactionId, final IlpAddress sourceAddress,
            final IlpAddress destinationAddress, final MonetaryAmount amount
    ) {
        this.ilpTransactionId = Objects.requireNonNull(ilpTransactionId);
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        this.destinationAddress = Objects.requireNonNull(destinationAddress);
        this.amount = Objects.requireNonNull(amount);
        this.optCondition = Optional.empty();
        this.optExpiry = Optional.empty();

        //TODO Validate address
        //TODO Validate amount
    }

    public InterledgerPacketHeader(
            final IlpTransactionId ilpTransactionId,
            final IlpAddress sourceAddress,
            final IlpAddress destinationAddress,
            final MonetaryAmount amount,
            final Condition condition,
            final Date expiry
    ) {
        this.ilpTransactionId = Objects.requireNonNull(ilpTransactionId);
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        this.destinationAddress = Objects.requireNonNull(destinationAddress);
        this.amount = Objects.requireNonNull(amount);
        this.optCondition = Optional.ofNullable(condition);
        this.optExpiry = Optional.ofNullable(expiry);

        if (optExpiry.isPresent() && !optCondition.isPresent()) {
            throw new IllegalArgumentException("Must provide a condition if providing an expiry.");
        }

        if (optCondition.isPresent() && !optExpiry.isPresent()) {
            throw new IllegalArgumentException("Must provide an expiry if providing a condition.");
        }

        //TODO Validate address
        //TODO Validate amount
    }

    /**
     * A unique identifier that identifies this ILP transaction.
     *
     * @return the ilpTransactionId
     */
    public IlpTransactionId getIlpTransactionId() {
        return ilpTransactionId;
    }

    /**
     * The ILP Address of the source account
     *
     * @return the sourceAddress
     */
    public IlpAddress getSourceAddress() {
        return this.sourceAddress;
    }


    /**
     * The ILP Address of the destination account
     *
     * @return the destinationAddress
     */
    public IlpAddress getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * The amount that must be transferred into the destination account
     *
     * @return the amount
     */
    public MonetaryAmount getAmount() {
        return amount;
    }

    /**
     * The optionally-present condition that must be fulfilled to release prepared transfers
     *
     * @return the condition
     */
    public Optional<Condition> getCondition() {
        return optCondition;
    }

    /**
     * The optionally-present expiry of the payment after which any prepared transfers must be
     * rolled back.
     *
     * @return the expiry
     */
    public Optional<Date> getExpiry() {
        return optExpiry;
    }

    /**
     * Checks if this header is for an optimistic mode payment.
     *
     * @return {@code true} if the header contains no timeout and condition
     */
    public boolean isOptimisticModeHeader() {
        return (!optCondition.isPresent() && !optExpiry.isPresent());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ilpTransactionId", ilpTransactionId)
                .toString();
    }
}
