package money.fluid.ilp.ledger.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.ConnectorId;
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
    private final String clientId;

    @NonNull
    @Getter
    private final String clientVersion;

    // The connectorId of the Connector that this client is communicating on behalf of.
    @NonNull
    @Getter
    private final ConnectorId connectorId;

    public String getConnectionId() {
        return String.format(
                "%s:%s:%s%s", this.clientId, this.clientVersion, this.connectorId, this.internalRandomConnectionId);
    }
}
