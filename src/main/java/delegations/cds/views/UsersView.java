package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableUsersView.class)
public interface UsersView {

    @JsonProperty("users")
    public abstract List<UserView> users();

    @JsonProperty("count")
    @Value.Derived
    public default int count() {
        return users().size();
    }
}
