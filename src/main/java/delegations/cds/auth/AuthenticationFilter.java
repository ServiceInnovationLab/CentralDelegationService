package delegations.cds.auth;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import delegations.cds.models.Client;
import delegations.cds.services.ClientService;
import delegations.cds.services.ServiceResponse;
import delegations.cds.util.Setting;
import delegations.cds.util.Settings;

@Provider
@RequestScoped
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String WWW_AUTHENTICATE_HEADER_FORMAT = "Basic realm=cds";

	private static final String AUTH_HEADER_TYPE_BASIC = "Basic";

	private static final List<String> AUTHZ_NOT_REQUIRED_WHITELIST = Lists.newArrayList(
            Settings.getSetting(Setting.AUTHZ_NOT_REQUIRED_CONFIG_FILE));

    @Inject
    private Logger logger;

    @Inject
    private ClientService clientService;

    private AuthenticatedContext authContext;


    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        String requestedPath = requestContext.getUriInfo().getPath();

        logger.debug("Checking authz for path {}", requestedPath);

        if (AUTHZ_NOT_REQUIRED_WHITELIST.contains(requestedPath)) {
            logger.debug("Authorization not required for path {}", requestedPath);
            return;
        }

        String authzHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authzHeader == null) {
            logger.debug("Authorization header not found");
            unauthorized(requestContext);
            return;
        }

        String[] authzHeaderPieces = authzHeader.split(" ", 2);

        if (authzHeaderPieces.length != 2 && !Objects.equals(authzHeaderPieces[0], AUTH_HEADER_TYPE_BASIC)) {
            logger.debug("Authorization header is not basic auth, {}", authzHeader);
            unauthorized(requestContext);
            return;
        }

        String basicAuth = new String(Base64.getDecoder().decode(authzHeaderPieces[1]), StandardCharsets.UTF_8);
        String[] credentials = basicAuth.split(":", 2);

        String accessKey = URLDecoder.decode(credentials[0], StandardCharsets.UTF_8.name());
        String secretKey = credentials[1];

        logger.debug("Received client access key {}", accessKey);

        ServiceResponse<Client> sr = clientService.findByAccessKey(accessKey);

        if (sr.successful()) {
            Client client = sr.asSuccess().payload();
            if (!client.getSecretKey().equals(secretKey)) {
                logger.debug("No match on secret key {}", secretKey);
                unauthorized(requestContext);
            }
            authContext = new AuthenticatedContext(client);
        } else {
            logger.debug("No client found");
            unauthorized(requestContext);
        }
    }

    private void unauthorized(final ContainerRequestContext requestContext) {
        authContext = null;
        requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_HEADER_FORMAT)
                .build());
    }

    @Produces
    public AuthenticatedContext getAuthContext() {
        return authContext;
    }

}
