package money.fluid.ilp.connector.model.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;
import java.util.Map;

/**
 * A class that contains meta-data information for a Connector.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class ConnectorMeta {

    @JsonProperty("urls")
    private final Map<String, URI> urls = null;

}
