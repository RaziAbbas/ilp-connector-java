package money.fluid.ilp.connector.model.constants;

/**
 * Constants for Headers.
 */
public class HeaderConstants {
    /**
     * Headers supplied by the Appengine Subsystems.
     */
    public static class AppengineHeaders {
        //public static final String X_APPENGINE_QUEUENAME = "X-AppEngine-QueueName";
    }

    /**
     * Versions supported by the Instacount API.
     */
    public static class ApiVersions {
        public static final String API_VERSION_1 = "application/vnd.sappenin-ilp-connector.v1+json";
        public static final String API_VERSION_DEFAULT = "application/vnd.sappenin-ilp-connector+json";

        //public static final MediaType API_VERSION_1_TYPE = new MediaType("application", "vnd.instacount.v1+json");
        //public static final MediaType API_VERSION_DEFAULT_TYPE = new MediaType("application", "vnd.instacount+json");
    }
}
