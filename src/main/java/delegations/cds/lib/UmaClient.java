package delegations.cds.lib;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import delegations.cds.models.Resource;
import delegations.cds.models.User;
import delegations.cds.models.enums.UmaResourceStatus;
import delegations.cds.services.ServiceResponse;
import delegations.cds.util.Setting;
import delegations.cds.util.Settings;

@ApplicationScoped
public class UmaClient {

    
	private static final String SYSTEM_PROPERTY_OAUTH2_CLIENT_ID = "oauth2.client_id";
	private static final String SYSTEM_PROPERTY_OAUTH2_CLIENT_SECRET = "oauth2.client_secret";
	private static final String POST_FIELD_CLIENT_SECRET = "client_secret";
	private static final String POST_FIELD_CLIENT_ID = "client_id";
	private static final String POST_FIELD_SCOPE = "scope";
	private static final String POST_FIELD_PASSWORD = "password";
	private static final String POST_FIELD_USERNAME = "username";
	private static final String POST_FIELD_GRANT_TYPE = "grant_type";
	
	private static final String ACCESS_TOKEN_SCOPE_UMA_AUTHORIZATION = "uma_authorization";
	private static final String ACCESS_TOKEN_SCOPE_UMA_PROTECTION = "uma_protection";
	
	private static final String QUERY_PARAM_TOKEN = "token";
	private static final String QUERY_PARAM_ACTION = "_action";
	
	private static final String ROUTE_PARAM_POLICY_ID = "policyId";
	private static final String ROUTE_PARAM_OWNER_UID = "ownerUid";
	
	private static final String SYSTEM_PROPERTY_OPENAM_COOKIE_NAME = "openam.cookie_name";
	private static final String SYSTEM_PROPERTY_OPENAM_BASE_PATH = "openam.base_path";

	private static final String HTTP_HEADER_X_OPEN_AM_PASSWORD = "X-OpenAM-Password";
	private static final String HTTP_HEADER_X_OPEN_AM_USERNAME = "X-OpenAM-Username";

	private static final String SCOPE = "default";

    private Logger logger;

    private String oauth2AccessTokenEndpoint;

    private String selfRegistrationEndpoint;
    private String authenticationEndpoint;
    private String sessionsEndpoint;

    private String resourceSetRegistrationEndpoint;
    private String umaPoliciesEndpoint;
    private String permissionRegistrationEndpoint;
    private String tokenIntrospectionEndpoint;

    private String rptEndpoint;

    private String openamSessionIdentifier;

    @Inject
    public UmaClient(final Logger logger) {
        this.logger = logger;

        String openamBasePath = System.getProperty(SYSTEM_PROPERTY_OPENAM_BASE_PATH, Settings.getSetting(Setting.OPENAM_BASE_PATH_DEFAULT));

        oauth2AccessTokenEndpoint = openamBasePath + Settings.getSetting(Setting.ACCESS_TOKEN_ENDPOINT);

        selfRegistrationEndpoint = openamBasePath + Settings.getSetting(Setting.SELF_REGISTRATION_ENDPOINT);
        authenticationEndpoint = openamBasePath + Settings.getSetting(Setting.AUTHENTICATION_ENDPOINT);
        sessionsEndpoint = openamBasePath + Settings.getSetting(Setting.SESSION_ENDPOINT);

        resourceSetRegistrationEndpoint = openamBasePath + Settings.getSetting(Setting.RESOURCE_SET_REGISTRATION_ENDPOINT);
        umaPoliciesEndpoint = openamBasePath + Settings.getSetting(Setting.UMA_POLICIES_ENDPOINT);
        tokenIntrospectionEndpoint = openamBasePath + Settings.getSetting(Setting.TOKEN_INTROSPECTION_ENDPOINT);

        // note: there appears to be an issue in openam that prevents specifying the realm in the path for the uma endpoints
        permissionRegistrationEndpoint = openamBasePath + Settings.getSetting(Setting.PERMISSION_REGISTRATION_ENDPOINT);
        rptEndpoint = openamBasePath + Settings.getSetting(Setting.RPT_ENDPOINT);

        openamSessionIdentifier = System.getProperty(SYSTEM_PROPERTY_OPENAM_COOKIE_NAME, Settings.getSetting(Setting.OPENAM_SESSION_IDENTIFIER_DEFAULT));
    }

