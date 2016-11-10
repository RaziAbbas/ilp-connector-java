package money.fluid.ilp.ledger.model;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.interledger.cryptoconditions.Condition;
import org.interledger.cryptoconditions.Fulfillment;
import org.interledgerx.ilp.core.LedgerTransferStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Objects;
import java.util.Optional;

/*
 * ILPTransfer entities are created for ILP aware transfer.
 *    local or non-ILP transfers don't use it.
 *    Is used to keep trace of the ILP transaction status.
 * TODO: Move (or createAccount interface) to java-ilp-core
 */
@Getter
@EqualsAndHashCode
@ToString
public class ILPTransfer {
    private final String id;
    private final String ledgerId;
    private final LedgerTransferStatus status;
    private final DateTime proposedAt;

    private final Optional<Condition> optCondition; // When the fulfillment arrives compare with Condition
    private final Optional<String> optExtraInfo;
    private final Optional<Fulfillment> optExecutionFulfillment;
    private final Optional<Fulfillment> optCancelationFulfillment;
    private final Optional<DateTime> optExpirationAt;
    private final Optional<DateTime> optPreparedAt;
    private final Optional<DateTime> optExecutedAt;
    private final Optional<DateTime> optRejectedAt;

    public ILPTransfer(
            final String id, final String ledgerId, final LedgerTransferStatus status, final DateTime proposedAt,
            final Optional<Condition> optCondition,
            final Optional<String> optExtraInfo,
            final Optional<Fulfillment> optExecutionFulfillment,
            final Optional<Fulfillment> optCancelationFulfillment,
            final Optional<DateTime> optExpirationAt,
            final Optional<DateTime> optPreparedAt,
            final Optional<DateTime> optExecutedAt,
            final Optional<DateTime> optRejectedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.ledgerId = Objects.requireNonNull(ledgerId);
        this.status = Objects.requireNonNull(status);
        this.proposedAt = Optional.ofNullable(proposedAt).orElse(this.getCurrentTime());
        this.optCondition = Objects.requireNonNull(optCondition);
        this.optExtraInfo = Objects.requireNonNull(optExtraInfo);
        this.optExecutionFulfillment = Objects.requireNonNull(optExecutionFulfillment);
        this.optCancelationFulfillment = Objects.requireNonNull(optCancelationFulfillment);
        this.optExpirationAt = Objects.requireNonNull(optExpirationAt);
        this.optPreparedAt = Objects.requireNonNull(optPreparedAt);
        this.optExecutedAt = Objects.requireNonNull(optExecutedAt);
        this.optRejectedAt = Objects.requireNonNull(optRejectedAt);

        // Don't silently fail on the date preconditions!
        this.enforcePostConstructionPreconditions();
    }

//    public ILPTransfer(final ILPTransfer ilpTransfer) {
//        this(
//                ilpTransfer.getId(),
//                ilpTransfer.getLedgerId(),
//                ilpTransfer.getStatus(),
//                ilpTransfer.getProposedAt(),
//                ilpTransfer.getOptCondition(),
//                ilpTransfer.getOptExtraInfo(),
//                ilpTransfer.getOptExecutionFulfillment(),
//                ilpTransfer.getOptCancelationFulfillment(),
//                ilpTransfer.getOptExpirationAt(),
//                ilpTransfer.getOptPreparedAt(),
//                ilpTransfer.getOptExecutedAt(),
//                ilpTransfer.getOptRejectedAt()
//        );
//    }

    public ILPTransfer withNewStatus(final LedgerTransferStatus newLedgerTransferStatus) {
        Objects.requireNonNull(newLedgerTransferStatus);

        switch (newLedgerTransferStatus) {
            case PROPOSED: {
                // Throw an InterledgerException instead.
                throw new RuntimeException("new status " + newLedgerTransferStatus + " not allowed");
            }
            case PREPARED: {
                Preconditions.checkArgument(this.status == LedgerTransferStatus.PROPOSED);
                Preconditions.checkArgument(!optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            case EXECUTED: {
                Preconditions.checkArgument(this.status == LedgerTransferStatus.PREPARED);
                Preconditions.checkArgument(optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            case REJECTED: {
                Preconditions.checkArgument(this.status != LedgerTransferStatus.EXECUTED);
                Preconditions.checkArgument(optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            default: {
                throw new RuntimeException("Unhandled new status " + newLedgerTransferStatus);
            }
        }

        return new ILPTransfer(
                this.getId(),
                this.getLedgerId(),
                newLedgerTransferStatus,
                this.getProposedAt(),
                this.getOptCondition(),
                this.getOptExtraInfo(),
                this.getOptExecutionFulfillment(),
                this.getOptCancelationFulfillment(),
                this.getOptExpirationAt(),
                this.getOptPreparedAt(),
                this.getOptExecutedAt(),
                this.getOptRejectedAt()
        );
    }

    /**
     * Enforces preconditions necessary to construct a new instance of {@link ILPTransfer}.
     */
    private void enforcePostConstructionPreconditions() {
        switch (this.status) {
            case PROPOSED: {
                Preconditions.checkArgument(!optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            case PREPARED: {
                Preconditions.checkArgument(optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            case EXECUTED: {
                Preconditions.checkArgument(!optPreparedAt.isPresent());
                Preconditions.checkArgument(optExecutedAt.isPresent());
                Preconditions.checkArgument(!optRejectedAt.isPresent());
                break;
            }
            case REJECTED: {
                Preconditions.checkArgument(!optPreparedAt.isPresent());
                Preconditions.checkArgument(!optExecutedAt.isPresent());
                Preconditions.checkArgument(optRejectedAt.isPresent());
                break;
            }
            default: {
                throw new RuntimeException("Unhandled new status " + status);
            }
        }
    }

    private DateTime getCurrentTime() {
        return DateTime.now(DateTimeZone.UTC);
    }
}
