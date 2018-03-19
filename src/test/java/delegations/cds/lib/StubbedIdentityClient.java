package delegations.cds.lib;

import com.google.common.collect.Maps;
import delegations.cds.services.ServiceResponse;
import java.text.MessageFormat;
import java.util.Map;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.slf4j.Logger;

@Alternative
public class StubbedIdentityClient extends IdentityClient {

    private final Map<String, String> rcmsStore;
    private final Logger logger;

    @Inject
    public StubbedIdentityClient(final Logger logger) {
        super(logger);
        this.logger = logger;
        rcmsStore = Maps.newHashMap();
        rcmsStore.put("rcms_one", "flt_one");
        rcmsStore.put("rcms_two", "flt_two");
    }

    @Override
    public ServiceResponse<String> exchangeRcmsForCdsFlt(final String rcms) {
        logger.debug(MessageFormat.format("Exchanging rcms {0}", rcms));
        if (rcmsStore.containsKey(rcms)) {
            return ServiceResponse.forSuccess(rcmsStore.get(rcms));
        } else {
            throw new NotFoundException("Not a known rcms token");
        }
    }

}
