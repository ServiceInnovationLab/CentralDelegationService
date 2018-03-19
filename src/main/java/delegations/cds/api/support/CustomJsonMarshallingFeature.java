package delegations.cds.api.support;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

public class CustomJsonMarshallingFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }
}