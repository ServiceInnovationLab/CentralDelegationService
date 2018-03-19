package delegations.cds.api.support;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;

/**
 * Catch-all mapper for handling anything that there isn't a more specific {@link ExceptionMapper} for. Returns 500 Internal Server Error.
 */
@Provider
public class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    private Logger logger;

    @Override
    public Response toResponse(final Throwable exception) {
        logger.debug("Uncaught exception {}", exception.getMessage());
        logger.trace("Uncaught exception", exception);
        return Response.serverError()
                .entity(exception.getMessage())
                .build();
    }

}
