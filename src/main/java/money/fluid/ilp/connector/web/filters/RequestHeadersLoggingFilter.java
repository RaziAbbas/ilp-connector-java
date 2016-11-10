package money.fluid.ilp.connector.web.filters;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A Servlet Filter that logs incoming headers for the request.
 */
@Component
public class RequestHeadersLoggingFilter extends com.sappenin.utils.servletfilters.RequestHeadersLoggingFilter
        implements Filter {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());


    @Override
    protected Set<String> getObfuscatedHeaderNames() {
        return ImmutableSet.<String>builder().build();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
