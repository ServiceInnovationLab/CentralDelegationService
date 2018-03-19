package delegations.cds.services;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

import delegations.cds.models.Delegation;
import delegations.cds.models.Email;
import delegations.cds.models.QDelegation;

@DataSource(value = "jboss/datasources/TestDS")
@RunWith(Arquillian.class)
public class RendezvousServiceTest {

    @Inject
    private RendezvousService rendezvousService;

    @Deployment
    public static WebArchive  deployment() {
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
    @UsingDataSet("rendezvous-one.yml")
    public void canGenerateEmail() {

        String crn =  "crn:dia:test::delegation/43046b2b-839a-4aba-a50e-c70212d88fc4";

        Delegation delegation = query()
                .from(QDelegation.delegation)
                .select(QDelegation.delegation)
                .where(QDelegation.delegation.crn.eq(crn)).fetchOne();



        Email email = rendezvousService.generateEmail(delegation);

        assertThat(email).isNotNull();
        assertThat(email.content()).contains(
            "<span>user1@example.com</span> has requested to share " +
            "<span>crn:dia:test:123456789012:templates/passport-1</span> " +
            "with <span >user2@example.com</span>"
        );
    }

    protected JPAQuery<?> query() {
        JPAQuery<?> query = new JPAQuery<>(em);
        return query;
    }

    @PersistenceContext
    protected EntityManager em;


}

