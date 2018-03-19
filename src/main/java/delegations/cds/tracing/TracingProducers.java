package delegations.cds.tracing;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.ext.Provider;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@Provider
public class TracingProducers {

    @Produces
    @ApplicationScoped
    public io.opentracing.Tracer jaegerTracer() {

        Tracer tracer =
         new Configuration("wildfly-cds",
                new Configuration.SamplerConfiguration(
                        ProbabilisticSampler.TYPE, 1),
                new Configuration.ReporterConfiguration())
                .getTracer();

        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer);
        }

        return tracer;
    }


}
