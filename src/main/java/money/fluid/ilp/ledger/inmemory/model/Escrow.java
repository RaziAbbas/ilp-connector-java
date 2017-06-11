package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;
import org.joda.time.DateTime;

import javax.money.MonetaryAmount;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * A class that outlines an initiateEscrow arrangement between two parties for which a given ledger is the
 * initiateEscrow agent.  This implementation assumes that the source and destination accounts are held by the same ILP
 * ledger (e.g., a sender and a connector), and therefore are denominated by the same asset type.
 */
@Getter
@RequiredArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Escrow {

    // Holds the ilp transaction id, the ultimate source and destination of funds from an ILP perspective (might be
    // different from the local accounts in this escrow)
    @NonNull
    private final InterledgerPacketHeader interledgerPacketHeader;

    // The ILP address of the local account that funded this escrow. This is often different
    // from ILP Source Address.
    @NonNull
    private final IlpAddress localSourceAddress;

    // The ILP address of the escrow account money is sitting in.  Structured as an ILP address to possibly enable
    // off-ledger escrowing.
    @NonNull
    private final IlpAddress escrowAddress;

    // The ILP address of the local account that will recieve funds if this escrow is executed.  This is often different
    // from ILP Destination Address.
    @NonNull
    private final IlpAddress localDestinationAddress;

    @NonNull
    private final MonetaryAmount amount;

    // This is the expiry of the escrow...
    private final Optional<DateTime> optExpiry;

    @NonNull
    private final Status status;

    public Escrow(final EscrowInputs escrowInputs, final IlpAddress escrowAddress) {
        Objects.requireNonNull(escrowInputs);

        this.interledgerPacketHeader = escrowInputs.getInterledgerPacketHeader();
        this.localSourceAddress = escrowInputs.getLocalSourceAddress();
        this.escrowAddress = Objects.requireNonNull(escrowAddress);
        this.localDestinationAddress = escrowInputs.getLocalDestinationAddress();
        this.optExpiry = escrowInputs.getOptExpiry();
        this.amount = escrowInputs.getAmount();
        this.status = Status.PENDING;
    }

    /**
     * Copy Constructor to update escrow status.
     *
     * @param escrow
     * @param newStatus
     */
    public Escrow(final Escrow escrow, final Status newStatus) {
        Objects.requireNonNull(escrow);

        this.interledgerPacketHeader = escrow.getInterledgerPacketHeader();
        this.localSourceAddress = escrow.getLocalSourceAddress();
        this.escrowAddress = escrow.getEscrowAddress();
        this.localDestinationAddress = escrow.getLocalDestinationAddress();
        this.optExpiry = escrow.getOptExpiry();
        this.amount = escrow.getAmount();
        this.status = newStatus;
    }

    public enum Status {
        // The Escrow has is waiting to be fulfilled or rejected.
        PENDING,
        // The escrow has been executed.  See Javadoc on EscrowService#execute.
        EXECUTED,
        // The escrow has been reversed.  See Javadoc on EscrowService#reverse
        REVERSED
    }
}
