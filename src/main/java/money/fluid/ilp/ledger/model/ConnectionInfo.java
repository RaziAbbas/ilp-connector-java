package money.fluid.ilp.ledger.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;

import java.util.UUID;

/**
 * This class defines the information required to be provided by an outside process attempting to connect to a {@link
 * Ledger}.  The combination of a clientId, version, and connectorId, plus a random identifier create a unique
 * identifier that allows a single connector to employ more than 1 ledger client on the same ledger (for whatever
 * reason).
 */
// TODO Make this an interface and move the impl to the inmemory package.
@RequiredArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ConnectionInfo {
    // A random identifier that allows a clientId/clientVersion/connectorId to be used more than once in the same
    // Ledger/Connector pair.  Not exposed - prefer getConnectionId
    @NonNull
    private final UUID internalRandomConnectionId = UUID.randomUUID();

    @NonNull
    @Getter
    private final String clientId = null;

    @NonNull
    @Getter
    private final String clientVersion = null;

    // TODO: Remove these comments...
    // This might be a wallet, or something else.  We use the ledgerAccountIlpAddress as the unique identifier instead.
    // The connectorId of the Connector that this client is communicating on behalf of.
    //@NonNull
    //@Getter
    //private final String connectorId;

    // The ILP address of this Connector on the Ledger that this ConnectionInfo will be used to connect to.  This should
    //  be specified by the client (and determined before-hand by an out-of-band process like a manual ledger
    // registration process).  This account id is required because a single connector might want to register for multiple
    // ledgerAccount events, and we want the ledger to be able to support this.
    @NonNull
    @Getter
    private final IlpAddress ledgerAccountIlpAddress = null;

    
    
    public UUID getInternalRandomConnectionId() {
		return internalRandomConnectionId;
	}



	public String getClientId() {
		return clientId;
	}



	public String getClientVersion() {
		return clientVersion;
	}



	public IlpAddress getLedgerAccountIlpAddress() {
		return ledgerAccountIlpAddress;
	}



	public String getConnectionId() {
        return String.format(
                "%s:%s:%s%s", this.clientId, this.clientVersion, this.ledgerAccountIlpAddress,
                this.internalRandomConnectionId
        );
    }
}
