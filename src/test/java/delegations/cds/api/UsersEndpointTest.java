package delegations.cds.api;

import io.restassured.response.Response;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class UsersEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @Deployment
    public static WebArchive deploy() { return createBaseDeployment();  }

    @CreateSwarm
    public static Swarm swarm() throws Exception { return createBaseContainer(); }

    @Test
    @UsingDataSet("users-one.yml")
    public void listUsers() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/users");

        response.then().statusCode(OK.getStatusCode());
        response.then().body("count", equalTo(2));
        response.then().body("users.id", hasItems(90001, 90002));
        response.then().body("users[0].email", is("user1@example.com"));

        // FLTs should not be exposed by the API
        response.then().body("users[0].realme_flt", nullValue());
    }
}
