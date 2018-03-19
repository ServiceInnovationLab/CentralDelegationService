package delegations.cds.services;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.NotAllowedException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.DataSource;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.querydsl.jpa.impl.JPAQuery;

import delegations.cds.auth.AuthenticatedContext;
import delegations.cds.models.Client;
import delegations.cds.models.QClient;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class ClientServiceTest {

    @Inject
    private ClientService clientService;

    @Deployment
    public static WebArchive deployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addPackages(true, Filters.exclude(".*Test.*"), "delegations.cds");
        war.addAsWebInfResource("test-beans.xml", "beans.xml");

        PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

        war.addAsLibraries(resolver
                .importDependencies(ScopeType.PROVIDED, ScopeType.COMPILE, ScopeType.RUNTIME, ScopeType.TEST)
                .resolve()
                .withTransitivity()
                .asFile());

        war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
        war.addAsResource("test-project-defaults.yml", "project-defaults.yml");

        return war;
    }

    @Test
    @UsingDataSet("clients.yml")
    public void disallowNormalClientToCreateClient() {

        Client dia = query()
                .from(QClient.client)
                .select(QClient.client)
                .where(QClient.client.crn.eq("crn:dia:test")).fetchOne();
        clientService.setAuthContext(new AuthenticatedContext(dia));

        Client newClient = Client.build("new", "newer","newest");

        ServiceResponse<Client> sr = clientService.create(newClient);


        assertThat(sr.successful()).isFalse();
        assertThat(sr.asError().exception()).isExactlyInstanceOf(NotAllowedException.class);
    }

    @Test
    @UsingDataSet("clients.yml")
    public void allowAdminClientToCreateClient() throws Exception {

        Client admin = query()
                .from(QClient.client)
                .select(QClient.client)
                .where(QClient.client.crn.eq("crn:cds:admin")).fetchOne();
        clientService.setAuthContext(new AuthenticatedContext(admin));

        Client newClient = Client.build("new", "newer","newest");

        ServiceResponse<Client> sr = clientService.create(newClient);

//        throw sr.asError().exception();
        assertThat(sr.successful()).isTrue();
    }

    protected JPAQuery<?> query() {
        JPAQuery<?> query = new JPAQuery<>(em);
        return query;
    }

    @PersistenceContext
    protected EntityManager em;






}
