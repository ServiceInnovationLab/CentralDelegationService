package delegations.cds.lib;

import delegations.cds.models.Client;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class GenerateCrnTest {

    private Client client;

    @Before
    public void setUp() {
        client = new Client();
        client.setPrivacyDomain("privdom");
        client.setService("test");
    }

    @Test
    public void generateDelegationCrn() {
        String crn1 = client.generateDelegationCrn();
        String crn2 = client.generateDelegationCrn();

        assertThat(crn1, startsWith("crn:privdom:test::delegation/"));
        assertThat(crn2, startsWith("crn:privdom:test::delegation/"));
        assertThat(crn2, not(equalTo(crn1)));
    }

    @Test
    public void generateRendezvousCrn() {
        String crn1 = client.generateRendezvousCrn();
        String crn2 = client.generateRendezvousCrn();

        assertThat(crn1, startsWith("crn:privdom:test::rendezvous/"));
        assertThat(crn2, startsWith("crn:privdom:test::rendezvous/"));
        assertThat(crn2, not(equalTo(crn1)));

    }

}
