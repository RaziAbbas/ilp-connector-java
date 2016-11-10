package money.fluid.ilp.connector.services;

import com.google.common.collect.ImmutableMap;
import money.fluid.ilp.connector.model.constants.UriConstants;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

/**
 * A meta service for determining information about "this" connector.
 */
@Service
public class WhoAmIService {

    /**
     * Get the root URI that this connector is served on.
     *
     * @return
     */
    public URI getRootUri() {
        // TODO: Externalize this to the config (https://github.com/sappenin/interledger-connectorj/issues/1).
        return URI.create("localhost:8080");
    }

    /**
     * Returns a {@link Map} of all URI's supported by this connector.
     *
     * @return A {@link Map} of all URI's supported by this connector.
     */
    public Map<String, URI> getMyUris() {
        final String rootUri = getRootUri().toString();
        return ImmutableMap.of(
                UriConstants.HEALTH, URI.create(rootUri.concat(UriConstants.SLASH + UriConstants.HEALTH)),
                UriConstants.PAIRS, URI.create(rootUri.concat(UriConstants.SLASH + UriConstants.PAIRS)),
                UriConstants.PAYMENT, URI.create(rootUri.concat(UriConstants.SLASH + UriConstants.PAYMENT)),
                UriConstants.QUOTE, URI.create(rootUri.concat(UriConstants.SLASH + UriConstants.QUOTE)),
                UriConstants.NOTIFICATIONS, URI.create(rootUri.concat(UriConstants.SLASH + UriConstants.NOTIFICATIONS))
        );
    }


}
