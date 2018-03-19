package delegations.cds.api;

import com.google.common.collect.Maps;
import delegations.cds.models.QClient;
import delegations.cds.models.QDelegationType;
import delegations.cds.models.QResource;
import delegations.cds.models.QUser;
import io.restassured.response.Response;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class DelegationRequestsEndpointTest extends ApiTest {

    @ArquillianResource
    URL basePath;

    @PersistenceContext
    private EntityManager em;

    @Deployment
    public static WebArchive deploy() {
        return createBaseDeployment();
    }

    @CreateSwarm
    public static Swarm swarm() throws Exception {
        return createBaseContainer();
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests.yml")
    public void sanityCheck() {
        assertThat(query().from(QClient.client).fetchCount(), is(1L));
        assertThat(query().from(QDelegationType.delegationType).fetchCount(), is(2L));
        assertThat(query().from(QUser.user).fetchCount(), is(2L));
        assertThat(query().from(QResource.resource).fetchCount(), is(1L));
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests.yml")
    // @Cleanup(strategy = CleanupStrategy.STRICT)
    public void bothPartiesKnownAndConsented() {
        Map<String, String> json = Maps.newHashMap();
        json.put("delegation_type_crn", "crn:dia:test::delegations/test-1");
        json.put("resource_crn", "crn:dia:test:123456789012:templates/passport-1");
        json.put("owner_rcms", "rcms_one");
        json.put("owner_consent", "true");
        json.put("agent_rcms", "rcms_two");
        json.put("agent_consent", "true");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_requests");

        res.then().statusCode(Status.OK.getStatusCode());
        res.then().body("delegation_ready", is(true));
        res.then().body("access_resource_uri", is(basePath + "api/resources/crn:dia:test:123456789012:templates/passport-1/access/{rcms_token}"));

        String accessResourceUri = res.getBody().jsonPath().get("access_resource_uri");

        // At this point, a delegation has been created. We should be able to test it.
        Response accessResponse = validAuthSpec()
                .when()
                .get(accessResourceUri, "rcms_two");

        accessResponse.then().statusCode(Status.OK.getStatusCode());
        accessResponse.then().body("permitted", is(true));

    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests.yml")
    public void ownerKnownPushDelegation() {
        Map<String, String> json = Maps.newHashMap();
        json.put("delegation_type_crn", "crn:dia:test::delegations/test-1");
        json.put("resource_crn", "crn:dia:test:123456789012:templates/passport-1");
        json.put("owner_rcms", "rcms_one");
        json.put("owner_consent", "true");
        json.put("agent_email", "agent@example.com");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_requests");

        res.then().statusCode(Status.ACCEPTED.getStatusCode())
                .body("delegation_ready", is(false))
                .body("complete_delegate_rendezvous_uri", allOf(startsWith(basePath + "api/rendezvous/"), endsWith("/accept/{rcms_token}")))
                .body("complete_delegate_rendezvous_code", notNullValue());

        // confirm delegation and rendezvous got created

        String uri = res.body().jsonPath().get("complete_delegate_rendezvous_uri");

        Matcher matcher = Pattern.compile(".*/api/rendezvous/([a-z0-9-]+)/accept/\\{rcms_token\\}").matcher(uri);
        assertThat(matcher.matches(), is(true));
        String code = matcher.group(1);

        validAuthSpec()
                .when()
                .get(basePath + "api/delegations")
                .then()
                .log().all()
                .body("count", is(1))
                .body("delegations[0].rendezvous.owner_code", nullValue())
                .body("delegations[0].rendezvous.delegate_code", is(code));
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests-no-users.yml")
    public void unknownUserPushDelegation() {
        Map<String, String> json = Maps.newHashMap();
        json.put("delegation_type_crn", "crn:dia:test::delegations/test-1");
        json.put("resource_crn", "crn:dia:test:123456789012:templates/passport-1");
        json.put("owner_rcms", "rcms_one");
        json.put("owner_consent", "true");
        json.put("agent_email", "agent@example.com");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_requests");

        res.then().statusCode(Status.ACCEPTED.getStatusCode())
                .body("delegation_ready", is(false))
                .body("complete_delegate_rendezvous_uri", allOf(startsWith(basePath + "api/rendezvous/"), endsWith("/accept/{rcms_token}")))
                .body("complete_delegate_rendezvous_code", notNullValue());

        // confirm delegation and rendezvous got created

        String uri = res.body().jsonPath().get("complete_delegate_rendezvous_uri");

        Matcher matcher = Pattern.compile(".*/api/rendezvous/([a-z0-9-]+)/accept/\\{rcms_token\\}").matcher(uri);
        assertThat(matcher.matches(), is(true));
        String code = matcher.group(1);

        validAuthSpec()
                .when()
                .get(basePath + "api/delegations")
                .then()
                .log().all()
                .body("count", is(1))
                .body("delegations[0].rendezvous.owner_code", nullValue())
                .body("delegations[0].rendezvous.delegate_code", is(code));
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests.yml")
    public void delegateKnownPullDelegation() {
        Map<String, String> json = Maps.newHashMap();
        json.put("delegation_type_crn", "crn:dia:test::delegations/test-1");
        json.put("resource_crn", "crn:dia:test:123456789012:templates/passport-1");
        json.put("agent_rcms", "rcms_two");
        json.put("agent_consent", "true");
        json.put("owner_email", "owner@example.com");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_requests");

        res.then().statusCode(Status.ACCEPTED.getStatusCode());
        res.then().body("delegation_ready", is(false));
        res.then().body("complete_owner_rendezvous_uri", allOf(startsWith(basePath + "api/rendezvous/"), endsWith("/accept/{rcms_token}")));

        // confirm delegation and rendezvous got created

        String uri = res.body().jsonPath().get("complete_owner_rendezvous_uri");

        Matcher matcher = Pattern.compile(".*/api/rendezvous/([a-z0-9-]+)/accept/\\{rcms_token\\}").matcher(uri);
        assertThat(matcher.matches(), is(true));
        String code = matcher.group(1);

        validAuthSpec()
                .when()
                .get(basePath + "api/delegations")
                .then()
                .log().all()
                .body("count", is(1))
                .body("delegations[0].rendezvous.delegate_code", nullValue())
                .body("delegations[0].rendezvous.owner_code", is(code));
    }

    @Test
    @Transactional(TransactionMode.ROLLBACK)
    @UsingDataSet("simple-requests.yml")
    public void ownerAndDelegateUnkown() {
        Map<String, String> json = Maps.newHashMap();
        json.put("delegation_type_crn", "crn:dia:test::delegations/test-1");
        json.put("resource_crn", "crn:dia:test:123456789012:templates/passport-1");
        json.put("agent_email", "agent@example.com");
        json.put("owner_email", "owner@example.com");

        Response res = validAuthSpec()
                .body(json)
                .when()
                .post(basePath + "api/delegation_requests");

        res.then().statusCode(Status.ACCEPTED.getStatusCode());
        res.then().body("delegation_ready", is(false));
        res.then().body("complete_owner_rendezvous_uri", allOf(startsWith(basePath + "api/rendezvous/"), endsWith("/accept/{rcms_token}")));
        res.then().body("complete_delegate_rendezvous_uri", allOf(startsWith(basePath + "api/rendezvous/"), endsWith("/accept/{rcms_token}")));

        // confirm delegation and rendezvous got created

        String ownerUri = res.body().jsonPath().get("complete_owner_rendezvous_uri");
        String delegateUri = res.body().jsonPath().get("complete_delegate_rendezvous_uri");

        Pattern uriPattern = Pattern.compile(".*/api/rendezvous/([a-z0-9-]+)/accept/\\{rcms_token\\}");

        Matcher ownerMatcher = uriPattern.matcher(ownerUri);
        assertThat(ownerMatcher.matches(), is(true));
        String ownerCode = ownerMatcher.group(1);

        Matcher delegateMatcher = uriPattern.matcher(delegateUri);
        assertThat(delegateMatcher.matches(), is(true));
        String delegteCode = delegateMatcher.group(1);

        validAuthSpec()
                .when()
                .get(basePath + "api/delegations")
                .then()
                .body("count", is(1))
                .body("delegations[0].rendezvous.delegate_code", is(delegteCode))
                .body("delegations[0].rendezvous.owner_code", is(ownerCode));
    }

}
