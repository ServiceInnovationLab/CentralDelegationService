package delegations.cds.api;

import com.google.common.collect.Maps;
import delegations.cds.models.DelegationType;
import delegations.cds.models.QDelegationType;
import io.restassured.response.Response;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;


@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class DelegationTypesEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @Deployment
    public static WebArchive deploy() { return createBaseDeployment();  }

    @CreateSwarm
    public static Swarm swarm() throws Exception {
        return createBaseContainer();
    }

    @Test
    @UsingDataSet("delegation-types-one.yml")
    public void listDelegationTypes() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/delegation_types");

        response.then().statusCode(OK.getStatusCode());
        response.then().body("count", equalTo(2));
        response.then().body("delegation_types.name", hasItems("read-only", "read-write"));

    }

//    @Test
//    @UsingDataSet("delegation-types-one.yml")
//    public void listDelegationTypesWithDeleted() {
//
//        Response response = validAuthSpec()
//                .when()
//                .get(basePath + "api/delegation_types?include=deleted");
//
    // response.then().statusCode(OK.getStatusCode());
//        response.then().body("count", equalTo(3));
//        response.then().body("delegations_types.name", hasItems("read_only", "read-write", "legacy-publish"));
//
//    }


    @Test
    @UsingDataSet("delegation-types-one.yml")
    public void showDelegationType() {

        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/delegation_types/crn:dia:test::delegation_types/read-only");

        response.then().statusCode(OK.getStatusCode());
        response.then().body("name", is("read-only"));
        response.then().body("id", is(30001));
    }

    @Test
    @UsingDataSet("delegation-types-one.yml")
    public void createOne() throws Exception {
        Map<String, String> json = Maps.newHashMap();
        json.put("crn", "crn:dia:test::delegation_types/limited-update");
        json.put("name", "limited-update");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_types");

        assertThat(res.getStatusCode()).isEqualTo(CREATED.getStatusCode());

        URI uri = new URI(res.getHeaders().get("Location").getValue());
        assertThat(uri).hasPath("/api/delegation_types/crn:dia:test::delegation_types/limited-update");

        Response query = validAuthSpec()
                .when()
                .get(basePath + "api/delegation_types/crn:dia:test::delegation_types/limited-update")
        // .get(res.getHeaders().get("Location").getValue())
        ;

        query.then().body("name", is("limited-update"));

    }

    @Test
    @UsingDataSet("delegation-types-one.yml")
    public void reassignTemplate() throws Exception {

        QDelegationType dt = QDelegationType.delegationType;
        String crn = "crn:dia:test::delegation_types/read-only";

        DelegationType before = query().from(dt).select(dt).where(dt.crn.eq(crn)).fetchOne();
        assertThat(before.getTemplate().getCrn()).isEqualTo("crn:dia:test::templates/first-template");
        assertThat(before.getTemplate().getVersion()).isEqualTo(1);

        Map<String, String> jsonInner = Maps.newHashMap();
        jsonInner.put("name", "first-template");
        jsonInner.put("version", "2");

        Map<String, Object> json = Maps.newHashMap();
        json.put("template", jsonInner);

        Response res = validAuthSpec()
                .body(json)
                .when()
                .put(basePath + "api/delegation_types/crn:dia:test::delegation_types/read-only");

        assertThat(res.getStatusCode()).isEqualTo(OK.getStatusCode());


        em.clear(); // dump the current context, to avoid getting the cached result
        DelegationType after = query().from(dt).select(dt).where(dt.crn.eq(crn)).fetchOne();

        assertThat(after.getTemplate().getCrn()).isEqualTo("crn:dia:test::templates/first-template");
        assertThat(after.getTemplate().getVersion()).isEqualTo(2);


    }

    @Test
    @UsingDataSet("delegation-types-one.yml")
    public void deleteDelegationType() {

        QDelegationType dt = QDelegationType.delegationType;


        String crn = "crn:dia:test::delegation_types/read-only";

        Long beforeCount = query().from(dt).where(
                dt.deleteTime.isNotNull(), dt.crn.eq(crn)
        ).fetchCount();

        Assert.assertThat(beforeCount, is(0L));

        Response response = validAuthSpec()
                .when()
                .delete(basePath + "api/delegation_types/" + crn);


        response.then().statusCode(OK.getStatusCode());

        Long afterCount = query().from(dt).where(
                dt.deleteTime.isNotNull(), dt.crn.eq(crn)
        ).fetchCount();

        Assert.assertThat(afterCount, is(1L));

    }
}
