package delegations.cds.api;


import com.google.common.collect.Maps;
import delegations.cds.models.QResource;
import io.restassured.response.Response;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
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

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class ResourcesEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @Deployment
    public static WebArchive deploy() { return createBaseDeployment();  }

    @CreateSwarm
    public static Swarm swarm() throws Exception { return createBaseContainer(); }


    @Test
    @UsingDataSet("resources-one.yml")
    public void createOne() {
        Map<String, String> json = Maps.newHashMap();
        json.put("crn", "red");

        Response response = validAuthSpec()
                .body(json)
        .when()
                .post(basePath + "api/resources");

        response.then().statusCode(CREATED.getStatusCode());

        String loc = response.getHeader(HttpHeaders.LOCATION);

        validAuthSpec().when()
                .get(loc)
                .then().statusCode(200);
    }

    @Test
    @UsingDataSet("resources-one.yml")
    public void listResources() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/resources");


        response.then().statusCode(OK.getStatusCode());
        response.then().body("count", equalTo(2));
        response.then().body("resources.id", hasItems(20001, 20002));
        response.then().body("resources.user.id", hasItems(90001));

    }

    @Test
    @UsingDataSet("resources-one.yml")
    public void showResource() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/resources/crn:dia:test:123456789012:templates/passport-1");


        response.then().statusCode(OK.getStatusCode());
        response.then().body("crn", is("crn:dia:test:123456789012:templates/passport-1"));

    }

    @Test
    @UsingDataSet("resources-one.yml")
    public void deleteResource() {

        QResource r= QResource.resource;

        assertThat(query().from(r).where(r.deleteTime.isNotNull()).fetchCount(), is(0L));

        String crn = "crn:dia:test:123456789012:templates/passport-1";

        Response response = validAuthSpec()
                .when()
                .delete(basePath + "api/resources/" + crn);


        response.then().statusCode(OK.getStatusCode());

        Long count = query().from(r).where(
                r.deleteTime.isNotNull(), r.crn.eq(crn)
        ).fetchCount();

        assertThat(count, is(1L));

    }


}
