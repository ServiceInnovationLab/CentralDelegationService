package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableUserView.class)
public interface UserView {

    @JsonProperty("id")
    public abstract Integer id();

    @JsonProperty("email")
    @Nullable
    public abstract String email();

}
