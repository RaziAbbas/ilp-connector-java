package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;

import javax.money.MonetaryAmount;
import java.util.Objects;

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
    private final InterledgerPacketHeader ilpPacketHeader;

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

    public Escrow(final EscrowInputs escrowInputs, final IlpAddress escrowAddress) {
        Objects.requireNonNull(escrowInputs);

        this.ilpPacketHeader = escrowInputs.getInterledgerPacketHeader();
        this.localSourceAddress = escrowInputs.getSourceAddress();
        this.escrowAddress = Objects.requireNonNull(escrowAddress);
        this.localDestinationAddress = escrowInputs.getDestinationAddress();
        this.amount = escrowInputs.getAmount();
    }
}
