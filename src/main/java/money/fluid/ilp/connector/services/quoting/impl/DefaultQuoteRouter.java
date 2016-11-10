package money.fluid.ilp.connector.services.quoting.impl;


import money.fluid.ilp.connector.services.quoting.QuoteService.LocalQuoteService;
import money.fluid.ilp.connector.services.quoting.QuoteService.RemoteQuoteService;
import money.fluid.ilp.connector.services.SupportedAssetsService;
import money.fluid.ilp.connector.services.quoting.QuoteRouter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * An implementation of {@link QuoteRouter} that extends {@link AbstractQuoteRouter} for default functionality.
 */
@Service
@Qualifier("default")
public class DefaultQuoteRouter extends AbstractQuoteRouter implements QuoteRouter {

    /**
     * Required-args Constructor.
     *
     * @param supportedAssetsService An instance of {@link SupportedAssetsService}.
     * @param localQuoteService      An instance of {@link LocalQuoteService}.
     * @param remoteQuoteService     An instance of {@link RemoteQuoteService}.
     */
    @Inject
    public DefaultQuoteRouter(
            final SupportedAssetsService supportedAssetsService,
            @Qualifier("default") final LocalQuoteService localQuoteService,
            @Qualifier("default") final RemoteQuoteService remoteQuoteService
    ) {
        super(supportedAssetsService, localQuoteService, remoteQuoteService);
    }

}
