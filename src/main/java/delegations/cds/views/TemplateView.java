package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTemplateView.class)
public abstract class TemplateView implements BaseView {

    @JsonProperty("crn")
    public abstract String crn();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("version")
    public abstract Integer version();

    @JsonProperty("content")
    public abstract String content();
}
