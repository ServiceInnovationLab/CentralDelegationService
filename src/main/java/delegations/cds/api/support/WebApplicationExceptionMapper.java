package delegations.cds.api.support;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;

/**
 * Explicit {@link ExceptionMapper} for {@link WebApplicationException}, so that the catch-all mapper doesn't catch them.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Inject
    private Logger logger;

    @Override
    public Response toResponse(final WebApplicationException exception) {
        logger.debug("Mapping WebApplicationException {}", exception.getMessage());
        logger.trace("Mapping WebApplicationException", exception);
        return exception.getResponse();
    }

}
