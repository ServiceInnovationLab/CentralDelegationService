package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.sql.Timestamp;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDelegationView.class)
public abstract class DelegationView implements BaseView {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("crn")
    public abstract String crn();

    @JsonProperty("metadata")
    @Nullable
    public abstract String metadata();

    @JsonProperty("resource")
    public abstract ResourceView resource();

    @JsonProperty("delegation_type")
    public abstract DelegationTypeView delegationType();

    @JsonProperty("owner")
    @Nullable
    public abstract UserView owner();

    @JsonProperty("owner_consent_time")
    @Nullable
    public abstract Timestamp ownerConsentTime();

    @JsonProperty("delegate")
    @Nullable
    public abstract UserView delegate();

    @JsonProperty("delegate_consent_time")
    @Nullable
    public abstract Timestamp delegateConsentTime();

    @Nullable
    public abstract RendezvousView rendezvous();
}
