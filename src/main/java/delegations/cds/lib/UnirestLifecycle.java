package delegations.cds.lib;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

public class UnirestLifecycle {

    public void startup(@Observes @Initialized(ApplicationScoped.class) final ServletContext payload) {

        Unirest.setDefaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        // configure objectmapper
        // use jackson since we've got it
        Unirest.setObjectMapper(new UniresetObjectMapper());

        // configure timeouts and connection pool
        // these are currently set to Unirest's defaults, but demonstrate how to configure these values

        // Using a custom http client because that's the only way to not keep openam's cookies (we don't want them)
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(200);
        connManager.setDefaultMaxPerRoute(20);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(60))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(1)) // set short so we don't wait around for a long time if the pool is busy
                .build();

        HttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        Unirest.setHttpClient(httpClient);

        // Unirest.setConcurrency(
        // 200, // Max connections total - default: 200
        // 20); // MAx connections per route - default: 20
        //
        // Unirest.setTimeouts(
        // 10_000L, // connection timeout - default: 10000 ms
        // 60_000L); // socket timeout - default: 60000 ms

    }

    public void shutdown(@Observes @Destroyed(ApplicationScoped.class) final ServletContext payload) {
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public class UniresetObjectMapper implements ObjectMapper {
        private final com.fasterxml.jackson.databind.ObjectMapper jacksonOM = new com.fasterxml.jackson.databind.ObjectMapper();

        @Override
        public String writeValue(final Object value) {
            try {
                return jacksonOM.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public <T> T readValue(final String value, final Class<T> valueType) {
            try {
                return jacksonOM.readValue(value, valueType);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
