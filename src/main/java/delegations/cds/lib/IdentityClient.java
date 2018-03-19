package delegations.cds.lib;

import java.text.MessageFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import delegations.cds.services.ServiceResponse;
import delegations.cds.util.Setting;
import delegations.cds.util.Settings;

@ApplicationScoped
public class IdentityClient {

    private static final String SYSTEM_PROPERTY_REALME_RCMS_API_KEY = "realme.rcms.api_key";
	private static final String SYSTEM_PROPERTY_REALME_RCMS_BASE_PATH = "realme.rcms.base_path";

	private Logger logger;

    private String realmeRcmsValidateEndpoint;
    private String apiKey;

    @Inject
    public IdentityClient(final Logger logger) {
        this.logger = logger;

        String rcmsBasePath = System.getProperty(SYSTEM_PROPERTY_REALME_RCMS_BASE_PATH, Settings.getSetting(Setting.RCMS_BASE_PATH_DEFAULT));
        realmeRcmsValidateEndpoint = rcmsBasePath + Settings.getSetting(Setting.RCMS_VALIDATE_ENDPOINT);

        apiKey = System.getProperty(SYSTEM_PROPERTY_REALME_RCMS_API_KEY, Settings.getSetting(Setting.API_KEY_DEFAULT));
    }

    IdentityClient() {
        // no-arg constructor to allow cdi proxying
    }

    public ServiceResponse<String> exchangeRcmsForCdsFlt(final String rcms) {
        ValidateRequest request = ValidateRequest.create(rcms);

        HttpResponse<ValidateResponse> response;
        try {
            response = Unirest.post(realmeRcmsValidateEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, String.format(Settings.getSetting(Setting.RCMS_AUTHORISATION_TOKEN_FORMAT), apiKey))
                    .body(request)
                    .asObject(ValidateResponse.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 200) {
            String message = MessageFormat.format("Validate RCMS failed, {0} {1}", response.getStatus(), response.getStatusText());
            logger.error(message);
            return ServiceResponse.forException(message);
        }

        return ServiceResponse.forSuccess(response.getBody().getFlt());
    }

    public static class ValidateRequest {
        private String token;
        private boolean allowCreate;

        public static ValidateRequest create(final String token) {
            ValidateRequest request = new ValidateRequest();
            request.token = token;
            request.allowCreate = true;
            return request;
        }

        public String getToken() {
            return token;
        }

        public void setToken(final String token) {
            this.token = token;
        }

        public boolean isAllowCreate() {
            return allowCreate;
        }

        public void setAllowCreate(final boolean allowCreate) {
            this.allowCreate = allowCreate;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidateResponse {
        private String flt;

        public String getFlt() {
            return flt;
        }

        public void setFlt(final String flt) {
            this.flt = flt;
        }
    }
}
