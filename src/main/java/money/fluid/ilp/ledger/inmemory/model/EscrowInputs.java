package money.fluid.ilp.ledger.inmemory.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.InterledgerPacketHeader;

import javax.money.MonetaryAmount;

/**
 * A class that outlines the data necessary to create an escrow arrangement between two parties for which a
 * given ledger is the escrow agent.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class EscrowInputs {

    // Holds the ilp transaction id, the ultimate source and destination of funds from an ILP perspective (might be
    // different from the local accounts in this escrow)
    @NonNull
    private final InterledgerPacketHeader interledgerPacketHeader;

    // The ILP address of the local account that funded this escrow. This is often different
    // from ILP Source Address.
    @NonNull
    private final IlpAddress sourceAddress;

    // The ILP address of the local account that will recieve funds if this escrow is executed.  This is often different
    // from ILP Destination Address.
    @NonNull
    private final IlpAddress destinationAddress;

    @NonNull
    private final MonetaryAmount amount;
}
