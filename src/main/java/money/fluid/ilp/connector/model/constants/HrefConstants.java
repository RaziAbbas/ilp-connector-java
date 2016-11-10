package money.fluid.ilp.connector.model.constants;

/**
 * Constants for Assembling Href URLs.
 */
public class HrefConstants {
    public static final String ROOT_CONTEXT = "";


    public static final String HEALTH = "health";

    // public static final String DASHBOARD = "dashboard";
    public static final String NUMBERS = "numbers";

    public static final String TASK = "task";

    // TODO #120: Rename this to ApiPaths, then create a generic Paths that has path constants that are shared between
    // the API and the Website.
    public static class Paths {
        public static final String SLASH = "/";

        // public static final String ROOT = SLASH + HrefConstants.ROOT_CONTEXT;
        public static final String HEALTH = SLASH + HrefConstants.HEALTH;

        public static final String NUMBERS = SLASH + HrefConstants.NUMBERS;

        //////////////
        // Task Queues
        //////////////
//		public static final String SERVLET_PATH__DEFAULT_TASK_QUEUE = "/_ah/queue/__deferred__";

        // /task
        public static final String SERVLET_PATH__TASK = SLASH + TASK;
        // For scheduling asynchronous sharded_counter increments and decrements.
//		public static final String SERVLET_PATH__ASYNC_COUNTER_INCREMENT = "/counters/increment";

        // Quota Counters
        public static final String SERVLET_PATH__QUOTA_COUNTER__AGGREGATE_OPERATION = "/quotacounters/aggregate";
    }

    public static class WebPaths {
        public static final String SLASH = "/";

        public static final String LEGAL__TERMS_OF_SERVICE = "/legal/terms.html";
        public static final String LEGAL__PRIVACY = "/legal/privacy.html";

        //////////////
        // Task Queues
        //////////////
        // public static final String SERVLET_PATH__DEFAULT_TASK_QUEUE = "/_ah/queue/__deferred__";

        // Path: /task
        //    public static final String SERVLET_PATH__TASK = SLASH + TASK;
    }

}
