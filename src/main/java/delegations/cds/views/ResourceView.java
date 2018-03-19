package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableResourceView.class)
public interface ResourceView {

    @JsonProperty("crn")
    public abstract String crn();

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("client_crn")
    public abstract String clientCrn();

    @JsonProperty("user")
    @Nullable
    public abstract UserView user();

}