    UmaClient() {
        // no-arg constructor for to allow cdi proxying
    }

    // See if a resource is protected
    public ServiceResponse<UmaResourceStatus> checkResourceStatus(final String resourceIdenfier) {

        if (resourceIdenfier == null) {
            return ServiceResponse.forSuccess(UmaResourceStatus.UNKNOWN);
        }

        // placeholder
        return ServiceResponse.forSuccess(UmaResourceStatus.UNKNOWN);
    }

    public ServiceResponse<Resource> registerResource(final Resource resource) {

        // Acquire user's PAT
        User user = resource.getUser();
        ServiceResponse<User> acquirePatResponse = acquirePat(user);
        if (!acquirePatResponse.successful()) {
            return acquirePatResponse.propagate();
        }
        User userWithPat = acquirePatResponse.asSuccess().payload();

        // register resource set
        RegisterResourceSetRequest request = new RegisterResourceSetRequest();
        request.setName(resource.getCrn());
        request.setScopes(Lists.newArrayList(SCOPE)); // think about this

        HttpResponse<RegisterResourceSetResponse> response;
        try {
            response = Unirest.post(resourceSetRegistrationEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, buildBearerHeaderValue(userWithPat.getUmaPat()))
                    .body(request)
                    .asObject(RegisterResourceSetResponse.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 201) {
            return ServiceResponse.forException(MessageFormat.format("Failed to register resource set, {0} {1}", response.getStatus(), response.getStatusText()));
        }

        RegisterResourceSetResponse result = response.getBody();

        resource.setOpenamId(result.getId());
        resource.setUser(userWithPat);
        return ServiceResponse.forSuccess(resource);

    }

    public ServiceResponse<UmaResourceStatus> protectResource(final Resource resource, final User delegate) {
        User owner = resource.getUser();
        ServiceResponse<User> registerResponse = ensureRegisteredAtOpenAm(delegate);
        if (!registerResponse.successful()) {
            return registerResponse.propagate();
        }
        User registeredDelegate = registerResponse.asSuccess().payload();

        // check whether resource is already shared with this delegate

        ServiceResponse<String> authnResponse = authenticateAtOpenAm(owner);
        if (!authnResponse.successful()) {
            return authnResponse.propagate();
        }
        String sessionToken = authnResponse.asSuccess().payload();

        CreateUmaPolicyRequest request = CreateUmaPolicyRequest.create(resource.getOpenamId(), registeredDelegate.getOpenamUid(), SCOPE);

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.put(umaPoliciesEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .routeParam(ROUTE_PARAM_OWNER_UID, owner.getOpenamUid())
                    .routeParam(ROUTE_PARAM_POLICY_ID, resource.getOpenamId())
                    .header(openamSessionIdentifier, sessionToken)
                    .body(request)
                    .asJson();
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 201) {
            String message = MessageFormat.format("Creating policy failed, {0} {1}", response.getStatus(), response.getStatusText());
            logger.error(message);
            throw new IllegalStateException(message); // placeholder for error handling
        }

        response.getHeaders().getFirst(HttpHeaders.LOCATION);

        endOpenAmSession(sessionToken);

        return ServiceResponse.forSuccess(UmaResourceStatus.PROTECTED);
    }

    public ServiceResponse<Boolean> checkAccessToResource(final Resource resource, final User accessor) {
        Objects.requireNonNull(resource, "You must provide a resource");
        Objects.requireNonNull(accessor, "You must provide an accessor");

        User owner = resource.getUser();

        if (owner == null) {
            return ServiceResponse.forSuccess(false);
        }

        // 1. permission request and PAT -> permission ticket
        UmaPermissionRequest permissionRequest = new UmaPermissionRequest();
        permissionRequest.setResourceSetId(resource.getOpenamId());
        permissionRequest.setScopes(Lists.newArrayList(SCOPE));

        HttpResponse<PermissionTicket> ticketResponse;
        try {
            ticketResponse = Unirest.post(permissionRegistrationEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, buildBearerHeaderValue(owner.getUmaPat()))
                    .body(permissionRequest)
                    .asObject(PermissionTicket.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (ticketResponse.getStatus() != 201) {
            // error handling
            String message = MessageFormat.format("Failed to create permission request, {0} {1}", ticketResponse.getStatus(), ticketResponse.getStatusText());
            logger.error(message);
            throw new IllegalStateException(message);
        }

        String permissionTicket = ticketResponse.getBody().getTicket();

        // 2. permission ticket and AAT -> RPT
        ServiceResponse<String> acquireAatResponse = acquireAat(accessor);
        if (!acquireAatResponse.successful()) {
            return acquireAatResponse.propagate();
        }
        String accessorAat = acquireAatResponse.asSuccess().payload();

        PermissionTicket rptRequest = new PermissionTicket();
        rptRequest.setTicket(permissionTicket);

        HttpResponse<RequestingPartyToken> rptResponse;
        try {
            rptResponse = Unirest.post(rptEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, buildBearerHeaderValue(accessorAat))
                    .body(rptRequest)
                    .asObject(RequestingPartyToken.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (rptResponse.getStatus() != 200) {
            // do error handling properly
            String message = MessageFormat.format("RPT request failed, {0} {1}", rptResponse.getStatus(), rptResponse.getStatusText());
            logger.error(message);
            throw new IllegalStateException(message);
        }

        String rpt = rptResponse.getBody().getRpt();

        // 3. introspect RPT
        // we just got it - really need to introspect? (Normally client would hand it to resource server and rs would introspect, but we are both client and rs)
        HttpResponse<JsonNode> introspectionResponse;
        try {
            introspectionResponse = Unirest.post(tokenIntrospectionEndpoint)
                    .queryString(QUERY_PARAM_TOKEN, rpt)
                    .basicAuth(fetchClientId(), fetchClientSecret())
                    .asJson();
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (introspectionResponse.getStatus() != 200) {
            // error handling
        }

        return ServiceResponse.forSuccess(true);
    }

    private ServiceResponse<User> acquirePat(final User user) {

        if (user.getUmaPat() != null) {
            // should check validity of pat here - get a fresh one if it's expired
            return ServiceResponse.forSuccess(user);
        }

        ServiceResponse<User> registerResponse = ensureRegisteredAtOpenAm(user);
        if (!registerResponse.successful()) {
            return registerResponse.propagate();
        }
        User registeredUser = registerResponse.asSuccess().payload();
        ServiceResponse<String> accessToken = acquireOauth2AccessToken(registeredUser, ACCESS_TOKEN_SCOPE_UMA_PROTECTION);
        if (!accessToken.successful()) {
            return accessToken.propagate();
        } else {
            return accessToken.asSuccess().propagate(pat -> {
                logger.debug("Acquired PAT: {}", accessToken);
                registeredUser.setUmaPat(pat);
                return registeredUser;
            });
        }

    }

    private ServiceResponse<String> acquireAat(final User user) {
        return acquireOauth2AccessToken(user, ACCESS_TOKEN_SCOPE_UMA_AUTHORIZATION);
    }

    private ServiceResponse<String> acquireOauth2AccessToken(final User user, final String scope) {
        ServiceResponse<User> registerResponse = ensureRegisteredAtOpenAm(user);
        if (!registerResponse.successful()) {
            return registerResponse.propagate();
        }
        User registeredUser = registerResponse.asSuccess().payload();

        HttpResponse<Oauth2AccessTokenResponse> response;
        try {
            response = Unirest.post(oauth2AccessTokenEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .field(POST_FIELD_GRANT_TYPE, "password")
                    .field(POST_FIELD_USERNAME, registeredUser.getOpenamUid())
                    .field(POST_FIELD_PASSWORD, registeredUser.getOpenamPassword())
                    .field(POST_FIELD_SCOPE, scope)
                    .field(POST_FIELD_CLIENT_ID, fetchClientId())
                    .field(POST_FIELD_CLIENT_SECRET, fetchClientSecret())
                    .asObject(Oauth2AccessTokenResponse.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 200) {
            String message = MessageFormat.format("Failed to acquire access token, {0} {1}", response.getStatus(), response.getStatusText());

            logger.error(message);
            throw new IllegalStateException(message);
        }

        String accessToken = response.getBody().getAccessToken();
        logger.debug("Acquired access_token: {}", accessToken);

        return ServiceResponse.forSuccess(accessToken); // ignoring possibliity of refresh token; resource owner password credential grant doesn't supply one
    }

    private ServiceResponse<User> ensureRegisteredAtOpenAm(final User user) {

        if (user.getOpenamUid() != null) {
            return ServiceResponse.forSuccess(user);
        }

        user.setOpenamUid(UUID.randomUUID().toString());
        user.setOpenamPassword(UUID.randomUUID().toString());

        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(selfRegistrationEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .queryString(QUERY_PARAM_ACTION, "submitRequirements")
                    .body(SelfRegistrationRequest.create(user.getOpenamUid(), user.getOpenamPassword()))
                    .asJson();
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 200) {
            String message = MessageFormat.format("Failed to register user at openam, {0}. {1}", response.getStatus(), response.getStatusText());

            logger.error(message);
            throw new IllegalStateException(message);
        }

        return ServiceResponse.forSuccess(user);
    }

    private ServiceResponse<String> authenticateAtOpenAm(final User user) {

        HttpResponse<OpenAmAuthenticationResponse> response;
        try {
            response = Unirest.post(authenticationEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HTTP_HEADER_X_OPEN_AM_USERNAME, user.getOpenamUid())
                    .header(HTTP_HEADER_X_OPEN_AM_PASSWORD, user.getOpenamPassword())
                    .asObject(OpenAmAuthenticationResponse.class);
        } catch (UnirestException e) {
            return ServiceResponse.forException(e);
        }

        if (response.getStatus() != 200) {
            String message = MessageFormat.format("Authentication for user {0} failed, {1} {2}", user.getId(), response.getStatus(), response.getStatusText());

            logger.error(message);
            throw new IllegalStateException(message); // placeholder for error handling
        }

        return ServiceResponse.forSuccess(response.getBody().getTokenId());
    }

    private void endOpenAmSession(final String sessionToken) {

        try {
            Unirest.post(sessionsEndpoint)
                    .header(openamSessionIdentifier, sessionToken)
                    .queryString(QUERY_PARAM_ACTION, "logout")
                    .asJson();
        } catch (UnirestException e) {
            throw new IllegalStateException(e);
        }
    }

    private String fetchClientSecret() {
        return System.getProperty(SYSTEM_PROPERTY_OAUTH2_CLIENT_SECRET, Settings.getSetting(Setting.OAUTH2_CLIENT_SECRET_DEFAULT));
    }

    private String fetchClientId() {
        return System.getProperty(SYSTEM_PROPERTY_OAUTH2_CLIENT_ID, Settings.getSetting(Setting.OAUTH2_CLIENT_ID_DEFAULT));
    }

    private String buildBearerHeaderValue(final String bearerToken) {
        return "Bearer " + bearerToken;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Oauth2AccessTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private long expiresIn;

        @JsonProperty("refresh_token")
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(final String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(final String tokenType) {
            this.tokenType = tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(final long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(final String refreshToken) {
            this.refreshToken = refreshToken;
        }

    }

    public static class SelfRegistrationRequest {
        private SelfRegistrationRequestInput input;

        private static SelfRegistrationRequest create(final String username, final String password) {
            SelfRegistrationRequestUser user = new SelfRegistrationRequestUser();
            user.username = username;
            user.userPassword = password;

            SelfRegistrationRequestInput requestInput = new SelfRegistrationRequestInput();
            requestInput.user = user;

            SelfRegistrationRequest request = new SelfRegistrationRequest();
            request.input = requestInput;
            return request;
        }

        public SelfRegistrationRequestInput getInput() {
            return input;
        }

        public void setInput(final SelfRegistrationRequestInput input) {
            this.input = input;
        }

    }

    public static class SelfRegistrationRequestInput {
        private SelfRegistrationRequestUser user;

        public SelfRegistrationRequestUser getUser() {
            return user;
        }

        public void setUser(final SelfRegistrationRequestUser user) {
            this.user = user;
        }
    }

    public static class SelfRegistrationRequestUser {
        private String username;
        private String userPassword;

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getUserPassword() {
            return userPassword;
        }

        public void setUserPassword(final String userPassword) {
            this.userPassword = userPassword;
        }

    }

    public static class RegisterResourceSetRequest {
        private String name;
        private List<String> scopes;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(final List<String> scopes) {
            this.scopes = scopes;
        }
    }

    public static class RegisterResourceSetResponse {

        @JsonProperty("_id")
        private String id;

        @JsonProperty("user_access_policy_uri")
        private String userAccessPolicyUri;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getUserAccessPolicyUri() {
            return userAccessPolicyUri;
        }

        public void setUserAccessPolicyUri(final String userAccessPolicyUri) {
            this.userAccessPolicyUri = userAccessPolicyUri;
        }
    }

    public static class CreateUmaPolicyRequest {
        private String policyId;
        private List<UmaPolicyPermission> permissions;

        private static CreateUmaPolicyRequest create(final String resourceSetId, final String delegateUid, final String scope) {
            CreateUmaPolicyRequest request = new CreateUmaPolicyRequest();
            request.setPolicyId(resourceSetId);

            UmaPolicyPermission permission = new UmaPolicyPermission();
            permission.subject = delegateUid;
            permission.scopes = Lists.newArrayList(scope);

            request.permissions = Lists.newArrayList(permission);
            return request;
        }

        public String getPolicyId() {
            return policyId;
        }

        public void setPolicyId(final String policyId) {
            this.policyId = policyId;
        }

        public List<UmaPolicyPermission> getPermissions() {
            return permissions;
        }

        public void setPermissions(final List<UmaPolicyPermission> permissions) {
            this.permissions = permissions;
        }
    }

    public static class UmaPolicyPermission {
        private String subject;
        private List<String> scopes;

        public String getSubject() {
            return subject;
        }

        public void setSubject(final String subject) {
            this.subject = subject;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(final List<String> scopes) {
            this.scopes = scopes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenAmAuthenticationResponse {
        private String tokenId;

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(final String tokenId) {
            this.tokenId = tokenId;
        }
    }

    public static class UmaPermissionRequest {

        @JsonProperty("resource_set_id")
        private String resourceSetId;
        private List<String> scopes;

        public String getResourceSetId() {
            return resourceSetId;
        }

        public void setResourceSetId(final String resourceSetId) {
            this.resourceSetId = resourceSetId;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(final List<String> scopes) {
            this.scopes = scopes;
        }
    }

    public static class PermissionTicket {
        private String ticket;

        public String getTicket() {
            return ticket;
        }

        public void setTicket(final String ticket) {
            this.ticket = ticket;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequestingPartyToken {
        private String rpt;

        public String getRpt() {
            return rpt;
        }

        public void setRpt(final String rpt) {
            this.rpt = rpt;
        }
    }

}
