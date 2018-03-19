package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;



@Value.Immutable
@JsonSerialize(as = ImmutableResourceAccessView.class)
public interface ResourceAccessView {


    @JsonProperty("resource")
    public abstract ResourceView resource();

    @JsonProperty("permitted")
    public abstract Boolean permitted();


}