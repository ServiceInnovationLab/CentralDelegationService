package delegations.cds.api.support;

import javax.persistence.PersistenceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(final PersistenceException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
