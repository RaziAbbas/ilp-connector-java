package money.fluid.ilp.connector.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import money.fluid.ilp.connector.Connector;
import money.fluid.ilp.connector.model.ids.ConnectorId;

/**
 * Provides information about a Connector from a Connector's perspective.
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
    private final ConnectorId connectorId;
}
