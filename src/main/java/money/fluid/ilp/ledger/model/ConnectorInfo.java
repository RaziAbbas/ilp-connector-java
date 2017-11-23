package money.fluid.ilp.ledger.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.Connector;
import money.fluid.ilp.connector.model.ids.ConnectorId;
import org.interledgerx.ilp.core.IlpAddress;
import org.interledgerx.ilp.core.Ledger;

/**
 * Provides information about a Connector from a {@link Ledger} perspective.
 */
@RequiredArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
// TODO Make this an interface
public class ConnectorInfo {
    /**
     * The unique identifier of a {@link Connector}.
     *
     * @return
     */
    @NonNull
    private final ConnectorId connectorId = null;

    // TODO: Split this into 2 classes.  Sometimes, this class is specified without an accountId because the object is
    // coming from the Connector.  However, once in the Ledger, the ledger will populate this account, and this is useful
    // to be able to lookup a LedgerEventListener by connectorId.
    @NonNull
    private final IlpAddress ilpAddress = null;

	public ConnectorId getConnectorId() {
		return connectorId;
	}

	public IlpAddress getIlpAddress() {
		return ilpAddress;
	}
}
