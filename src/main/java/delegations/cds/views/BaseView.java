package delegations.cds.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.sql.Timestamp;

public interface BaseView {

    @JsonProperty("create_time") @Nullable
    public Timestamp createTime();

    @JsonProperty("update_time") @Nullable
    public Timestamp updateTime();

    @JsonProperty("delete_time") @Nullable
    public Timestamp deleteTime();

    @JsonProperty("deleted")
    @Value.Derived
    public default Boolean deleted() { return deleteTime() != null; }
}
