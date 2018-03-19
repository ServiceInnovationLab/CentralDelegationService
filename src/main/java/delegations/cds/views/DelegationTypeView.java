package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDelegationTypeView.class)
public abstract class DelegationTypeView implements BaseView {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("crn")
    public abstract String crn();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("template") @Nullable
    public abstract TemplateView template();

    @JsonProperty("metadata_keys")
    @Nullable
    public abstract String metadataKeys();


}
