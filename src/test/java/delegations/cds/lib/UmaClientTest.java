package delegations.cds.lib;

import delegations.cds.api.ApiTest;
import delegations.cds.models.Resource;
import delegations.cds.models.User;
import delegations.cds.services.ServiceResponse;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class UmaClientTest extends ApiTest {

    @Inject
    private UmaClient umaClient;

    @Deployment
    public static WebArchive deploy() {
        return createBaseDeployment();
    }

    @CreateSwarm
    public static Swarm swarm() throws Exception {
        return createBaseContainer();
    }

    @Test
    @UsingDataSet("users-one.yml")
    public void sanityCheck() {

        Resource resource = new Resource();
        User accessor = new User();

        try {
            umaClient.checkAccessToResource(null, accessor);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("You must provide a resource"));
        }

        try {
            umaClient.checkAccessToResource(resource, null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), is("You must provide an accessor"));
        }

        ServiceResponse<Boolean> response = umaClient.checkAccessToResource(resource, accessor);

        assertThat(response.successful(), is(true));
        assertThat(response.asSuccess().payload(), is(false));
    }

}
