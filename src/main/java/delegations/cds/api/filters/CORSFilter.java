package delegations.cds.api.filters;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(110)
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {

        String requestedOrigin = requestContext.getHeaderString("Origin");
        if (requestedOrigin == null) {
            // only set CORS headers if an Origin is provided
            return;
        }

        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Origin", requestedOrigin);

        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Credentials", "true");

        String requestedMethods = requestContext.getHeaderString("Access-Control-Request-Methods");
        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Methods", requestedMethods == null ? "GET,POST,OPTIONS" : requestedMethods);

        String requestedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
        responseContext.getHeaders()
                .putSingle("Access-Control-Allow-Headers", requestedHeaders == null ? "Authorization" : requestedHeaders);

    }

}
