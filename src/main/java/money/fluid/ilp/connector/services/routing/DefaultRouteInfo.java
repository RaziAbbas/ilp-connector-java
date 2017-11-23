package money.fluid.ilp.connector.services.routing;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.model.ids.ConnectorId;

/**
 * Created by dfuelling on 11/4/16.
 */
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class DefaultRouteInfo implements RouteInfo {

    // TODO: String?
    private final Object additionalInfo = null;

    // The local account identifier for the source ledger.  E.g., "mark" or "123"
    //private final LedgerAccountId sourceLedgerAccountId;
    //private final LedgerId destinationLedgerId;

    private final long minMessageWindow = -1;
    private final ConnectorId connectorId = null;

}
