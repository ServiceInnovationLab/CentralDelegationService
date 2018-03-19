package delegations.cds.api;

import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import io.swagger.jaxrs.config.BeanConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
@SwaggerDefinition(
        info = @Info(
                title = "Central Delegation Service API",
                version = "0.1",
                description = "Provides endpoints for creating and using delegations"),
        tags = {
                @Tag(
                        name = "core",
                        description = "The core api, creating and using a delegation."),
                @Tag(
                        name = "info",
                        description = "Provides additional information to clients."),
                @Tag(
                        name = "admin",
                        description = "The admin api, restricted to admin-enabled clients."),
                @Tag(
                        name = "delegations",
                        description = "A delegation represents a resource-owner granting access to a resource to an agent."
                                + " Creating a delegation will implicitly create a rendezvous if required."),
                @Tag(
                        name = "resources",
                        description = "Resources owned by a resource-owner, managed by a client."
                                + " A resource must be created before it can be delegated."),
                @Tag(
                        name = "delegation_types",
                        description = "Each delegation must be of a specific type."),
                @Tag(
                        name = "rendezvous",
                        description = "Represents a pending or completed rendezvous between a resource-owner and an agent."
                                + " Rendezvous are created automatically if creating a delegation requires it."
                                + " Completing a rendezvous will implicitly complete the creation of the corresponding delegation.")

        },
        securityDefinition = @SecurityDefinition(
                basicAuthDefinitions = @BasicAuthDefinition(
                        key = "client-auth",
                        description = "client-id and secret sent via Basic Auth."
                                + " The service expects client-id to be url-encoded;"
                                + " this allows including colons (:) in the client-id.")))
public class Application extends javax.ws.rs.core.Application {

    public Application() {
        super();
        // set up swagger
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setBasePath(System.getProperty("swagger.context.root", "") + "/api");

        beanConfig.setResourcePackage("delegations.cds.api");
        beanConfig.setScan(true);
    }

}
