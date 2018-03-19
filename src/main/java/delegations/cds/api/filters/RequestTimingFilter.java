package delegations.cds.api.filters;

import java.io.IOException;
import java.net.InetAddress;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;

@Provider
@Priority(100)
public class RequestTimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    private Logger logger;

    @Context
    private HttpServletRequest sr;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty("request:start", System.currentTimeMillis());
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        Object propStart = requestContext.getProperty("request:start");
        if (propStart == null) {
            return;
        }
        long start = (Long) propStart;
        long end = System.currentTimeMillis();
        requestContext.setProperty("request:end", end);
        long elapsed = end - start;

        logger.info("method={} path={} host={} fwd={} elapsed={}ms",
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath(true),
                InetAddress.getLocalHost().getHostName(),
                sr.getRemoteAddr(),
                elapsed);
    }
}
