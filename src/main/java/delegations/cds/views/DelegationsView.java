package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableDelegationsView.class)
public abstract class DelegationsView implements BaseView {

    @JsonProperty("delegations")
    public abstract List<DelegationView> delegations();

    @JsonProperty("count")
    @Value.Derived
    public int count() {
        return delegations().size();
    }

}
