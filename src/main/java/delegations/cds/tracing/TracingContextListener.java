package delegations.cds.tracing;


import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.opentracing.util.GlobalTracer;


@WebListener
public class TracingContextListener implements ServletContextListener {

    // Inspired by https://dzone.com/articles/opentracing-jax-rs-instrumentation
    @Inject
    private io.opentracing.Tracer tracer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    	// do nothing
    }

}
