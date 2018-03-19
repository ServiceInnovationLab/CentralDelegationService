package delegations.cds.api;

import static io.restassured.RestAssured.given;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.MediaType;

import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.wildfly.swarm.Swarm;

import com.querydsl.jpa.impl.JPAQuery;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.specification.RequestSpecification;

public abstract class ApiTest {

    protected static final String WEBAPP_SRC = "src/main/webapp";
    protected static final String WEB_INF = "src/main/webapp/WEB-INF";

    @PersistenceContext
    protected EntityManager em;

    @Inject
    protected UserTransaction utx;

    protected void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }

    public void commitTransaction() throws Exception {
        utx.commit();
    }

    protected <T> int deleteAllEntities(final Class<T> entityType) {
        String query = new StringBuilder("DELETE FROM ")
                .append(entityType.getSimpleName())
                .append(" e")
                .toString();
        return em.createQuery(query).executeUpdate();
    }

    public static WebArchive createBaseDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addPackages(true, Filters.exclude(".*Test.*"), "delegations.cds");
//        war.addPackages(true, "org.postgresql");
        war.addAsWebInfResource("test-beans.xml", "beans.xml");
        war.addAsWebInfResource("jboss-deployment-structure.xml");

        PomEquippedResolveStage resolver = Maven.resolver().loadPomFromFile("pom.xml");

        // https://github.com/shrinkwrap/resolver
        war.addAsLibraries(resolver
                //.importRuntimeDependencies()
                .importDependencies(ScopeType.PROVIDED, ScopeType.COMPILE, ScopeType.RUNTIME, ScopeType.TEST)
                .resolve()
                .withTransitivity()
                .asFile());

//        war.addPackages(true, "org.postgresql");

//        war.addAsManifestResource("org.postgresql:postgresql");


        // Tests that expose the UI will have to add Omnifaces etc.

        // war.addAsWebInfResource(new File(WEB_INF,"test-beans.xml"), "test-beans.xml");
        // war.addAsWebInfResource(new File(WEB_INF,"web.xml"), "web.xml");
        // war.addAsWebInfResource(new File(WEB_INF,"faces-config.xml"), "faces-config.xml");

        // resources
        war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
        war.addAsResource("test-project-defaults.yml", "project-defaults.yml");
//        war.addAsResource("logging.properties", "logging.properties");

//        System.out.println(war.toString(true));

        return war;
    }

    public static Swarm createBaseContainer() throws Exception {

        return new Swarm()
//                .start()
//                .deploy(Swarm.artifact("org.postgresql:postgresql", "postgresql"))
//                .withProperty("arquillian.debug", "true")
//                .withProperty("log4j.debug", "true")

                .withProperty("swarm.port.offset", "1")
                .withProperty("swarm.debug.port", "8082")

//                .withProperty("swarm.jdbc.driver", "org.postgresql.Driver")
                .withProperty("swarm.ds.name", "TestDS")
                .withProperty("swarm.ds.username", "docker")
                .withProperty("swarm.ds.password", "docker")
                .withProperty("swarm.ds.connection.url", "jdbc:postgresql://127.0.0.1:5432/cds-test");


    }

    // Just a helper to set valid credentials and turn request logging on
    protected RequestSpecification validAuthSpec() {
        return given()
                .config(RestAssured.config().logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                // pre-emptively sends credentials
                .auth().preemptive().basic("test", "testsecret");
    }

    protected JPAQuery<?> query() {
        JPAQuery<?> query = new JPAQuery<>(em);
        return query;
    }

}
