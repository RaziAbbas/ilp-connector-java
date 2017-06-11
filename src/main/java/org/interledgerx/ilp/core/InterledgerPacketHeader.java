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

    private final Integer version = 1;
    private final IlpTransactionId ilpTransactionId;
    // TODO: Consider removing source address?  The spec doesn't have it.
    private final IlpAddress sourceAddress;
    private final IlpAddress destinationAddress;
    private final MonetaryAmount destinationAmount;
    private final Optional<Condition> optCondition;
    //User-specified memo, will be the memo of the final transfer
    private final Optional<String> optData;
    // Maximum expiry time of the last transfer that the recipient will accept.  In other words, this is the expiry of
    // the entire payment..
    private final Optional<Date> optExpiry;

    /**
     * Required-args Constructor.
     * <p>
     * The following are some rules for creating conditional payments:
     * <pre>
     *   <ul>
     *     <li>Ledgers MAY require that all transfers with a condition also carry an expiry timestamp.</li>
     *     <li>Ledgers MUST reject transfers that carry an expiry timestamp, but no condition.</li>
     *     <li>Ledgers MUST reject transfers whose expiry transfer time has been reached or exceeded and whose
     * condition
     *     has not yet been fulfilled.</li>
     *     <li>When rejecting a transfer, the ledger MUST lift the hold and make the funds available to the sender
     * again.</li>
     *   </ul>
     * </pre>
     *
     * @param ilpTransactionId
     * @param sourceAddress
     * @param destinationAddress
     * @param destinationAmount
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md"
     */
    public InterledgerPacketHeader(
            final IlpTransactionId ilpTransactionId, final IlpAddress sourceAddress,
            final IlpAddress destinationAddress, final MonetaryAmount destinationAmount
    ) {
        this.ilpTransactionId = Objects.requireNonNull(ilpTransactionId);
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        this.destinationAddress = Objects.requireNonNull(destinationAddress);
        this.destinationAmount = Objects.requireNonNull(destinationAmount);
        this.optCondition = Optional.empty();
        this.optData = Optional.empty();
        this.optExpiry = Optional.empty();

        //TODO Validate address
        //TODO Validate destinationAmount
    }

    /**
     * Required-args Constructor that specifies a condition and a payment expiration.
     * <p>
     * The following rules are enforced for creating conditional payments:
     * <pre>
     *   <ul>
     *     <li>Ledgers MAY require that all transfers with a condition also carry an expiry timestamp.</li>
     *     <li>Ledgers MUST reject transfers that carry an expiry timestamp, but no condition.</li>
     *     <li>Ledgers MUST reject transfers whose expiry transfer time has been reached or exceeded and whose
     * condition has not yet been fulfilled.</li>
     *   </ul>
     * </pre>
     *
     * @param ilpTransactionId
     * @param sourceAddress
     * @param destinationAddress
     * @param destinationAmount
     * @param condition
     * @param expiry
     * @see "https://github.com/interledger/rfcs/blob/master/0003-interledger-protocol/0003-interledger-protocol.md"
     */
    public InterledgerPacketHeader(
            final IlpTransactionId ilpTransactionId,
            final IlpAddress sourceAddress,
            final IlpAddress destinationAddress,
            final MonetaryAmount destinationAmount,
            final Condition condition,
            final String optData,
            final Date expiry
    ) {
        this.ilpTransactionId = Objects.requireNonNull(ilpTransactionId);
        this.sourceAddress = Objects.requireNonNull(sourceAddress);
        this.destinationAddress = Objects.requireNonNull(destinationAddress);
        this.destinationAmount = Objects.requireNonNull(destinationAmount);
        this.optCondition = Optional.ofNullable(condition);
        this.optData = Optional.ofNullable(optData);
        this.optExpiry = Optional.ofNullable(expiry);

        if (optExpiry.isPresent() && !optCondition.isPresent()) {
            throw new IllegalArgumentException("Must provide a condition if providing an expiry.");
        }

        if (optCondition.isPresent() && !optExpiry.isPresent()) {
            throw new IllegalArgumentException("Must provide an expiry if providing a condition.");
        }

        //TODO Validate address
        //TODO Validate destinationAmount
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
     * The destinationAmount that must be transferred into the destination account
     *
     * @return the destinationAmount
     */
    public MonetaryAmount getDestinationAmount() {
        return destinationAmount;
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
     * The optionally-present User-specified memo, will be the memo of the final transfer.
     *
     * @return the optData
     */
    public Optional<String> getData() {
        return optData;
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
