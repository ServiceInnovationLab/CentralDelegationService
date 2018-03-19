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
import static org.hamcrest.core.IsCollectionContaining.hasItems;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class DelegationsEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @Deployment
    public static WebArchive deploy() { return createBaseDeployment();  }

    @CreateSwarm
    public static Swarm swarm() throws Exception { return createBaseContainer(); }

    @Test
    @UsingDataSet("delegations-one.yml")
    public void listDelegations() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/delegations");


        response.then().statusCode(OK.getStatusCode());
        response.then().body("count", equalTo(2));
        response.then().body("delegations.id", hasItems(40001, 40002));
        response.then().body("delegations.resource.crn", hasItems("crn:dia:test:123456789012:templates/passport-1"));
        response.then().body("delegations[0].delegation_type.name", is("read_only"));
    }


    @Test
    @UsingDataSet("delegations-one.yml")
    public void showDelegation() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/delegations/crn:dia:test::delegations/84a4d1f5-aa81-45dc-b1d2-b90abdecb463");


        response.then().statusCode(OK.getStatusCode());
        response.then().body("id", is(40001));
        response.then().body("resource.crn", is("crn:dia:test:123456789012:templates/passport-1"));
        response.then().body("delegation_type.crn", is("crn:dia:test::delegation_types/read-only"));

    }


}
