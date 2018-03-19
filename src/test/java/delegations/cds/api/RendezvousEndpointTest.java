package delegations.cds.api;

import delegations.cds.models.QClient;
import delegations.cds.models.QDelegationType;
import delegations.cds.models.QRendezvous;
import delegations.cds.models.QResource;
import delegations.cds.models.QUser;
import delegations.cds.models.Rendezvous;
import io.restassured.response.Response;
import java.net.URL;
import javax.ws.rs.core.Response.Status;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class RendezvousEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @Deployment
    public static WebArchive deploy() { return createBaseDeployment();  }

    @CreateSwarm
    public static Swarm swarm() throws Exception {
        return createBaseContainer();
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void listRendezvous() throws Exception {
        Response response = validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous");

        response.then().statusCode(OK.getStatusCode());
        response.then().body("count", equalTo(3));
        response.then().body("rendezvouses.id", notNullValue());

        response.then().body("rendezvouses[0].owner_code", nullValue());

        response.then().body("rendezvouses[0].delegate_code", is("ABCDE"));

    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void sanityCheck() throws Exception {
        assertThat(query().from(QClient.client).fetchCount(), is(1L));
        assertThat(query().from(QDelegationType.delegationType).fetchCount(), is(3L));
        assertThat(query().from(QUser.user).fetchCount(), is(2L));
        assertThat(query().from(QResource.resource).fetchCount(), is(3L));
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void completeRendezvousAsDelegate() {

        Response response = validAuthSpec()
                .when()
                .post(basePath + "api/rendezvous/{code}/accept/{rcms}", "ABCDE", "rcms_two");

        response.then().statusCode(Status.OK.getStatusCode());
        response.then().body("delegation_ready", is(true));
        response.then().body("access_resource_uri", is(basePath + "api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}"));
        response.then().body("complete_owner_rendezvous_uri", nullValue());
        response.then().body("complete_delegate_rendezvous_uri", nullValue());

        // completing rendezvous consumes the code
        Rendezvous rz = query().select(QRendezvous.rendezvous)
                .from(QRendezvous.rendezvous)
                .where(QRendezvous.rendezvous.crn.eq("crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81"))
                .fetchOne();

        assertThat(rz.getDelegateCode(), is("ABCDE"));
        assertThat(rz.isDelegateCodeConsumed(), is(true));

        validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous")
                .then()
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81'}.owner_code", nullValue())
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81'}.delegate_code", nullValue());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/unknown_user.yml")
    public void completeWithUnknownUser() {

        Response response = validAuthSpec()
                .when()
                .post(basePath + "api/rendezvous/{code}/accept/{rcms}", "ABCDE", "rcms_two");

        response.then().statusCode(Status.OK.getStatusCode());
        response.then().body("delegation_ready", is(true));
        response.then().body("access_resource_uri", is(basePath + "api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}"));
        response.then().body("complete_owner_rendezvous_uri", nullValue());
        response.then().body("complete_delegate_rendezvous_uri", nullValue());

        // completing rendezvous consumes the code
        Rendezvous rz = query().select(QRendezvous.rendezvous)
                .from(QRendezvous.rendezvous)
                .where(QRendezvous.rendezvous.crn.eq("crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81"))
                .fetchOne();

        assertThat(rz.getDelegateCode(), is("ABCDE"));
        assertThat(rz.isDelegateCodeConsumed(), is(true));

        validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous")
                .then()
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81'}.owner_code", nullValue())
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/1b6880ba-a1c5-4512-8429-97c3c4a3ca81'}.delegate_code", nullValue());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void completeRendezvousAsOwner() {

        Response response = validAuthSpec()
                .when()
                .post(basePath + "api/rendezvous/{code}/accept/{rcms}", "QWERT", "rcms_one");

        response.then().statusCode(Status.OK.getStatusCode());
        response.then().body("delegation_ready", is(true));
        response.then().body("access_resource_uri", is(basePath + "api/resources/crn:dia:test:123456789013:templates/passport-2/access/{rcms_token}"));
        response.then().body("complete_owner_rendezvous_uri", nullValue());
        response.then().body("complete_delegate_rendezvous_uri", nullValue());

        // completing rendezvous consumes the code
        Rendezvous rz = query().select(QRendezvous.rendezvous)
                .from(QRendezvous.rendezvous)
                .where(QRendezvous.rendezvous.crn.eq("crn:dia:test::rendezvous/2a6e7d9e-9287-4827-b86e-7ca8bcd56505"))
                .fetchOne();

        assertThat(rz.getOwnerCode(), is("QWERT"));
        assertThat(rz.isOwnerCodeConsumed(), is(true));

        validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous")
                .then()
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/2a6e7d9e-9287-4827-b86e-7ca8bcd56505'}.owner_code", nullValue())
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/2a6e7d9e-9287-4827-b86e-7ca8bcd56505'}.delegate_code", nullValue());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void completeRendezvousAsDelegateWhenBothConsentsRequired() {
        Response response = validAuthSpec()
                .when()
                .post(basePath + "api/rendezvous/{code}/accept/{rcms}", "ASDFG", "rcms_two");

        response.then().statusCode(Status.OK.getStatusCode());
        response.then().body("delegation_ready", is(false));
        response.then().body("complete_owner_rendezvous_uri", is(basePath + "api/rendezvous/ZXCVB/accept/{rcms_token}"));
        response.then().body("complete_delegate_rendezvous_uri", nullValue());
        response.then().body("access_resource_uri", nullValue());

        // completing rendezvous consumes the code
        Rendezvous rz = query().select(QRendezvous.rendezvous)
                .from(QRendezvous.rendezvous)
                .where(QRendezvous.rendezvous.crn.eq("crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391"))
                .fetchOne();

        assertThat(rz.getOwnerCode(), is("ZXCVB"));
        assertThat(rz.isOwnerCodeConsumed(), is(false));
        assertThat(rz.getDelegateCode(), is("ASDFG"));
        assertThat(rz.isDelegateCodeConsumed(), is(true));

        validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous")
                .then()
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391'}.owner_code", is("ZXCVB"))
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391'}.delegate_code", nullValue());
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("rendezvous_endpoint/delegations.yml")
    public void completeRendezvousAsOwnerWhenBothConsentsRequired() {
        Response response = validAuthSpec()
                .when()
                .post(basePath + "api/rendezvous/{code}/accept/{rcms}", "ZXCVB", "rcms_one");

        response.then().statusCode(Status.OK.getStatusCode());
        response.then().body("delegation_ready", is(false));
        response.then().body("complete_owner_rendezvous_uri", nullValue());
        response.then().body("complete_delegate_rendezvous_uri", is(basePath + "api/rendezvous/ASDFG/accept/{rcms_token}"));
        response.then().body("access_resource_uri", nullValue());

        // completing rendezvous consumes the code
        Rendezvous rz = query().select(QRendezvous.rendezvous)
                .from(QRendezvous.rendezvous)
                .where(QRendezvous.rendezvous.crn.eq("crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391"))
                .fetchOne();

        assertThat(rz.getOwnerCode(), is("ZXCVB"));
        assertThat(rz.isOwnerCodeConsumed(), is(true));
        assertThat(rz.getDelegateCode(), is("ASDFG"));
        assertThat(rz.isDelegateCodeConsumed(), is(false));

        validAuthSpec()
                .when()
                .get(basePath + "api/rendezvous")
                .then()
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391'}.owner_code", nullValue())
                .body("rendezvouses.find {it.crn == 'crn:dia:test::rendezvous/a9637623-50a8-4798-aa7a-1c54b9a13391'}.delegate_code", is("ASDFG"));
    }

}
