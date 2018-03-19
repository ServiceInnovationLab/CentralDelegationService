package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableRendezvousesView.class)
public interface RendezvousesView {

    @JsonProperty("rendezvouses")
    public abstract List<RendezvousView> rendezvouses();

    @JsonProperty("count")
    @Value.Derived
    public default int count() {
        return rendezvouses().size();
    }
}
