package delegations.cds.lib;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class CrnSchemaTest {

    @Test
    public void testMissingPartsOne() {
        ValidationResponse vr = CrnSchema.validateClient("crn::");
        assertThat(vr.isValid(), is(false));
        assertThat(vr.formattedMessage(), is("Crn must have 5 parts. crn:: has 3"));
    }

    @Test
    public void testMissingPartsTwo() {
        ValidationResponse vr = CrnSchema.validateClient("bob::::");
        assertThat(vr.isValid(), is(false));
        assertThat(vr.formattedMessage(), is("Crn must start with crn:"));
    }


    @Test
    public void testMissingPartsThree() {
        ValidationResponse vr = CrnSchema.validateClient("crn::::");
        assertThat(vr.isValid(), is(false));
        assertThat(vr.formattedMessage(), is("Crn crn:::: is not in a valid form\nCrn must contain a privacy domain\nCrn must contain a service"));
    }

    @Test
    public void testAccountNumberTooShort() {
        ValidationResponse vr = CrnSchema.validateClient("crn:a:b:1234567890:something");
        assertThat(vr.isValid(), is(false));
        assertThat(vr.formattedMessage(), is("Crn crn:a:b:1234567890:something is not in a valid form\nAccount numbers must be 12 digits"));
    }

    @Test
    public void testValidOne() {
        ValidationResponse vr = CrnSchema.validateClient("crn:datacom:identity:123456789012:my/docs");
        assertThat(vr.isValid(), is(true));
        assertThat(vr.formattedMessage(), is(""));
    }

    @Test
    public void testValidTwo() {
        ValidationResponse vr = CrnSchema.validateDelegationType("crn:dia:test::delegations/test-1");
        assertThat(vr.formattedMessage(), is(""));
        assertThat(vr.isValid(), is(true));

    }

    @Test
    public void testValidThree() {
        ValidationResponse vr = CrnSchema.validateResource("crn:dia:test:123456789012:templates/passport-1");
        assertThat(vr.formattedMessage(), is(""));
        assertThat(vr.isValid(), is(true));

    }

    @Test
    public void testFoundInWild() {
        ValidationResponse vr = CrnSchema.validateResource("crn:dia:test::delegation_types/limited-update");
        assertThat(vr.formattedMessage(), is(""));
        assertThat(vr.isValid(), is(true));
    }

}
