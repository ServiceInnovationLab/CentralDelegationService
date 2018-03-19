package delegations.cds.util;

public enum Setting {

	ACCESS_TOKEN_ENDPOINT("access.token.endpoint"),
	SELF_REGISTRATION_ENDPOINT("self.registration.endpoint"),
	AUTHENTICATION_ENDPOINT("authentication.endpoint"),
	SESSION_ENDPOINT("session.endpoint"),
	RESOURCE_SET_REGISTRATION_ENDPOINT("resource.set.registration.endpoint"),
	UMA_POLICIES_ENDPOINT("uma.policies.endpoint"),
	TOKEN_INTROSPECTION_ENDPOINT("token.introspection.endpoint"),
	PERMISSION_REGISTRATION_ENDPOINT("permission.registration.endpoint"),
	RPT_ENDPOINT("rpt.endpoint"),
	RCMS_VALIDATE_ENDPOINT("rcms.validate.endpoint"),
	
	OPENAM_BASE_PATH_DEFAULT("openam.base.path.default"),
	OAUTH2_CLIENT_SECRET_DEFAULT("oauth2.client.secret.default"),
	OPENAM_SESSION_IDENTIFIER_DEFAULT("openam.session.identifier.default"),
	OAUTH2_CLIENT_ID_DEFAULT("oauth2.client.id.default"),
	RCMS_BASE_PATH_DEFAULT("rcms.base.path.default"),
	API_KEY_DEFAULT("api.key.default"),
	
	RCMS_AUTHORISATION_TOKEN_FORMAT("rcms.authorisation.token.format"),
	JSF_RCMS_TOKEN_DEFAULT("jsf.rmcs.token.default"),
	AUTHZ_NOT_REQUIRED_CONFIG_FILE("authorisation.not.required.config.file"),
	
	RENDEZVOUS_EMAIL_ADDRESS_SENDER_FIELD("rendezvous.email.address.sender.field"),
	RENDEZVOUS_EMAIL_ADDRESS_FROM_FIELD("rendezvous.email.address.from.field"),
	RENDEZVOUS_EMAIL_SENDER_NAME("rendezvous.email.sender.name"),
	RENDEZVOUS_EMAIL_DELEGATE_NAME("rendezvous.email.delegate.name"),
	RENDEZVOUS_EMAIL_OWNER_NAME("rendezvous.email.owner.name"),
	RENDEZVOUS_EMAIL_SUBJECT("rendezvous.email.subject");
	
	private String value;
	
	private Setting(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
