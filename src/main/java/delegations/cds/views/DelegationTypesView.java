package delegations.cds.views;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableDelegationTypesView.class)
public interface DelegationTypesView {

    @JsonProperty("delegation_types")
    public abstract List<DelegationTypeView> delegationTypes();

    @JsonProperty("count")
    @Value.Derived
    public default int count() {
        return delegationTypes().size();
    }

}
